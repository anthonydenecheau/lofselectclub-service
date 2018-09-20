package com.scc.lofselectclub.model;

import javax.persistence.*;

@Entity
@Table(name = "ls_stats_confirmation")
public class ConfirmationStatistics {

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
    @Id 
    @Column(name = "id_chien")
    Integer idChien;
    @Column(name = "on_taille_obligatoire")
    String onTailleObligatoire;
    @Column(name = "taille")
    Integer taille;
    @Column(name = "annee")
    Integer annee;
    @Column(name = "mois")
    Integer mois;

    public ConfirmationStatistics() {
    	
    }
    
    public ConfirmationStatistics(Integer idClub, String nomClub, Integer numClub, Integer idRace, String nomRace, String codeFci,
			Integer idVariete, String nomVariete, Integer idChien, String onTailleObligatoire, Integer taille,
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
		this.idChien = idChien;
		this.onTailleObligatoire = onTailleObligatoire;
		this.taille = taille;
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

	public Integer getIdChien() { return idChien; }
	public void setIdChien(Integer idChien) { this.idChien = idChien; }

	public String getOnTailleObligatoire() { return onTailleObligatoire; }
	public void setOnTailleObligatoire(String onTailleObligatoire) { this.onTailleObligatoire = onTailleObligatoire; }

	public Integer getTaille() { return taille; }
	public void setTaille(Integer idSaillie) { this.taille = taille; }

	public Integer getAnnee() { return annee; }
	public void setAnnee(Integer annee) { this.annee = annee; }

	public Integer getMois() { return mois; }
	public void setMois(Integer mois) { this.mois = mois;}

	@Override
	public String toString() {
		return "BreederStatistics [idClub=" + idClub + ", nomClub=" + nomClub + ", numClub=" + numClub + ", idRace=" + idRace
				+ ", nomRace=" + nomRace + ", codeFci=" + codeFci + ", idVariete=" + idVariete + ", nomVariete="
				+ nomVariete + ", idChien=" + idChien + ", onTailleObligatoire=" + onTailleObligatoire + ", taille="
				+ taille + ", annee=" + annee + ", mois=" + mois + "]";
	}
	
}
