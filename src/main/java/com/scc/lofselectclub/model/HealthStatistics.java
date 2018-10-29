package com.scc.lofselectclub.model;

import javax.persistence.*;

@Entity
@Table(name = "ls_stats_sante")
@IdClass(HealthStatisticsId.class)
public class HealthStatistics extends GenericStatistics {

   // NOTE : Pky ! IdClub, IdRace, IdVariete, Annee, Mois, IdMaladie, CodeResultat

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
   @Id
   @Column(name = "id_maladie")
   Integer idMaladie;
   @Column(name = "code_supramaladie")
   String codeSupraMaladie;
   @Column(name = "libelle_supramaladie")
   String libelleSupraMaladie;
   @Column(name = "code_maladie")
   String codeMaladie;
   @Column(name = "libelle_maladie")
   String libelleMaladie;
   @Column(name = "nature_suivi")
   Integer natureSuivi;
   @Id
   @Column(name = "code_resultat")
   String codeResultat;
   @Column(name = "libelle_resultat")
   String libelleResultat;
   @Column(name = "nb_resultat")
   Integer nbResultat;
   @Id
   @Column(name = "annee")
   Integer annee;
   @Id
   @Column(name = "mois")
   Integer mois;

   public HealthStatistics() {

   }

   public HealthStatistics(Integer idClub, String nomClub, Integer numClub, Integer idRace, String nomRace,
         String codeFci, Integer idVariete, String nomVariete, Integer idMaladie, String codeSupraMaladie,
         String libelleSupraMaladie, String codeMaladie, String libelleMaladie, Integer natureSuivi,
         String codeResultat, String libelleResultat, Integer nbResultat, Integer annee, Integer mois) {
      super();
      this.idClub = idClub;
      this.nomClub = nomClub;
      this.numClub = numClub;
      this.idRace = idRace;
      this.nomRace = nomRace;
      this.codeFci = codeFci;
      this.idVariete = idVariete;
      this.nomVariete = nomVariete;
      this.idMaladie = idMaladie;
      this.codeSupraMaladie = codeSupraMaladie;
      this.libelleSupraMaladie = libelleSupraMaladie;
      this.codeMaladie = codeMaladie;
      this.libelleMaladie = libelleMaladie;
      this.natureSuivi = natureSuivi;
      this.codeResultat = codeResultat;
      this.libelleResultat = libelleResultat;
      this.nbResultat = nbResultat;
      this.annee = annee;
      this.mois = mois;
   }

   public HealthStatistics(Integer nbResultat) {
      this.nbResultat = nbResultat;
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

   public Integer getIdMaladie() {
      return idMaladie;
   }

   public void setIdMaladie(Integer idMaladie) {
      this.idMaladie = idMaladie;
   }

   public String getCodeSupraMaladie() {
      return codeSupraMaladie;
   }

   public void setCodeSupraMaladie(String codeSupraMaladie) {
      this.codeSupraMaladie = codeSupraMaladie;
   }

   public String getLibelleSupraMaladie() {
      return libelleSupraMaladie;
   }

   public void setLibelleSupraMaladie(String libelleSupraMaladie) {
      this.libelleSupraMaladie = libelleSupraMaladie;
   }

   public String getCodeMaladie() {
      return codeMaladie;
   }

   public void setCodeMaladie(String codeMaladie) {
      this.codeMaladie = codeMaladie;
   }

   public String getLibelleMaladie() {
      return libelleMaladie;
   }

   public void setLibelleMaladie(String libelleMaladie) {
      this.libelleMaladie = libelleMaladie;
   }

   public Integer getNatureSuivi() {
      return natureSuivi;
   }

   public void setNatureSuivi(Integer natureSuivi) {
      this.natureSuivi = natureSuivi;
   }

   public String getCodeResultat() {
      return codeResultat;
   }

   public void setCodeResultat(String codeResultat) {
      this.codeResultat = codeResultat;
   }

   public String getLibelleResultat() {
      return libelleResultat;
   }

   public void setLibelleResultat(String libelleResultat) {
      this.libelleResultat = libelleResultat;
   }

   public Integer getNbResultat() {
      return nbResultat;
   }

   public void setNbResultat(Integer nbResultat) {
      this.nbResultat = nbResultat;
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
      return "HealthStatistics [idClub=" + idClub + ", nomClub=" + nomClub + ", numClub=" + numClub + ", idRace="
            + idRace + ", nomRace=" + nomRace + ", codeFci=" + codeFci + ", idVariete=" + idVariete + ", nomVariete="
            + nomVariete + ", idMaladie=" + idMaladie + ", codeSupraMaladie=" + codeSupraMaladie
            + ", libelleSupraMaladie=" + libelleSupraMaladie + ", codeMaladie=" + codeMaladie + ", libelleMaladie="
            + libelleMaladie + ", natureSuivi=" + natureSuivi + ", codeResultat=" + codeResultat + ", libelleResultat="
            + libelleResultat + ", nbResultat=" + nbResultat + ", annee=" + annee + ", mois=" + mois + "]";
   }

}
