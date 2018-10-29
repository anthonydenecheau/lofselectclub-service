package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.BreederStatistics;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface BreederRepository extends CrudRepository<BreederStatistics, Long> {

   List<BreederStatistics> findByIdClub(Integer idClub);

   @Query("select c from BreederStatistics c where c.idClub = :idClub")
   Stream<BreederStatistics> findByIdClubReturnStream(@Param("idClub") Integer idClub);

}
