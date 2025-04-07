package com.example.bordeauxintersections;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

public class AlertsFragment extends Fragment {

    private SharedPreferences preferences;
    private SwitchMaterial alertsSwitch;
    private Slider distanceSlider;
    private TextView distanceText;
    private CheckBox checkBoxEnCours;
    private CheckBox checkBoxPlanifie;
    private CheckBox checkBoxTermine;
    private RecyclerView alertsHistoryRecyclerView;
    private final List<AlertHistoryEntry> alertHistory = new ArrayList<>();
    private AlertHistoryAdapter historyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        initializeViews(view);

        // Ajout des entrées test
        addTestEntries();

//        // Modifier la configuration du slider
//        distanceSlider.setValueFrom(0.5f);
//        distanceSlider.setValueTo(5f);
//        distanceSlider.setStepSize(0.5f);
//        distanceSlider.setValue(1f);  // Valeur par défaut : 1 km

        // Initialiser l'adaptateur avec la liste
        historyAdapter = new AlertHistoryAdapter(alertHistory);
        alertsHistoryRecyclerView.setAdapter(historyAdapter);

        loadSavedPreferences();
        logPreferences();
        setupListeners();
    }

    private void logPreferences() {
        String prefsState = "État des préférences :\n" +
                "Alertes activées : " + preferences.getBoolean("alerts_enabled", false) + "\n" +
                "Distance d'alerte : " + preferences.getFloat("alert_distance", 100f) + "\n" +
                "En cours activé : " + preferences.getBoolean("en_cours_enabled", true) + "\n" +
                "Planifié activé : " + preferences.getBoolean("planifie_enabled", true) + "\n" +
                "Terminé activé : " + preferences.getBoolean("termine_enabled", false);

        Log.d("AlertsFragment", prefsState);
        // Aussi afficher dans un Toast pour voir directement sur l'émulateur
        Toast.makeText(getContext(), prefsState, Toast.LENGTH_LONG).show();
    }

    private void addTestEntries() {
        addAlertToHistory("Rue Sainte-Catherine", 0.5);
        addAlertToHistory("Place de la Bourse", 1.2);
        addAlertToHistory("Place des Quinconces", 0.8);
        addAlertToHistory("Cours de l'Intendance", 1.5);
    }

    private void initializeViews(View view) {
        alertsSwitch = view.findViewById(R.id.switch_alerts_active);
        distanceSlider = view.findViewById(R.id.slider_distance);
        distanceText = view.findViewById(R.id.text_distance);
        checkBoxEnCours = view.findViewById(R.id.checkbox_en_cours);
        checkBoxPlanifie = view.findViewById(R.id.checkbox_planifie);
        checkBoxTermine = view.findViewById(R.id.checkbox_termine);
        alertsHistoryRecyclerView = view.findViewById(R.id.recycler_alerts_history);

        alertsHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadSavedPreferences() {
        boolean alertsEnabled = preferences.getBoolean("alerts_enabled", false);
        float alertDistance = preferences.getFloat("alert_distance", 100f);
        boolean enCoursEnabled = preferences.getBoolean("en_cours_enabled", true);
        boolean planifieEnabled = preferences.getBoolean("planifie_enabled", true);
        boolean termineEnabled = preferences.getBoolean("termine_enabled", false);

        alertsSwitch.setChecked(alertsEnabled);
        distanceSlider.setValue(alertDistance);
        checkBoxEnCours.setChecked(enCoursEnabled);
        checkBoxPlanifie.setChecked(planifieEnabled);
        checkBoxTermine.setChecked(termineEnabled);

        updateDistanceText(alertDistance);
    }

    private void setupListeners() {
        alertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("alerts_enabled", isChecked).apply();
        });

        distanceSlider.addOnChangeListener((slider, value, fromUser) -> {
            updateDistanceText(value);
            preferences.edit().putFloat("alert_distance", value).apply();
            logPreferences();
        });

        checkBoxEnCours.setOnCheckedChangeListener((buttonView, isChecked) ->{
                preferences.edit().putBoolean("en_cours_enabled", isChecked).apply();
                logPreferences();
        });
        checkBoxPlanifie.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean("planifie_enabled", isChecked).apply());

        checkBoxTermine.setOnCheckedChangeListener((buttonView, isChecked) ->
                preferences.edit().putBoolean("termine_enabled", isChecked).apply());
    }

    private void updateDistanceText(float distance) {
        distanceText.setText(String.format("Distance : %.0fm", distance));
    }

    public void addAlertToHistory(String intersectionName, double distance) {
        AlertHistoryEntry alert = new AlertHistoryEntry(intersectionName, distance);
        alertHistory.add(alert);
    }
}
