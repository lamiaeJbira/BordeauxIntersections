package com.example.bordeauxintersections;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import com.example.bordeauxintersections.Intersection; // pour votre modèle Intersection
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "intersections.db";
    private static final int DATABASE_VERSION = 2;

    // Table intersections
    public static final String TABLE_INTERSECTIONS = "intersections";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TYPE = "type"; // Type d'incident (travaux, accident, etc.)
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_LAST_UPDATE = "last_update";
    public static final String COLUMN_DATA_SOURCE = "data_source"; // API, JSON, ou manuel

    // Table statistiques (pour le mode admin)
    public static final String TABLE_STATS = "statistics";
    public static final String COLUMN_STATS_ID = "_id";
    public static final String COLUMN_INTERSECTION_ID = "intersection_id";
    public static final String COLUMN_INCIDENT_COUNT = "incident_count";
    public static final String COLUMN_LAST_INCIDENT_DATE = "last_incident_date";

    // Création de la table intersections
    private static final String CREATE_TABLE_INTERSECTIONS = "CREATE TABLE "
            + TABLE_INTERSECTIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TITLE + " TEXT NOT NULL, "
            + COLUMN_DESCRIPTION + " TEXT NOT NULL, "
            + COLUMN_STATUS + " TEXT NOT NULL, "
            + COLUMN_LATITUDE + " DOUBLE NOT NULL, "
            + COLUMN_LONGITUDE + " DOUBLE NOT NULL, "
            + COLUMN_TYPE + " TEXT DEFAULT 'unknown', "  // Changé en optionnel avec valeur par défaut
            + COLUMN_START_DATE + " DATETIME, "
            + COLUMN_END_DATE + " DATETIME, "
            + COLUMN_LAST_UPDATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + COLUMN_DATA_SOURCE + " TEXT DEFAULT 'unknown');";  // Changé en optionnel avec valeur par défaut

    // Création de la table statistiques
    private static final String CREATE_TABLE_STATS = "CREATE TABLE "
            + TABLE_STATS + "("
            + COLUMN_STATS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_INTERSECTION_ID + " INTEGER NOT NULL, "
            + COLUMN_INCIDENT_COUNT + " INTEGER DEFAULT 0, "
            + COLUMN_LAST_INCIDENT_DATE + " DATETIME, "
            + "FOREIGN KEY(" + COLUMN_INTERSECTION_ID + ") REFERENCES "
            + TABLE_INTERSECTIONS + "(" + COLUMN_ID + "));";



    private static final String CREATE_TABLE_ALERT_HISTORY =
            "CREATE TABLE alert_history ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "intersection_id INTEGER, "
                    + "distance FLOAT, "
                    + "alert_time INTEGER DEFAULT (strftime('%s', 'now')), "
                    + "FOREIGN KEY(intersection_id) REFERENCES " + TABLE_INTERSECTIONS + "(_id))";


    // Index pour les recherches géographiques
    private static final String CREATE_LOCATION_INDEX =
            "CREATE INDEX IF NOT EXISTS idx_location ON "
                    + TABLE_INTERSECTIONS + "(" + COLUMN_LATITUDE + ", " + COLUMN_LONGITUDE + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void ensureAlertHistoryTableExists() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='alert_history'",
                null
        );
        boolean tableExists = cursor.getCount() > 0;
        cursor.close();

        if (!tableExists) {
            db.execSQL(CREATE_TABLE_ALERT_HISTORY);
            Log.d("DatabaseHelper", "Table alert_history créée");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Création des tables de la base de données");
        try {
            db.execSQL(CREATE_TABLE_ALERT_HISTORY);

            db.execSQL(CREATE_TABLE_INTERSECTIONS);
            db.execSQL(CREATE_TABLE_STATS);
            db.execSQL(CREATE_LOCATION_INDEX);
            Log.d("DatabaseHelper", "Tables créées avec succès");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Erreur lors de la création des tables: " + e.getMessage());
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTERSECTIONS);
        onCreate(db);
    }


    public List<Intersection> getAllIntersections() {
        List<Intersection> intersections = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_DESCRIPTION,
                COLUMN_STATUS,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_TYPE,
                COLUMN_START_DATE,
                COLUMN_END_DATE,
                COLUMN_DATA_SOURCE
        };

        Cursor cursor = db.query(
                TABLE_INTERSECTIONS,
                columns,
                null,
                null,
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                do {
                    Intersection intersection = new Intersection(
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
                    );

                    // Définir les autres propriétés
                    intersection.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    intersection.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                    intersection.setDataSource(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA_SOURCE)));

                    intersections.add(intersection);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }

        return intersections;
    }
}