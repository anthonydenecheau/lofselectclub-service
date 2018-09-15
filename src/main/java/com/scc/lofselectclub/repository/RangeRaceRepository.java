package com.scc.lofselectclub.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.scc.lofselectclub.model.RangeRace;

@Repository
public interface RangeRaceRepository extends CrudRepository<RangeRace, Long> {

    RangeRace findByIdRace(Integer idRace);

}
