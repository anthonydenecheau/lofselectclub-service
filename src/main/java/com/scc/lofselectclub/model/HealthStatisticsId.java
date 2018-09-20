package com.scc.lofselectclub.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class HealthStatisticsId implements Serializable {

	private static final long serialVersionUID = 1L;
	
    Integer idClub;	
    Integer idRace;
    Integer idVariete;
    Integer annee;
    Integer mois;
    Integer idMaladie;
    String codeResultat;
    
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public Integer getIdClub() { return idClub; }
	public void setIdClub(Integer idClub) { this.idClub = idClub; }
	
	public Integer getIdRace() { return idRace; }
	public void setIdRace(Integer idRace) { this.idRace = idRace; }
	
	public Integer getIdVariete() { return idVariete; }
	public void setIdVariete(Integer idVariete) { this.idVariete = idVariete; }
	
	public Integer getAnnee() { return annee; }
	public void setAnnee(Integer annee) { this.annee = annee; }
	
	public Integer getMois() { return mois; }
	public void setMois(Integer mois) { this.mois = mois; }

	public Integer getIdMaladie() { return idMaladie; }
	public void setIdMaladie(Integer idMaladie) { this.idMaladie = idMaladie; }

	public String getCodeResultat() { return codeResultat; }
	public void setCodeResultat(String codeResultat) { this.codeResultat = codeResultat; }
}
