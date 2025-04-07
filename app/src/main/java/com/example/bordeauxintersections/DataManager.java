package com.example.bordeauxintersections;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DataManager {
    private static final String TAG = "DataManager";
    private final Context context;
    private final IntersectionDAO dao;
    private final NetworkService networkService;
    private static DataManager instance;

    private DataManager(Context context) {
        this.context = context.getApplicationContext();
        this.dao = new IntersectionDAO(context);
        this.networkService = new NetworkService();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context.getApplicationContext());
        }
        return instance;
    }

    public List<Intersection> loadIntersections() throws Exception {
        Log.d(TAG, "=== Début loadIntersections ===");
        List<Intersection> allIntersections = new ArrayList<>();

        // 1. Essayer d'abord l'API si le réseau est disponible
        if (isNetworkAvailable()) {
            try {
                Log.d(TAG, "Tentative de chargement depuis l'API");
                // Utiliser un ExecutorService pour l'appel réseau
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<List<Intersection>> future = executor.submit(() -> networkService.getIntersections());

                try {
                    List<Intersection> apiData = future.get(30, TimeUnit.SECONDS);
                    Log.d(TAG, "Données reçues de l'API: " + apiData.size() + " intersections");

                    if (!apiData.isEmpty()) {
                        for (Intersection intersection : apiData) {
                            intersection.setDataSource("api");
                        }
                        allIntersections.addAll(apiData);
                        saveIntersectionsToDatabase(apiData);
                        return apiData;
                    }
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    Log.e(TAG, "Erreur lors de l'appel API: " + e.getMessage());
                } finally {
                    executor.shutdown();
                }
            } catch (Exception e) {
                Log.e(TAG, "Erreur lors du chargement depuis l'API: " + e.getMessage());
            }
        }

        // 2. Si l'API échoue, charger depuis la base de données locale
        dao.open();
        List<Intersection> localData = dao.getAllIntersections();
        dao.close();

        if (!localData.isEmpty()) {
            Log.d(TAG, "Données trouvées en local: " + localData.size() + " intersections");
            return localData;
        }

        // 3. En dernier recours, charger depuis le JSON local
        List<Intersection> jsonData = loadFromLocalJSON();
        if (!jsonData.isEmpty()) {
            Log.d(TAG, "Données JSON chargées: " + jsonData.size() + " intersections");
            saveIntersectionsToDatabase(jsonData);
            return jsonData;
        }

        return allIntersections;
    }

    public void saveIntersectionsToDatabase(List<Intersection> intersections) {
        Log.d(TAG, "Début de la sauvegarde en base de données");
        dao.open();
        try {
            dao.clearAllIntersections();
            Log.d(TAG, "Table vidée avant insertion");

            // Puis insérer les nouvelles données
            int successCount = 0;
            for (Intersection intersection : intersections) {

                intersection.setLastUpdate(new Date());
                long id = dao.createIntersection(intersection);
                if (id != -1) successCount++;
            }
//            Log.d(TAG, "Nombre d'intersections sauvegardées avec succès: " + successCount + "/" + intersections.size());
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de la sauvegarde en base: " + e.getMessage());
        } finally {
            dao.close();
        }
    }

    private List<Intersection> loadFromLocalJSON() {
        List<Intersection> intersections = new ArrayList<>();
        try {
            Log.d(TAG, "Ouverture du fichier JSON");
            InputStream is = context.getAssets().open("intersection.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }

            JSONObject jsonObject = new JSONObject(jsonString.toString());
            JSONArray results = jsonObject.getJSONArray("results");
            Log.d(TAG, "Nombre d'items trouvés dans le JSON: " + results.length());

            for (int i = 0; i < results.length(); i++) {
                JSONObject obj = results.getJSONObject(i);
                JSONObject geoPoint = obj.getJSONObject("geoPoint");

                Intersection intersection = new Intersection(
                        obj.getString("title"),
                        obj.getString("description"),
                        obj.getString("status"),
                        geoPoint.getDouble("latitude"),
                        geoPoint.getDouble("longitude")
                );
                intersection.setDataSource("json");
                intersections.add(intersection);
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du chargement du JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return intersections;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isAvailable = activeNetwork != null && activeNetwork.isConnected();
        Log.d(TAG, "Réseau disponible: " + isAvailable);
        return isAvailable;
    }
}