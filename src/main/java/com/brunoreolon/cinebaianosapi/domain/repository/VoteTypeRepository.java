package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteTypeRepository extends JpaRepository<VoteType, Long> {

    Optional<VoteType> findByName(String name);
    List<VoteType> findAllByActiveOrderByIdAsc(Boolean active);

}
