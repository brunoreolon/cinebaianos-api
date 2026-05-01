package com.brunoreolon.cinebaianosapi.api.model.group.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GroupAvailabilityResponse", description = "Disponibilidade dos identificadores únicos de um grupo.")
public record GroupAvailabilityResponse(
        FieldAvailability tag,
        FieldAvailability slug
) {

    @Schema(name = "GroupFieldAvailability", description = "Disponibilidade de um campo único do grupo.")
    public record FieldAvailability(
            String value,
            boolean available
    ) {
    }
}