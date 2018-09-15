package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.RangeDefinition;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RangeDefinitionRepository extends CrudRepository<RangeDefinition, Long> {

	public List<RangeDefinition> findByIdRangeGroupOrderBySequence (Integer idRangeGroup);
}
