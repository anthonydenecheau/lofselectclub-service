package com.scc.lofselectclub.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "LS_RACE")
public class ConfigurationClub {

   @Column(name = "id_club")
   Integer idClub;
   @Column(name = "num_club")
   Integer numClub;
   @Column(name = "id_race")
   Integer idRace;
   @Column(name = "nom_race")
   String nomRace;
   @Id
   @Column(name = "id_variete")
   Integer idVariete;
   @Column(name = "nom_variete")
   String nomVariete;
   @Column(name = "tri")
   Integer tri;

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

   public String getNomRace() {
      return nomRace;
   }

   public void setNomRace(String nomRace) {
      this.nomRace = nomRace;
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

   public Integer getTri() {
      return tri;
   }

   public void setTri(Integer tri) {
      this.tri = tri;
   }

}