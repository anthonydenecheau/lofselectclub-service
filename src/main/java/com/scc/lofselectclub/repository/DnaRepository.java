package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.DnaStatistics;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DnaRepository extends CrudRepository<DnaStatistics, Long> {

	List<DnaStatistics> findByIdClub(Integer idClub);

}
