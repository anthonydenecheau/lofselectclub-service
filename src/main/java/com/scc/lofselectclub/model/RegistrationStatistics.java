package com.scc.lofselectclub.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ls_stats_inscription")
public class RegistrationStatistics extends GenericStatistics{

   // NOTE : Pky ! idSaillie

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
   @Column(name = "id_saillie")
   Integer idSaillie;
   @Column(name = "nb_male")
   Integer nbMale;
   @Column(name = "nb_femelle")
   Integer nbFemelle;
   @Column(name = "annee")
   Integer annee;
   @Column(name = "mois")
   Integer mois;
   @Column(name = "tri")
   Integer tri;

   public RegistrationStatistics() {
      super();
   }

   public RegistrationStatistics(Integer nbMale, Integer nbFemelle) {
      super();
      this.nbMale = nbMale;
      this.nbFemelle = nbFemelle;
   }

   public RegistrationStatistics(Integer idClub, String nomClub, Integer numClub, Integer idRace, String nomRace,
         String codeFci, Integer idVariete, String nomVariete,
         Integer idSaillie, Integer nbMale, Integer nbFemelle, Integer annee, Integer mois, Integer tri) {
      super();
      this.idClub = idClub;
      this.nomClub = nomClub;
      this.numClub = numClub;
      this.idRace = idRace;
      this.nomRace = nomRace;
      this.codeFci = codeFci;
      this.idVariete = idVariete;
      this.nomVariete = nomVariete;
      this.idSaillie = idSaillie;
      this.nbMale = nbMale;
      this.nbFemelle = nbFemelle;
      this.annee = annee;
      this.mois = mois;
      this.tri = tri;
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

   public Integer getIdSaillie() {
      return idSaillie;
   }

   public void setIdSaillie(Integer idSaillie) {
      this.idSaillie = idSaillie;
   }


   public Integer getNbMale() {
      return nbMale;
   }

   public void setNbMale(Integer nbMale) {
      this.nbMale = nbMale;
   }

   public Integer getNbFemelle() {
      return nbFemelle;
   }

   public void setNbFemelle(Integer nbFemelle) {
      this.nbFemelle = nbFemelle;
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
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((annee == null) ? 0 : annee.hashCode());
      result = prime * result + ((codeFci == null) ? 0 : codeFci.hashCode());
      result = prime * result + ((idClub == null) ? 0 : idClub.hashCode());
      result = prime * result + ((idRace == null) ? 0 : idRace.hashCode());
      result = prime * result + ((idSaillie == null) ? 0 : idSaillie.hashCode());
      result = prime * result + ((idVariete == null) ? 0 : idVariete.hashCode());
      result = prime * result + ((mois == null) ? 0 : mois.hashCode());
      result = prime * result + ((nbFemelle == null) ? 0 : nbFemelle.hashCode());
      result = prime * result + ((nbMale == null) ? 0 : nbMale.hashCode());
      result = prime * result + ((nomClub == null) ? 0 : nomClub.hashCode());
      result = prime * result + ((nomRace == null) ? 0 : nomRace.hashCode());
      result = prime * result + ((nomVariete == null) ? 0 : nomVariete.hashCode());
      result = prime * result + ((numClub == null) ? 0 : numClub.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      BreederStatistics other = (BreederStatistics) obj;
      if (annee == null) {
         if (other.annee != null)
            return false;
      } else if (!annee.equals(other.annee))
         return false;
      if (codeFci == null) {
         if (other.codeFci != null)
            return false;
      } else if (!codeFci.equals(other.codeFci))
         return false;
      if (idClub == null) {
         if (other.idClub != null)
            return false;
      } else if (!idClub.equals(other.idClub))
         return false;
      if (idRace == null) {
         if (other.idRace != null)
            return false;
      } else if (!idRace.equals(other.idRace))
         return false;
      if (idSaillie == null) {
         if (other.idSaillie != null)
            return false;
      } else if (!idSaillie.equals(other.idSaillie))
         return false;
      if (idVariete == null) {
         if (other.idVariete != null)
            return false;
      } else if (!idVariete.equals(other.idVariete))
         return false;
      if (mois == null) {
         if (other.mois != null)
            return false;
      } else if (!mois.equals(other.mois))
         return false;
      if (nbFemelle == null) {
         if (other.nbFemelle != null)
            return false;
      } else if (!nbFemelle.equals(other.nbFemelle))
         return false;
      if (nbMale == null) {
         if (other.nbMale != null)
            return false;
      } else if (!nbMale.equals(other.nbMale))
         return false;
      if (nomClub == null) {
         if (other.nomClub != null)
            return false;
      } else if (!nomClub.equals(other.nomClub))
         return false;
      if (nomRace == null) {
         if (other.nomRace != null)
            return false;
      } else if (!nomRace.equals(other.nomRace))
         return false;
      if (nomVariete == null) {
         if (other.nomVariete != null)
            return false;
      } else if (!nomVariete.equals(other.nomVariete))
         return false;
      if (numClub == null) {
         if (other.numClub != null)
            return false;
      } else if (!numClub.equals(other.numClub))
         return false;
      return true;
   }

   
   @Override
   public String toString() {
      return "RegisterStatistics [idClub=" + idClub + ", nomClub=" + nomClub + ", numClub=" + numClub + ", idRace="
            + idRace + ", nomRace=" + nomRace + ", codeFci=" + codeFci + ", idVariete=" + idVariete + ", nomVariete="
            + nomVariete + ", idSaillie=" + idSaillie + ", nbMale=" + nbMale + ", nbFemelle=" + nbFemelle + ", annee="
            + annee + ", mois=" + mois + ", tri=" + tri + "]";
   }

}
