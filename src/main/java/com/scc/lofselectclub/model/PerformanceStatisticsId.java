package com.scc.lofselectclub.model;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class PerformanceStatisticsId implements Serializable {

   private static final long serialVersionUID = 1L;

   Integer idClub;
   Integer idRace;
   Integer idVariete;
   Integer annee;
   Integer mois;
   String sexe;
   Integer idKey;
   Integer typeKey;

   public Integer getIdClub() {
      return idClub;
   }

   public void setIdClub(Integer idClub) {
      this.idClub = idClub;
   }

   public Integer getIdRace() {
      return idRace;
   }

   public void setIdRace(Integer idRace) {
      this.idRace = idRace;
   }

   public Integer getIdVariete() {
      return idVariete;
   }

   public void setIdVariete(Integer idVariete) {
      this.idVariete = idVariete;
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

   public String getSexe() {
      return sexe;
   }

   public void setSexe(String sexe) {
      this.sexe = sexe;
   }
   
   public Integer getIdKey() {
      return idKey;
   }

   public void setIdKey(Integer idKey) {
      this.idKey = idKey;
   }

   public Integer getTypeKey() {
      return typeKey;
   }

   public void setTypeKey(Integer typeKey) {
      this.typeKey = typeKey;
   }   

}
