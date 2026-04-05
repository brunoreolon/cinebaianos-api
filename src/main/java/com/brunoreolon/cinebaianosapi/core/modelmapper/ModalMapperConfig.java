package com.brunoreolon.cinebaianosapi.core.modelmapper;

import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteSummaryResponse;
import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
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

        modelMapper.typeMap(User.class, User.class).addMappings(mapper -> {
            mapper.skip(User::setMovies);
            mapper.skip(User::setActive);
            mapper.skip(User::setIsBot);
        });

        modelMapper.createTypeMap(ClientMovieDetailsResponse.class, Movie.class)
                .addMappings(mapper -> {
                    mapper.skip(Movie::setId);
                    mapper.map(ClientMovieDetailsResponse::getId, Movie::setTmdbId);
                    mapper.using(Converters.DATE_CONVERTER)
                            .map(ClientMovieDetailsResponse::getReleaseDate, Movie::setYear);
//                    mapper.using(Converters.GENRE_CONVERTER)
//                            .map(ClientMovieDetailsResponse::getGenres, Movie::setGenre);
                });

        modelMapper.createTypeMap(User.class, UserDetailResponse.class)
                .addMappings(mapper -> {
                    mapper.map(User::getCreatedAt, UserDetailResponse::setJoined);
                    mapper.map(User::getAvatar, UserDetailResponse::setAvatar);
                    mapper.map(User::getBiography, UserDetailResponse::setBiography);
                });

        modelMapper.createTypeMap(Vote.class, VoteSummaryResponse.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getVote().getId(), VoteSummaryResponse::setId);
                    mapper.map(src -> src.getVote().getDescription(), VoteSummaryResponse::setDescription);
                    mapper.map(src -> src.getVote().getColor(), VoteSummaryResponse::setColor);
                    mapper.map(src -> src.getVote().getEmoji(), VoteSummaryResponse::setEmoji);
                    mapper.map(Vote::getCreatedAt, VoteSummaryResponse::setVotedAt);
                });

        modelMapper.typeMap(UserRequest.class, User.class)
                .addMappings(mapper -> mapper.skip(User::setId));

        modelMapper.typeMap(User.class, UserSummaryResponse.class)
                .addMappings(mapper -> mapper.map(User::getId, UserSummaryResponse::setId));

        return modelMapper;
    }

}
