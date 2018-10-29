package com.scc.lofselectclub.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.scc.lofselectclub.model.ConfigurationRace;

@Repository
public interface ConfigurationRaceRepository extends CrudRepository<ConfigurationRace, Long> {

   ConfigurationRace findByIdRace(Integer idRace);

}
