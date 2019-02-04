package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.PerformanceStatistics;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerformanceRepository extends CrudRepository<PerformanceStatistics, Long> {

   List<PerformanceStatistics> findByIdClub(Integer idClub, Sort sort);
}
