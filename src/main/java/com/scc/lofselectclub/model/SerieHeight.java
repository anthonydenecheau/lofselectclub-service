package com.scc.lofselectclub.model;


public class SerieHeight {

   public SerieHeight(int idVariety, int height) {
      super();
      this.idVariety = idVariety;
      this.height = height;
   }

   int idVariety;
   int height;

   public int getIdVariety() {
      return idVariety;
   }

   public void setIdVariety(int idVariety) {
      this.idVariety = idVariety;
   }


   public int getHeight() {
      return height;
   }

   public void setHeight(int height) {
      this.height = height;
   }
}
