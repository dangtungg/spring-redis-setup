package com.example.common.mapper;

import java.util.List;

public interface EntityMapper<D, E> {

    E toEntity(D dto);

    D toDTO(E entity);

    List<E> toEntities(List<D> dtoList);

    List<D> toDTOs(List<E> entityList);

}
