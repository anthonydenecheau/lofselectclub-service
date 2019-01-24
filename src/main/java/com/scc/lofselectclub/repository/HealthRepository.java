package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.HealthStatistics;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthRepository extends CrudRepository<HealthStatistics, Long> {

   List<HealthStatistics> findByIdClub(Integer idClub, Sort sort);
   
}
