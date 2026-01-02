package com.brunoreolon.cinebaianosapi.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Schema(description = "Detalhes de erro retornados pela API")
public class ApiErrorResponse {

    @Schema(description = "Título do erro", example = "Token Expired")
    private String title;

    @Schema(description = "Código HTTP", example = "401")
    private int status;

    @Schema(description = "Mensagem detalhada do erro", example = "The access token has expired")
    private String detail;

    @Schema(description = "Código de erro específico da API", example = "token_expired")
    private String errorCode;

    @Schema(description = "Propriedades adicionais do erro")
    private Map<String, Object> properties;

}
