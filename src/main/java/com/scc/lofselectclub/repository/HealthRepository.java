package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.HealthStatistics;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HealthRepository extends CrudRepository<HealthStatistics, Long> {

	List<HealthStatistics> findByIdClub(Integer idClub);

}
