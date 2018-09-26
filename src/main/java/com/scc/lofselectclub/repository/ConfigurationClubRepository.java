package com.scc.lofselectclub.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.scc.lofselectclub.model.ConfigurationClub;

@Repository
public interface ConfigurationClubRepository extends CrudRepository<ConfigurationClub, Long> {

	List<ConfigurationClub> findByIdClub(Integer idClub);

}
