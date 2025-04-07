package com.example.bordeauxintersections.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChantierResponse {
    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("results")
    private List<Chantier> results;

    public int getTotalCount() { return totalCount; }
    public List<Chantier> getResults() { return results; }

    public static class Chantier {
        @SerializedName("gid")
        private int gid;

        @SerializedName("ident")
        private String ident;

        @SerializedName("localisation")
        private String localisation;

        @SerializedName("date_debut")
        private String dateDebut;

        @SerializedName("date_fin")
        private String dateFin;

        @SerializedName("libelle")
        private String libelle;

        @SerializedName("geo_point_2d")
        private GeoPoint geoPoint;

        public static class GeoPoint {
            @SerializedName("lon")
            private double longitude;

            @SerializedName("lat")
            private double latitude;

            public double getLongitude() { return longitude; }
            public double getLatitude() { return latitude; }
        }

        public int getGid() { return gid; }
        public String getIdent() { return ident; }
        public String getLocalisation() { return localisation; }
        public String getDateDebut() { return dateDebut; }
        public String getDateFin() { return dateFin; }
        public String getLibelle() { return libelle; }
        public GeoPoint getGeoPoint() { return geoPoint; }
    }
}