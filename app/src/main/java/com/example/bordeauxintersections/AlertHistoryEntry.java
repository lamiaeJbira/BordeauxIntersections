package com.example.bordeauxintersections;

public class AlertHistoryEntry {
    private final String intersectionName;
    private final double distance;
    private final long timestamp;

    public AlertHistoryEntry(String intersectionName, double distance) {
        this.intersectionName = intersectionName;
        this.distance = distance;
        this.timestamp = System.currentTimeMillis();
    }

    public AlertHistoryEntry(String intersectionName, double distance, long timestamp) {
        this.intersectionName = intersectionName;
        this.distance = distance;
        this.timestamp = timestamp;
    }

    public String getIntersectionName() {
        return intersectionName;
    }

    public double getDistance() {
        return distance;
    }

    public long getTimestamp() {
        return timestamp;
    }
}