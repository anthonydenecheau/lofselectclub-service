package com.scc.lofselectclub.model;

import javax.persistence.*;

@Entity
@Table(name = "ls_range_race")
public class RangeRace {

	@Id 
    @Column(name = "id_race")
    Integer idRace;
	
	@Column(name = "break_period")
    String breakPeriod;
	   
    @Column(name = "id_range_group")
    Integer idRangeGroup;
    
	public Integer getIdRangeGroup() { return idRangeGroup; }
	public void setIdRangeGroup(Integer idRangeGroup) { this.idRangeGroup = idRangeGroup; }
	
	public Integer getIdRace() { return idRace; }
	public void setIdRace(Integer idRace) { this.idRace = idRace; }

	public String getBreakPeriod() { return breakPeriod; }
	public void setBreakPeriod(String breakPeriod) { this.breakPeriod = breakPeriod; }

}
