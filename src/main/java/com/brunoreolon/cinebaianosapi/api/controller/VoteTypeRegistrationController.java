package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.VoteTypeConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeDetailResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.service.VoteTypeRegistrationService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;

@RestController
@RequestMapping("/api/vote-types")
@AllArgsConstructor
@Tag(name = "Tipos de Voto", description = "Operações relacionadas ao gerenciamento de tipos de voto.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class VoteTypeRegistrationController {

    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final VoteTypeConverter voteTypeConverter;

    @PostMapping
    @RequireRole(roles = {Role.ADMIN})
    @Operation(summary = "Criar tipo de voto", description = "Permite que um administrador crie um novo tipo de voto no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tipo de voto criado com sucesso", content = @Content(schema = @Schema(implementation = VoteTypeDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para esta ação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<VoteTypeDetailResponse> create(
            @Parameter(description = "Dados do tipo de voto a ser criado")
            @Valid @RequestBody VoteTypeRequest voteTypeRequest) {
        VoteType voteType = voteTypeConverter.toEntityFromCreate(voteTypeRequest);
        VoteType newVoteType = voteTypeRegistrationService.save(voteType);

        return ResponseEntity.status(HttpStatus.CREATED).body(voteTypeConverter.toDetailResponse(newVoteType));
    }

    @GetMapping
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Listar tipos de voto", description = "Retorna todos os tipos de voto ativos ou inativos, conforme o filtro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipos de voto retornados com sucesso", content = @Content(schema = @Schema(implementation = VoteTypeDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<VoteTypeDetailResponse>> getAll(
            @Parameter(description = "Filtrar apenas tipos de voto ativos (true) ou inativos (false)", example = "true")
            @RequestParam(name = "active", defaultValue = "true") Boolean active) {
        List<VoteType> voteTypes = voteTypeRegistrationService.getAll(active);
        return ResponseEntity.ok().body(voteTypeConverter.toDetailResponseList(voteTypes));
    }

    @GetMapping("/{typeVoteId}")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Buscar tipo de voto por ID", description = "Retorna os detalhes de um tipo de voto específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo de voto encontrado", content = @Content(schema = @Schema(implementation = VoteTypeDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<VoteTypeDetailResponse> get(
            @Parameter(description = "ID do tipo de voto", example = "1")
            @PathVariable Long typeVoteId) {
        VoteType voteType = voteTypeRegistrationService.get(typeVoteId);
        return ResponseEntity.ok().body(voteTypeConverter.toDetailResponse(voteType));
    }

    @DeleteMapping("/{typeVoteId}")
    @RequireRole(roles = {Role.ADMIN})
    @Operation(summary = "Excluir tipo de voto", description = "Permite que um administrador exclua um tipo de voto específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tipo de voto excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para esta ação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do tipo de voto", example = "1")
            @PathVariable Long typeVoteId) {
        voteTypeRegistrationService.delete(typeVoteId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{typeVoteId}")
    @RequireRole(roles = {Role.ADMIN})
    @Operation(summary = "Editar tipo de voto", description = "Permite que um administrador atualize os dados de um tipo de voto existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo de voto atualizado com sucesso", content = @Content(schema = @Schema(implementation = VoteTypeDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para esta ação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<VoteTypeDetailResponse> edit(
            @Parameter(description = "ID do tipo de voto", example = "1")
            @PathVariable Long typeVoteId,

            @Parameter(description = "Dados atualizados do tipo de voto")
            @Valid @RequestBody VoteTypeUpdateRequest voteTypeUpdateRequest) {
        VoteType voteType = voteTypeConverter.toEntityFromUpdate(voteTypeUpdateRequest);
        voteType.setId(typeVoteId);

        VoteType voteTypeUpdated = voteTypeRegistrationService.save(voteType);

        return ResponseEntity.ok().body(voteTypeConverter.toDetailResponse(voteTypeUpdated));
    }

}