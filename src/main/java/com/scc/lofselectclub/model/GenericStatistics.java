package com.scc.lofselectclub.model;

public class GenericStatistics {

   protected Integer annee;;
   protected Integer idVariete;
   protected String nomVariete;
   protected Integer idVarieteEtalon;
   protected String nomVarieteEtalon;
   
   public Integer getAnnee() {
      return annee;
   }
   public Integer getIdVariete() {
      return idVariete;
   }
   public String getNomVariete() {
      return nomVariete;
   }
   public Integer getIdVarieteEtalon() {
      return idVarieteEtalon;
   }
   public void setIdVarieteEtalon(Integer idVarieteEtalon) {
      this.idVarieteEtalon = idVarieteEtalon;
   }
   public String getNomVarieteEtalon() {
      return nomVarieteEtalon;
   }
   public void setNomVarieteEtalon(String nomVarieteEtalon) {
      this.nomVarieteEtalon = nomVarieteEtalon;
   }

}
