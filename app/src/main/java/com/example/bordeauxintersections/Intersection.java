package com.example.bordeauxintersections;

import java.util.Date;

public class Intersection {
    private long id;
    private String title;
    private String description;
    private String status;
    private double latitude;
    private double longitude;
    private float distanceFromUser;
    private Date startDate;
    private Date endDate;
    private Date lastUpdate;
    private String dataSource; // "api", "local", ou "json"
    private String type;

    // Constructeur par défaut nécessaire pour SQLite
    public Intersection() {
    }

    // Constructeur existant
    public Intersection(String title, String description, String status, double latitude, double longitude) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdate = new Date();
    }

    // Constructeur complet
    public Intersection(long id, String title, String description, String status,
                        double latitude, double longitude, Date startDate,
                        Date endDate, String dataSource) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dataSource = dataSource;
        this.lastUpdate = new Date();
    }

    // Getters existants
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getDistanceFromUser() { return distanceFromUser; }

    // Nouveaux getters
    public long getId() { return id; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public Date getLastUpdate() { return lastUpdate; }
    public String getDataSource() { return dataSource; }

    // Setters existants avec retour chaîné
    public void setDistanceFromUser(float distance) {
        this.distanceFromUser = distance;
    }

    // Nouveaux setters
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(String status) { this.status = status; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public void setLastUpdate(Date lastUpdate) { this.lastUpdate = lastUpdate; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    // Méthode utilitaire pour la distance
    public String getFormattedDistance() {
        if (distanceFromUser < 1000) {
            return String.format("%.0f m", distanceFromUser);
        } else {
            return String.format("%.1f km", distanceFromUser / 1000);
        }
    }

    // Méthode utilitaire pour obtenir l'icône en fonction de la source
    public int getSourceIcon() {
        switch (dataSource) {
            case "api":
                return R.drawable.ic_cloud; // À créer
            case "local":
                return R.drawable.ic_database; // À créer
            case "json":
                return R.drawable.ic_file; // À créer
            default:
                return R.drawable.ic_unknown; // À créer
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;

    }
}