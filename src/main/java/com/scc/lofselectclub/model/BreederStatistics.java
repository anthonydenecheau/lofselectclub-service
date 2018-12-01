package com.scc.lofselectclub.model;

import javax.persistence.*;

@Entity
@Table(name = "ls_stats_eleveur")
public class BreederStatistics  extends GenericStatistics {

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
   @Column(name = "id_eleveur")
   Integer idEleveur;
   @Column(name = "affixe_eleveur")
   String affixeEleveur;
   @Id
   @Column(name = "id_saillie")
   Integer idSaillie;
   @Column(name = "cotation_portee")
   Integer cotationPortee;
   @Column(name = "id_etalon")
   Integer idEtalon;
   @Column(name = "nom_etalon")
   String nomEtalon;
   @Column(name = "cotation_etalon")
   Integer cotationEtalon;
   @Column(name = "type_etalon")
   Integer typeEtalon;
   @Column(name = "new_etalon")
   String premiereSaillieEtalon;
   @Column(name = "id_lice")
   Integer idLice;
   @Column(name = "cotation_lice")
   Integer cotationLice;
   @Column(name = "type_lice")
   Integer typeLice;
   @Column(name = "new_lice")
   String premiereSaillieLice;
   @Column(name = "nb_male")
   Integer nbMale;
   @Column(name = "nb_femelle")
   Integer nbFemelle;
   @Column(name = "prolificite_race")
   Double prolificiteRace;
   @Column(name = "prolificite_variete")
   Double prolificiteVariete;
   @Column(name = "nb_ancetre_commun")
   Integer nbAncetreCommun;
   @Column(name = "consanguinite")
   Double consanguinite;
   @Column(name = "annee")
   Integer annee;
   @Column(name = "mois")
   Integer mois;

   public BreederStatistics() {
      super();
   }

   public BreederStatistics(Integer idEtalon, String nomEtalon) {
      this.idEtalon = idEtalon;
      this.nomEtalon = nomEtalon;
   }

   public BreederStatistics(Integer nbMale, Integer nbFemelle) {
      this.nbMale = nbMale;
      this.nbFemelle = nbFemelle;
   }

