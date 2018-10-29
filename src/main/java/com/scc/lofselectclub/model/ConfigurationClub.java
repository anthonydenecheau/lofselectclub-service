package com.scc.lofselectclub.model;

import javax.persistence.*;

@Entity
@Table(name = "ls_race")
public class ConfigurationClub {

   @Column(name = "id_club")
   Integer idClub;

   @Column(name = "num_club")
   Integer numClub;

   @Column(name = "id_race")
   Integer idRace;

   @Column(name = "nom_race")
   String libelleRace;

   @Id
   @Column(name = "id_variete")
   Integer idVariete;

   @Column(name = "nom_variete")
   String libelleVariete;

   public Integer getIdClub() {
      return idClub;
   }

   public void setIdClub(Integer idClub) {
      this.idClub = idClub;
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

   public String getLibelleRace() {
      return libelleRace;
   }

   public void setLibelleRace(String libelleRace) {
      this.libelleRace = libelleRace;
   }

   public Integer getIdVariete() {
      return idVariete;
   }

   public void setIdVariete(Integer idVariete) {
      this.idVariete = idVariete;
   }

   public String getLibelleVariete() {
      return libelleVariete;
   }

   public void setLibelleVariete(String libelleVariete) {
      this.libelleVariete = libelleVariete;
   }

}