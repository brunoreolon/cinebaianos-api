package com.brunoreolon.cinebaianosapi.domain.model;

public interface OwnableService<T extends Ownable, ID> {

    T get(ID id);

}
