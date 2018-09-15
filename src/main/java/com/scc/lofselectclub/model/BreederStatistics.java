package com.scc.lofselectclub.model;

import javax.persistence.*;

@Entity
@Table(name = "ls_stats_eleveur")
public class BreederStatistics {

	@Column(name = "id_club")
    Integer idClub;	
    @Column(name = "nom_club")
    String nomClub;
    @Column(name = "num_club")
    Integer numClub;
    @Column(name = "id_race")
    Integer idRace;
    @Column(name = "nom_race")
    String nomRace;
    @Column(name = "code_fci")
    String codeFci;
    @Column(name = "id_variete")
    Integer idVariete;
    @Column(name = "nom_variete")
    String nomVariete;
    @Column(name = "id_eleveur")
    Integer idEleveur;
    @Column(name = "affixe_eleveur")
    String affixeEleveur;
    
    @Id 
    @Column(name = "id_saillie")
    Integer idSaillie;
    @Column(name = "annee")
    Integer annee;
    @Column(name = "mois")
    Integer mois;

    public BreederStatistics() {
    	
    }
    
    public BreederStatistics(Integer idClub, String nomClub, Integer numClub, Integer idRace, String nomRace, String codeFci,
			Integer idVariete, String nomVariete, Integer idEleveur, String affixeEleveur, Integer idSaillie,
			Integer annee, Integer mois) {
		super();
		this.idClub = idClub;
		this.nomClub = nomClub;
		this.numClub = numClub;
		this.idRace = idRace;
		this.nomRace = nomRace;
		this.codeFci = codeFci;
		this.idVariete = idVariete;
		this.nomVariete = nomVariete;
		this.idEleveur = idEleveur;
		this.affixeEleveur = affixeEleveur;
		this.idSaillie = idSaillie;
		this.annee = annee;
		this.mois = mois;
	}


	public Integer getIdClub() { return idClub; }
	public void setIdClub(Integer idClub) { this.idClub = idClub; }

	public String getNomClub() { return nomClub; }
	public void setNomClub(String nomClub) { this.nomClub = nomClub; }

	public Integer getNumClub() { return numClub; }
	public void setNumClub(Integer numClub) { this.numClub = numClub; }

	public Integer getIdRace() { return idRace; }
	public void setIdRace(Integer idRace) { this.idRace = idRace; }

	public String getNomRace() { return nomRace; }
	public void setNomRace(String nomRace) { this.nomRace = nomRace; }

	public String getCodeFci() { return codeFci; }
	public void setCodeFci(String codeFci) { this.codeFci = codeFci; }

	public Integer getIdVariete() { return idVariete; }
	public void setIdVariete(Integer idVariete) { this.idVariete = idVariete; }

	public String getNomVariete() { return nomVariete; }
	public void setNomVariete(String nomVariete) { this.nomVariete = nomVariete; }

	public Integer getIdEleveur() { return idEleveur; }
	public void setIdEleveur(Integer idEleveur) { this.idEleveur = idEleveur; }

	public String getAffixeEleveur() { return affixeEleveur; }
	public void setAffixeEleveur(String affixeEleveur) { this.affixeEleveur = affixeEleveur; }

	public Integer getIdSaillie() { return idSaillie; }
	public void setIdSaillie(Integer idSaillie) { this.idSaillie = idSaillie; }

	public Integer getAnnee() { return annee; }
	public void setAnnee(Integer annee) { this.annee = annee; }

	public Integer getMois() { return mois; }
	public void setMois(Integer mois) { this.mois = mois;}

	@Override
	public String toString() {
		return "BreederStatistics [idClub=" + idClub + ", nomClub=" + nomClub + ", numClub=" + numClub + ", idRace=" + idRace
				+ ", nomRace=" + nomRace + ", codeFci=" + codeFci + ", idVariete=" + idVariete + ", nomVariete="
				+ nomVariete + ", idEleveur=" + idEleveur + ", affixeEleveur=" + affixeEleveur + ", idSaillie="
				+ idSaillie + ", annee=" + annee + ", mois=" + mois + "]";
	}
	
}
