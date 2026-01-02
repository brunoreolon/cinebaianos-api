package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.UserConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.ResourceKey;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.UserRepository;
import com.brunoreolon.cinebaianosapi.domain.service.UserRegistratioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.brunoreolon.cinebaianosapi.api.model.ValidationGroups.*;
import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Tag(name = "Usuários", description = "Operações relacionadas ao gerenciamento de usuários, incluindo criação, consulta, atualização e exclusão.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class UserRegistrationController {

    private final UserRegistratioService userRegistratioService;
    private final UserRepository userRepository;
    private final UserConverter userConverter;

    @PostMapping
    @RequireRole(roles = {Role.ADMIN}, allowBot = true)
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema. Permite criação por administradores ou bots autorizados.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso", content = @Content(schema = @Schema(implementation = UserDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para criar outro usuário", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserDetailResponse> create(
            @Parameter(description = "Dados do usuário a ser criado")
            @Validated(UserCreateGroup.class) @RequestBody UserRequest userRequestuser) {
        User newUser = userRegistratioService.create(userConverter.toEntityFromCreate(userRequestuser));
        return ResponseEntity.status(HttpStatus.CREATED).body(userConverter.toDetailResponse(newUser));
    }

    @GetMapping
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Listar usuários", description = "Retorna todos os usuários cadastrados. É possível incluir ou excluir bots do resultado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuários retornados com sucesso", content = @Content(schema = @Schema(implementation = UserDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<UserDetailResponse>> getAll(
            @Parameter(description = "Incluir bots na listagem", example = "false")
            @RequestParam(value = "includeBot", required = false, defaultValue = "false") boolean includeBot
    ) {
        List<User> users = userRegistratioService.getAll();

        if (!includeBot) {
            users = users.stream()
                    .filter(user -> !user.getIsBot())
                    .toList();
        }

        return ResponseEntity.ok().body(userConverter.toDetailResponseList(users));
    }

    @GetMapping("/{discordId}")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Buscar usuário por Discord ID", description = "Retorna os detalhes de um usuário específico pelo seu Discord ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado", content = @Content(schema = @Schema(implementation = UserDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserDetailResponse> get(
            @Parameter(description = "Discord ID do usuário", example = "987654321098765432")
            @PathVariable String discordId) {
        User user = userRegistratioService.get(discordId);
        return ResponseEntity.ok().body(userConverter.toDetailResponse(user));
    }

    @GetMapping("/me")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Consultar dados do usuário logado", description = "Retorna os detalhes do usuário autenticado com base no token de acesso.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso", content = @Content(schema = @Schema(implementation = UserDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserDetailResponse> me(
            @Parameter(description = "Usuário autenticado (injetado pelo Spring Security)")
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        return ResponseEntity.ok(userConverter.toDetailResponse(user));
    }

    @DeleteMapping("/{discordId}")
    @RequireRole(roles = {Role.ADMIN})
    @Operation(summary = "Excluir usuário", description = "Remove um usuário do sistema com base no Discord ID. Apenas administradores podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para excluir outro usuário", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Discord ID do usuário a ser excluído", example = "987654321098765432")
            @PathVariable @ResourceKey String discordId) {
        userRegistratioService.delete(discordId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{discordId}")
    @CheckOwner(service = UserRegistratioService.class, allowAdmin = true)
    @Operation(summary = "Atualizar usuário", description = "Atualiza os dados de um usuário existente. Administradores podem atualizar qualquer usuário, outros só podem atualizar seu próprio registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso", content = @Content(schema = @Schema(implementation = UserDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Permissão negada para atualizar o usuário", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserDetailResponse> update(
            @Parameter(description = "Discord ID do usuário", example = "987654321098765432")
            @PathVariable @ResourceKey String discordId,

            @Parameter(description = "Dados para atualização do usuário")
            @Valid @RequestBody UserUpdateRequest userRequest) {
        User userUpdate = userConverter.toEntityFromUpdate(userRequest);
        userUpdate.setDiscordId(discordId);

        User existingUser = userRegistratioService.get(discordId);
        existingUser = userConverter.merge(userUpdate, existingUser);

        User updated = userRegistratioService.update(existingUser);
        return ResponseEntity.ok().body(userConverter.toDetailResponse(updated));
    }
}