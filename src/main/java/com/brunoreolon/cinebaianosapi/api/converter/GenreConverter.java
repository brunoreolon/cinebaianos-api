package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.genre.GenreResponse;
import com.brunoreolon.cinebaianosapi.api.model.genre.stats.GenreStatsResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Genre;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@AllArgsConstructor
public class GenreConverter {

    private final ModelMapper modelMapper;

    public List<GenreStatsResponse> toResponseList(Map<String, Integer> genreCount) {
        Set<Map.Entry<String, Integer>> entries = genreCount.entrySet();
        return entries.stream()
                .map(entry -> new GenreStatsResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(GenreStatsResponse::getTotal).reversed())
                .toList();
    }

    public List<GenreResponse> toDetailResponseList(List<Genre> genreList) {
        return genreList.stream()
                .map(this::toResponse)
                .toList();
    }

    public GenreResponse toResponse(Genre genre) {
        return modelMapper.map(genre, GenreResponse.class);
    }

}
