package com.example.bordeauxintersections;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private SharedPreferences preferences;
    private DatabaseHelper dbHelper;
    private static final String TAG = "LocationService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate appelé");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        dbHelper = new DatabaseHelper(this);
        setupLocationCallback();
    }


    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    checkNearbyIntersections(location);
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000);  // 5 secondes pour le test

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("LocationService", "Démarrage des mises à jour de position");
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            Log.e("LocationService", "Permission manquante");
        }
    }


    private void checkNearbyIntersections(Location userLocation) {
        Log.d("LocationService", "=== Début vérification intersections ===");
        Log.d("LocationService", "Position actuelle: " + userLocation.getLatitude() + ", " + userLocation.getLongitude());

        // Vérifier si les alertes sont activées
        boolean alertsEnabled = preferences.getBoolean("alerts_enabled", false);
        Log.d("LocationService", "Alertes activées: " + alertsEnabled);

        if (!alertsEnabled) {
            Log.d("LocationService", "Alertes désactivées, arrêt de la vérification");
            return;
        }

        float alertDistance = preferences.getFloat("alert_distance", 100f);
        Log.d("LocationService", "Distance d'alerte: " + alertDistance + " mètres");

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            // Un seul accès à la base pour récupérer les intersections
            Cursor cursor = db.query(DatabaseHelper.TABLE_INTERSECTIONS, null, null, null, null, null, null);
            int count = cursor.getCount();
            Log.d("LocationService", "Nombre d'intersections trouvées: " + count);

            while (cursor.moveToNext()) {
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE));

                float[] results = new float[1];
                Location.distanceBetween(
                        userLocation.getLatitude(), userLocation.getLongitude(),
                        latitude, longitude,
                        results
                );

                if (results[0] <= alertDistance) {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
                    long intersectionId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    Log.d("LocationService", "Distance avec " + title + ": " + results[0] + " mètres");

                    // Vérifier si une alerte a déjà été émise récemment
                    boolean shouldAlert = true;
                    Cursor alertCursor = db.query(
                            "alert_history",
                            new String[]{"alert_time"},
                            "intersection_id = ?",
                            new String[]{String.valueOf(intersectionId)},
                            null, null, "alert_time DESC", "1"
                    );

                    if (alertCursor.moveToFirst()) {
                        long lastAlertTime = alertCursor.getLong(0);
                        long currentTime = System.currentTimeMillis();
                        shouldAlert = (currentTime - lastAlertTime) > 3600000; // 1 heure
                    }
                    alertCursor.close();

                    if (shouldAlert) {
                        // Créer l'objet Intersection pour la notification
                        Intersection intersection = new Intersection();
                        intersection.setId(intersectionId);
                        intersection.setTitle(title);
                        intersection.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
                        intersection.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)));
                        intersection.setLatitude(latitude);
                        intersection.setLongitude(longitude);

                        // Sauvegarder l'alerte
                        ContentValues values = new ContentValues();
                        values.put("intersection_id", intersectionId);
                        values.put("distance", results[0]);
                        values.put("alert_time", System.currentTimeMillis());
                        db.insert("alert_history", null, values);

                        Log.d("LocationService", "INTERSECTION PROCHE TROUVÉE ! Affichage notification...");
                        showNotification(intersection, results[0]);
                    }
                    break;
                }
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            Log.e("LocationService", "Erreur lors de la vérification des intersections", e);
        }

        Log.d("LocationService", "=== Fin vérification intersections ===");
    }

    private boolean shouldCheckIntersection(Intersection intersection,
                                            boolean enCours,
                                            boolean planifie,
                                            boolean termine) {
        switch (intersection.getStatus().toLowerCase()) {
            case "en cours": return enCours;
            case "planifié": return planifie;
            case "terminé": return termine;
            default: return false;
        }
    }

    private void showNotification(Intersection intersection, float distance) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permission de notification manquante");
            return;
        }

        String CHANNEL_ID = "intersection_alerts";

        // Configuration détaillée du canal de notification
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Alertes intersection",
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Alertes de proximité des intersections");
        channel.enableLights(true);
        channel.setLightColor(getColor(R.color.red)); // Assurez-vous d'avoir défini cette couleur
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
        channel.setShowBadge(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        // Construction de la notification avec plus de détails
        @SuppressLint("DefaultLocale") Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle("⚠️ Intersection proche")
                .setContentText(String.format("%s à %.0f mètres",
                        intersection.getTitle(), distance))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(String.format("%s à %.0f mètres\n%s",
                                intersection.getTitle(),
                                distance,
                                intersection.getDescription())))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setColor(getColor(R.color.red))
                .setColorized(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        notificationManager.notify(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}