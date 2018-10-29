package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.DnaStatistics;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DnaRepository extends CrudRepository<DnaStatistics, Long> {

   List<DnaStatistics> findByIdClub(Integer idClub);
}
