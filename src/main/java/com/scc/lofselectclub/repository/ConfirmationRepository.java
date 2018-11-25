package com.scc.lofselectclub.repository;

import com.scc.lofselectclub.model.ConfirmationStatistics;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConfirmationRepository extends CrudRepository<ConfirmationStatistics, Long> {

   List<ConfirmationStatistics> findByIdClub(Integer idClub);

   List<ConfirmationStatistics> findByIdRaceAndAnneeAndSexe(Integer idRace, Integer annee, String sexe);

   List<ConfirmationStatistics> findByIdVarieteAndAnneeAndSexe(Integer idVariete, Integer annee, String sexe);

}
