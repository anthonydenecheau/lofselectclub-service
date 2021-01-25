package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.RegistrationStatistics;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
public interface RegistrationRepository extends CrudRepository<RegistrationStatistics, Long> {

   List<RegistrationStatistics> findByIdClub(Integer idClub, Sort sort);
   
   Long countByIdRaceAndAnnee(Integer idRace, Integer annee);
   
   @Query("select c from BreederStatistics c where c.idClub = :idClub")
   Stream<RegistrationStatistics> findByIdClubReturnStream(@Param("idClub") Integer idClub);

}
