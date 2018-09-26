package com.scc.lofselectclub.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ls_parametrage_race")
public class ConfigurationRace {

	@Id
	@Column(name = "id_race")
	Integer idRace;

	@Column(name = "break_period")
	String breakPeriod;

	@Column(name = "id_serie_group")
	Integer idSerieGroup;

	@Column(name = "date_calcul")
	Date lastDate;

	public Integer getIdSerieGroup() {
		return idSerieGroup;
	}

	public void setIdSerieGroup(Integer idSerieGroup) {
		this.idSerieGroup = idSerieGroup;
	}

	public Integer getIdRace() {
		return idRace;
	}

	public void setIdRace(Integer idRace) {
		this.idRace = idRace;
	}

	public String getBreakPeriod() {
		return breakPeriod;
	}

	public void setBreakPeriod(String breakPeriod) {
		this.breakPeriod = breakPeriod;
	}

	public Date getLastDate() {
		return lastDate;
	}

	public void setLastDate(Date lastDate) {
		this.lastDate = lastDate;
	}

}
