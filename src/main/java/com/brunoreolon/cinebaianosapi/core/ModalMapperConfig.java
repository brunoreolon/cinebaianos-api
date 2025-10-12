package com.brunoreolon.cinebaianosapi.core;

import com.brunoreolon.cinebaianosapi.client.model.MovieResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModalMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.getConfiguration().setFieldMatchingEnabled(true);
        modelMapper.getConfiguration().setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        modelMapper.createTypeMap(MovieResponse.class, Movie.class)
                .addMappings(mapper -> {
                    mapper.skip(Movie::setId);
                    mapper.map(MovieResponse::getId, Movie::setTmbdId);
                    mapper.using(Converters.DATE_CONVERTER)
                            .map(MovieResponse::getReleaseDate, Movie::setYear);
                    mapper.using(Converters.GENRE_CONVERTER)
                            .map(MovieResponse::getGenres, Movie::setGenre);
                });

        return modelMapper;
    }

}
