package com.scc.lofselectclub.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "ls_stats_performance")
@IdClass(PerformanceStatisticsId.class)
public class PerformanceStatistics extends GenericStatistics {

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
   @Column(name = "sexe")
   String sexe;
   @Id
   @Column(name = "id_key")
   Integer idKey;
   @Column(name = "code_key")
   String codeKey;
   @Column(name = "name_key")
   String nameKey;
   @Column(name = "qtity")
   Integer qtity;
   @Id
   @Column(name = "type_key")
   Integer typeKey;
   @Id
   @Column(name = "annee")
   Integer annee;
   @Id
   @Column(name = "mois")
   Integer mois;
   @Column(name = "tri")
   Integer tri;
   
   public PerformanceStatistics() {
      super();
   }

   public PerformanceStatistics(Integer qtity) {
      this.qtity = qtity;
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

   public String getCodeKey() {
      return codeKey;
   }

   public void setCodeKey(String codeKey) {
      this.codeKey = codeKey;
   }
   
   public String getNameKey() {
      return nameKey;
   }

   public void setNameKey(String nameKey) {
      this.nameKey = nameKey;
   }

   public Integer getQtity() {
      return qtity;
   }

   public void setQtity(Integer qtity) {
      this.qtity = qtity;
   }

   public Integer getTypeKey() {
      return typeKey;
   }

   public void setTypeKey(Integer typeKey) {
      this.typeKey = typeKey;
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
   public Integer getTri() {
      return tri;
   }
   public void setTri(Integer tri) {
      this.tri = tri;
   }

   @Override
   public String toString() {
      return "PerformanceStatistics [idClub=" + idClub + ", nomClub=" + nomClub + ", numClub=" + numClub + ", idRace="
            + idRace + ", nomRace=" + nomRace + ", codeFci=" + codeFci + ", idVariete=" + idVariete + ", nomVariete="
            + nomVariete + ", sexe=" + sexe + ", idKey=" + idKey + ", nameKey=" + nameKey + ", qtity=" + qtity
            + ", typeKey=" + typeKey + ", annee=" + annee + ", mois=" + mois + ", tri=" + tri + "]";
   }

}
