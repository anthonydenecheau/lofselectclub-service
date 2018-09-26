package com.scc.lofselectclub.model;

import javax.persistence.*;

@Entity
@Table(name = "ls_stats_adn")
@IdClass(DnaStatisticsId.class)
public class DnaStatistics {

	@Id
	@Column(name = "id_club")
	Integer idClub;
	@Column(name = "nom_club")
	String nomClub;
	@Column(name = "num_club")
	Integer numClub;
	@Id
	@Column(name = "id_race")
	Integer idRace;
	@Column(name = "nom_race")
	String nomRace;
	@Column(name = "code_fci")
	String codeFci;
	@Id
	@Column(name = "id_variete")
	Integer idVariete;
	@Column(name = "nom_variete")
	String nomVariete;
	@Column(name = "dna")
	Integer dna;
	@Column(name = "dnacomp")
	Integer dnaComp;
	@Column(name = "dnacomp_p")
	Integer dnaCompP;
	@Column(name = "dnacomp_m")
	Integer dnaCompM;
	@Id
	@Column(name = "annee")
	Integer annee;
	@Id
	@Column(name = "mois")
	Integer mois;

	public DnaStatistics() {

	}

	public DnaStatistics(Integer idClub, String nomClub, Integer numClub, Integer idRace, String nomRace,
			String codeFci, Integer idVariete, String nomVariete, Integer dna, Integer dnaComp, Integer dnaCompP,
			Integer dnaCompM, Integer annee, Integer mois) {
		super();
		this.idClub = idClub;
		this.nomClub = nomClub;
		this.numClub = numClub;
		this.idRace = idRace;
		this.nomRace = nomRace;
		this.codeFci = codeFci;
		this.idVariete = idVariete;
		this.nomVariete = nomVariete;
		this.dna = dna;
		this.dnaComp = dnaComp;
		this.dnaCompP = dnaCompP;
		this.dnaCompM = dnaCompM;
		this.annee = annee;
		this.mois = mois;
	}

	public DnaStatistics(Integer dna, Integer dnaComp, Integer dnaCompP, Integer dnaCompM) {
		this.dna = dna;
		this.dnaComp = dnaComp;
		this.dnaCompP = dnaCompP;
		this.dnaCompM = dnaCompM;
	}

	public Integer getIdClub() {
		return idClub;
	}

	public void setIdClub(Integer idClub) {
		this.idClub = idClub;
	}

	public String getNomClub() {
		return nomClub;
	}

	public void setNomClub(String nomClub) {
		this.nomClub = nomClub;
	}

	public Integer getNumClub() {
		return numClub;
	}

	public void setNumClub(Integer numClub) {
		this.numClub = numClub;
	}

	public Integer getIdRace() {
		return idRace;
	}

	public void setIdRace(Integer idRace) {
		this.idRace = idRace;
	}

	public String getNomRace() {
		return nomRace;
	}

	public void setNomRace(String nomRace) {
		this.nomRace = nomRace;
	}

	public String getCodeFci() {
		return codeFci;
	}

	public void setCodeFci(String codeFci) {
		this.codeFci = codeFci;
	}

	public Integer getIdVariete() {
		return idVariete;
	}

	public void setIdVariete(Integer idVariete) {
		this.idVariete = idVariete;
	}

	public String getNomVariete() {
		return nomVariete;
	}

	public void setNomVariete(String nomVariete) {
		this.nomVariete = nomVariete;
	}

	public Integer getDna() {
		return dna;
	}

	public void setDna(Integer dna) {
		this.dna = dna;
	}

	public Integer getDnaComp() {
		return dnaComp;
	}

	public void setDnaComp(Integer dnaComp) {
		this.dnaComp = dnaComp;
	}

	public Integer getDnaCompP() {
		return dnaCompP;
	}

	public void setDnaCompP(Integer dnaCompP) {
		this.dnaCompP = dnaCompP;
	}

	public Integer getDnaCompM() {
		return dnaCompM;
	}

	public void setDnaCompM(Integer dnaCompM) {
		this.dnaCompM = dnaCompM;
	}

	public Integer getAnnee() {
		return annee;
	}

	public void setAnnee(Integer annee) {
		this.annee = annee;
	}

	public Integer getMois() {
		return mois;
	}

	public void setMois(Integer mois) {
		this.mois = mois;
	}

	@Override
	public String toString() {
		return "BirthStatistics [idClub=" + idClub + ", nomClub=" + nomClub + ", numClub=" + numClub + ", idRace="
				+ idRace + ", nomRace=" + nomRace + ", codeFci=" + codeFci + ", idVariete=" + idVariete
				+ ", nomVariete=" + nomVariete + ", dna=" + dna + ", dnaComp=" + dnaComp + ", dnaCompP=" + dnaCompP
				+ ", dnaCompM=" + dnaCompM + ", annee=" + annee + ", mois=" + mois + "]";
	}

}
