package com.brunoreolon.cinebaianosapi.core;

import com.brunoreolon.cinebaianosapi.client.model.GenreResponse;
import org.modelmapper.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class Converters {

    public static final Converter<List<GenreResponse>, String> GENRE_CONVERTER =
            ctx -> {
                List<GenreResponse> genres = ctx.getSource();
                if (genres == null || genres.isEmpty()) {
                    return "";
                }
                return genres.getFirst().getName();
            };

    public static final Converter<LocalDate, String> DATE_CONVERTER = ctx -> {
        LocalDate releaseDate = ctx.getSource();
        return releaseDate == null ? "" : String.valueOf(releaseDate.getYear());
    };

}
