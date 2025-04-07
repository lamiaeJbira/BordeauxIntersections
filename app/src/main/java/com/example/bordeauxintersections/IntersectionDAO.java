package com.example.bordeauxintersections;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class IntersectionDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] allColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_TITLE,
            DatabaseHelper.COLUMN_DESCRIPTION,
            DatabaseHelper.COLUMN_STATUS,
            DatabaseHelper.COLUMN_LATITUDE,
            DatabaseHelper.COLUMN_LONGITUDE
    };

    public IntersectionDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean isDatabasePopulated() {
        Cursor cursor = database.query(DatabaseHelper.TABLE_INTERSECTIONS,
                new String[]{DatabaseHelper.COLUMN_ID}, // Vérifie uniquement l'ID
                null, null, null, null, null);

        boolean hasData = cursor.getCount() > 0;
        cursor.close();
        return hasData;
    }


    public long createIntersection(Intersection intersection) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, intersection.getTitle());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, intersection.getDescription());
        values.put(DatabaseHelper.COLUMN_STATUS, intersection.getStatus());
        values.put(DatabaseHelper.COLUMN_LATITUDE, intersection.getLatitude());
        values.put(DatabaseHelper.COLUMN_LONGITUDE, intersection.getLongitude());
        values.put(DatabaseHelper.COLUMN_DATA_SOURCE, intersection.getDataSource());
        values.put(DatabaseHelper.COLUMN_TYPE, intersection.getType() != null ? intersection.getType() : "inconnue");

        long insertId = database.insert(DatabaseHelper.TABLE_INTERSECTIONS, null, values);

        if (insertId == -1) {
            Log.e("IntersectionDAO", "Échec de l'insertion");
        } else {
            Log.d("IntersectionDAO", "Insertion réussie avec id: " + insertId);
        }

        return insertId;
    }

    // Supprimer une intersection
    public void deleteIntersection(Intersection intersection) {
        long id = intersection.getId();
        database.delete(DatabaseHelper.TABLE_INTERSECTIONS,
                DatabaseHelper.COLUMN_ID + " = " + id, null);
    }

    // Mettre à jour une intersection
    public boolean updateIntersection(Intersection intersection) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, intersection.getTitle());
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, intersection.getDescription());
        values.put(DatabaseHelper.COLUMN_STATUS, intersection.getStatus());
        values.put(DatabaseHelper.COLUMN_LATITUDE, intersection.getLatitude());
        values.put(DatabaseHelper.COLUMN_LONGITUDE, intersection.getLongitude());

        return database.update(DatabaseHelper.TABLE_INTERSECTIONS, values,
                DatabaseHelper.COLUMN_ID + " = " + intersection.getId(), null) > 0;
    }

    // Récupérer une intersection par son ID
    public Intersection getIntersectionById(long id) {
        Cursor cursor = database.query(DatabaseHelper.TABLE_INTERSECTIONS,
                allColumns, DatabaseHelper.COLUMN_ID + " = " + id,
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Intersection intersection = cursorToIntersection(cursor);
            cursor.close();
            return intersection;
        }
        return null;
    }

    // Récupérer les intersections par statut
    public List<Intersection> getIntersectionsByStatus(String status) {
        List<Intersection> intersections = new ArrayList<>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_INTERSECTIONS,
                allColumns, DatabaseHelper.COLUMN_STATUS + " = ?",
                new String[] { status }, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Intersection intersection = cursorToIntersection(cursor);
            intersections.add(intersection);
            cursor.moveToNext();
        }
        cursor.close();
        return intersections;
    }

    public void clearAllIntersections() {
        int count = database.delete(DatabaseHelper.TABLE_INTERSECTIONS, null, null);
        Log.d("IntersectionDAO", "Nombre d'intersections supprimées: " + count);
    }

    // Méthode helper pour convertir un curseur en objet Intersection
    private Intersection cursorToIntersection(Cursor cursor) {
        Intersection intersection = new Intersection();

        intersection.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        intersection.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE)));
        intersection.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIPTION)));
        intersection.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)));
        intersection.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE)));
        intersection.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE)));

        return intersection;
    }

    public List<Intersection> getAllIntersections() {
        List<Intersection> intersections = new ArrayList<>();

        // Requête pour récupérer toutes les intersections
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_INTERSECTIONS, // table
                null,  // toutes les colonnes
                null,  // pas de clause WHERE
                null,  // pas d'arguments WHERE
                null,  // pas de GROUP BY
                null,  // pas de HAVING
                null   // pas d'ORDER BY
        );

        try {
            // Parcourir le curseur et créer les objets Intersection
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Intersection intersection = cursorToIntersection(cursor);
                intersections.add(intersection);
                cursor.moveToNext();
            }
        } finally {
            // Toujours fermer le curseur
            cursor.close();
        }

        return intersections;
    }

}