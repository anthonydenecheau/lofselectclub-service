package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.SerieDefinition;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SerieDefinitionRepository extends CrudRepository<SerieDefinition, Long> {

	public List<SerieDefinition> findByIdSerieGroupOrderBySequence(Integer idSerieGroup);
}
