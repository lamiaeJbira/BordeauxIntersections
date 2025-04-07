package com.example.bordeauxintersections;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bordeauxintersections.api.ApiClient;
import com.example.bordeauxintersections.api.ChantierResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import android.widget.Toast;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private static final LatLng BORDEAUX = new LatLng(44.837789, -0.57918);
    private static final int LIMIT = 100;
    private int currentOffset = 0;
    private boolean isLoading = false;
    private int totalMarkers = 0;
    private Map<String, Marker> markers = new HashMap<>();
    private String selectedLocalisation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedLocalisation = getArguments().getString("selectedLocalisation");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    private void getCurrentLocation() {
        if (getContext() == null) return;

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            showToast("Position trouvée : " + location.getLatitude() + ", " + location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                        } else {
                            showToast("Position non disponible. Vérifiez que votre GPS est activé.");
                        }
                    })
                    .addOnFailureListener(e -> showToast("Erreur de localisation : " + e.getMessage()));
        } catch (SecurityException e) {
            showToast("Permission de localisation nécessaire");
        }
    }

    private void checkLocationPermission() {
        if (getContext() != null &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        // Initialiser le client de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
        loadAllMarkers();
    }

    private void setupMap() {
        if (mMap == null) return;

        try {
            // Configuration basique de la carte
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

            // Ajouter ces deux lignes pour activer la localisation
            mMap.setMyLocationEnabled(true);  // Affiche le point bleu
            mMap.getUiSettings().setMyLocationButtonEnabled(true);  // Affiche le bouton

            // Temporairement centrer sur Bordeaux pendant qu'on récupère la vraie position
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BORDEAUX, 13f));

            // Demander la permission et obtenir la position
            checkLocationPermission();
        } catch (SecurityException e) {
            showToast("Permission de localisation nécessaire");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée, on réessaie d'activer la localisation
                try {
                    mMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            } else {
                showToast("Permission de localisation refusée");
            }
        }
    }


    public void highlightMarkers(List<String> localisations) {
        // Reset all markers to red
        for (Marker marker : markers.values()) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

        // Highlight matching markers in blue
        for (String localisation : localisations) {
            for (Map.Entry<String, Marker> entry : markers.entrySet()) {
                if (entry.getKey().toLowerCase().contains(localisation.toLowerCase())) {
                    entry.getValue().setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }
            }
        }
    }

    private void loadAllMarkers() {
        if (isLoading) return;
        isLoading = true;
        loadMarkersPage();
    }

    private void loadMarkersPage() {
        ApiClient.getClient().getChantiers(LIMIT, currentOffset).enqueue(new Callback<ChantierResponse>() {
            @Override
            public void onResponse(Call<ChantierResponse> call, Response<ChantierResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChantierResponse.Chantier> chantiers = response.body().getResults();
                    addMarkersToMap(chantiers);

                    totalMarkers += chantiers.size();

                    if (chantiers.size() == LIMIT && totalMarkers < response.body().getTotalCount()) {
                        currentOffset += LIMIT;
                        loadMarkersPage();
                    } else {
                        isLoading = false;
                        showToast(totalMarkers + " marqueurs chargés");

                        // When loading is complete, highlight selected marker if any
                        if (selectedLocalisation != null) {
                            highlightMarkers(List.of(selectedLocalisation));
                            focusOnLocalisation(selectedLocalisation);
                        }
                    }
                } else {
                    isLoading = false;
                    showToast("Erreur de chargement des marqueurs");
                }
            }

            @Override
            public void onFailure(Call<ChantierResponse> call, Throwable t) {
                isLoading = false;
                showToast("Erreur réseau: " + t.getMessage());
            }
        });
    }

    private void addMarkersToMap(List<ChantierResponse.Chantier> chantiers) {
        if (mMap == null) return;

        for (ChantierResponse.Chantier chantier : chantiers) {
            if (chantier.getGeoPoint() != null) {
                LatLng position = new LatLng(
                        chantier.getGeoPoint().getLatitude(),
                        chantier.getGeoPoint().getLongitude()
                );

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(chantier.getLocalisation())
                        .snippet(chantier.getLibelle())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                if (marker != null) {
                    markers.put(chantier.getLocalisation(), marker);
                }
            }
        }
    }

    public void focusOnLocalisation(String localisation) {
        Marker marker = markers.get(localisation);
        if (marker != null) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            marker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15f));
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void focusOnLocation(double latitude, double longitude) {
        if (mMap != null) {
            LatLng location = new LatLng(latitude, longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        }
    }
}