   public BreederStatistics(Integer idClub, String nomClub, Integer numClub, Integer idRace, String nomRace,
         String codeFci, Integer idVariete, String nomVariete, Integer idEleveur, String affixeEleveur,
         Integer idSaillie, Integer cotationPortee, Integer nbMale, Integer nbFemelle, Double prolificiteRace,
         Double prolificiteVariete, Integer annee, Integer mois) {
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
      this.cotationPortee = cotationPortee;
      this.nbMale = nbMale;
      this.nbFemelle = nbFemelle;
      this.prolificiteRace = prolificiteRace;
      this.prolificiteVariete = prolificiteVariete;
      this.annee = annee;
      this.mois = mois;
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

   public Integer getIdEleveur() {
      return idEleveur;
   }

   public void setIdEleveur(Integer idEleveur) {
      this.idEleveur = idEleveur;
   }

   public String getAffixeEleveur() {
      return affixeEleveur;
   }

   public void setAffixeEleveur(String affixeEleveur) {
      this.affixeEleveur = affixeEleveur;
   }

   public Integer getIdSaillie() {
      return idSaillie;
   }

   public void setIdSaillie(Integer idSaillie) {
      this.idSaillie = idSaillie;
   }

   public Integer getCotationPortee() {
      return cotationPortee;
   }

   public void setCotationPortee(Integer cotationPortee) {
      this.cotationPortee = cotationPortee;
   }

   public Integer getIdEtalon() {
      return idEtalon;
   }

   public void setIdEtalon(Integer idEtalon) {
      this.idEtalon = idEtalon;
   }

   public String getNomEtalon() {
      return nomEtalon;
   }

   public void setNomEtalon(String nomEtalon) {
      this.nomEtalon = nomEtalon;
   }

   public Integer getCotationEtalon() {
      return cotationEtalon;
   }

   public void setCotationEtalon(Integer cotationEtalon) {
      this.cotationEtalon = cotationEtalon;
   }

   public Integer getTypeEtalon() {
      return typeEtalon;
   }

   public String getPremiereSaillieEtalon() {
      return premiereSaillieEtalon;
   }

   public void setPremiereSaillieEtalon(String  premiereSaillieEtalon) {
      this.premiereSaillieEtalon = premiereSaillieEtalon;
   }

   public void setTypeEtalon(Integer typeEtalon) {
      this.typeEtalon = typeEtalon;
   }

   public Integer getIdLice() {
      return idLice;
   }

   public void setIdLice(Integer idLice) {
      this.idLice = idLice;
   }

   public Integer getCotationLice() {
      return cotationLice;
   }

   public void setCotationLice(Integer cotationLice) {
      this.cotationLice = cotationLice;
   }

   public Integer getTypeLice() {
      return typeLice;
   }

   public void setTypeLice(Integer typeLice) {
      this.typeLice = typeLice;
   }

   public Integer getNbMale() {
      return nbMale;
   }

   public String getPremiereSaillieLice() {
      return premiereSaillieLice;
   }

   public void setPremiereSaillieLice(String  premiereSaillieLice) {
      this.premiereSaillieLice = premiereSaillieLice;
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

   public Double getProlificiteRace() {
      return prolificiteRace;
   }

   public void setProlificiteRace(Double prolificiteRace) {
      this.prolificiteRace = prolificiteRace;
   }

   public Double getProlificiteVariete() {
      return prolificiteVariete;
   }

   public void setProlificiteVariete(Double prolificiteVariete) {
      this.prolificiteVariete = prolificiteVariete;
   }

   public Integer getNbAncetreCommun() {
      return nbAncetreCommun;
   }

   public void setNbAncetreCommun(Integer nbAncetreCommun) {
      this.nbAncetreCommun = nbAncetreCommun;
   }

   public Double getConsanguinite() {
      return consanguinite;
   }

   public void setConsanguinite(Double consanguinite) {
      this.consanguinite = consanguinite;
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
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((affixeEleveur == null) ? 0 : affixeEleveur.hashCode());
      result = prime * result + ((annee == null) ? 0 : annee.hashCode());
      result = prime * result + ((codeFci == null) ? 0 : codeFci.hashCode());
      result = prime * result + ((consanguinite == null) ? 0 : consanguinite.hashCode());
      result = prime * result + ((cotationEtalon == null) ? 0 : cotationEtalon.hashCode());
      result = prime * result + ((cotationLice == null) ? 0 : cotationLice.hashCode());
      result = prime * result + ((cotationPortee == null) ? 0 : cotationPortee.hashCode());
      result = prime * result + ((idClub == null) ? 0 : idClub.hashCode());
      result = prime * result + ((idEleveur == null) ? 0 : idEleveur.hashCode());
      result = prime * result + ((idEtalon == null) ? 0 : idEtalon.hashCode());
      result = prime * result + ((idLice == null) ? 0 : idLice.hashCode());
      result = prime * result + ((idRace == null) ? 0 : idRace.hashCode());
      result = prime * result + ((idSaillie == null) ? 0 : idSaillie.hashCode());
      result = prime * result + ((idVariete == null) ? 0 : idVariete.hashCode());
      result = prime * result + ((mois == null) ? 0 : mois.hashCode());
      result = prime * result + ((nbAncetreCommun == null) ? 0 : nbAncetreCommun.hashCode());
      result = prime * result + ((nbFemelle == null) ? 0 : nbFemelle.hashCode());
      result = prime * result + ((nbMale == null) ? 0 : nbMale.hashCode());
      result = prime * result + ((nomClub == null) ? 0 : nomClub.hashCode());
      result = prime * result + ((nomEtalon == null) ? 0 : nomEtalon.hashCode());
      result = prime * result + ((nomRace == null) ? 0 : nomRace.hashCode());
      result = prime * result + ((nomVariete == null) ? 0 : nomVariete.hashCode());
      result = prime * result + ((numClub == null) ? 0 : numClub.hashCode());
      result = prime * result + ((prolificiteRace == null) ? 0 : prolificiteRace.hashCode());
      result = prime * result + ((prolificiteVariete == null) ? 0 : prolificiteVariete.hashCode());
      result = prime * result + ((typeEtalon == null) ? 0 : typeEtalon.hashCode());
      result = prime * result + ((typeLice == null) ? 0 : typeLice.hashCode());
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
      if (affixeEleveur == null) {
         if (other.affixeEleveur != null)
            return false;
      } else if (!affixeEleveur.equals(other.affixeEleveur))
         return false;
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
      if (consanguinite == null) {
         if (other.consanguinite != null)
            return false;
      } else if (!consanguinite.equals(other.consanguinite))
         return false;
      if (cotationEtalon == null) {
         if (other.cotationEtalon != null)
            return false;
      } else if (!cotationEtalon.equals(other.cotationEtalon))
         return false;
      if (cotationLice == null) {
         if (other.cotationLice != null)
            return false;
      } else if (!cotationLice.equals(other.cotationLice))
         return false;
      if (cotationPortee == null) {
         if (other.cotationPortee != null)
            return false;
      } else if (!cotationPortee.equals(other.cotationPortee))
         return false;
      if (idClub == null) {
         if (other.idClub != null)
            return false;
      } else if (!idClub.equals(other.idClub))
         return false;
      if (idEleveur == null) {
         if (other.idEleveur != null)
            return false;
      } else if (!idEleveur.equals(other.idEleveur))
         return false;
      if (idEtalon == null) {
         if (other.idEtalon != null)
            return false;
      } else if (!idEtalon.equals(other.idEtalon))
         return false;
      if (idLice == null) {
         if (other.idLice != null)
            return false;
      } else if (!idLice.equals(other.idLice))
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
      if (nbAncetreCommun == null) {
         if (other.nbAncetreCommun != null)
            return false;
      } else if (!nbAncetreCommun.equals(other.nbAncetreCommun))
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
      if (nomEtalon == null) {
         if (other.nomEtalon != null)
            return false;
      } else if (!nomEtalon.equals(other.nomEtalon))
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
      if (prolificiteRace == null) {
         if (other.prolificiteRace != null)
            return false;
      } else if (!prolificiteRace.equals(other.prolificiteRace))
         return false;
      if (prolificiteVariete == null) {
         if (other.prolificiteVariete != null)
            return false;
      } else if (!prolificiteVariete.equals(other.prolificiteVariete))
         return false;
      if (typeEtalon == null) {
         if (other.typeEtalon != null)
            return false;
      } else if (!typeEtalon.equals(other.typeEtalon))
         return false;
      if (typeLice == null) {
         if (other.typeLice != null)
            return false;
      } else if (!typeLice.equals(other.typeLice))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "BreederStatistics [idClub=" + idClub + ", nomClub=" + nomClub + ", numClub=" + numClub + ", idRace="
            + idRace + ", nomRace=" + nomRace + ", codeFci=" + codeFci + ", idVariete=" + idVariete + ", nomVariete="
            + nomVariete + ", idEleveur=" + idEleveur + ", affixeEleveur=" + affixeEleveur + ", idSaillie=" + idSaillie
            + ", cotationPortee=" + cotationPortee + ", idEtalon=" + idEtalon + ", nomEtalon=" + nomEtalon
            + ", cotationEtalon=" + cotationEtalon + ", typeEtalon=" + typeEtalon  + ", premiereSaillieEtalon=" + premiereSaillieEtalon  
            + ", idLice=" + idLice + ", cotationLice=" + cotationLice + ", typeLice=" + typeLice + ", premiereSaillieLice=" + premiereSaillieLice + ", nbMale=" + nbMale + ", nbFemelle="
            + nbFemelle + ", prolificiteRace=" + prolificiteRace + ", prolificiteVariete=" + prolificiteVariete
            + ", nbAncetreCommun=" + nbAncetreCommun + ", consanguinite=" + consanguinite + ", annee=" + annee
            + ", mois=" + mois + "]";
   }

}
