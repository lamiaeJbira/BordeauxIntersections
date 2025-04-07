package com.example.bordeauxintersections;


import android.os.Build;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private IntersectionAlertView alertView;
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1);
            }
        }

        // Vérifier et demander les permissions nécessaires
        checkAndRequestPermissions();

        // Configuration de la toolbar

        setupToolbar();
       // Initialiser le client de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configuration de la navigation
        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new MapFragment());
        }
        startLocationService();
    }

    private void startLocationService() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            startService(serviceIntent);
            Toast.makeText(this, "Service de localisation démarré", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Service de localisation démarré");
        } else {
            Toast.makeText(this, "Permission de localisation non accordée", Toast.LENGTH_LONG).show();
            Log.e("MainActivity", "Permission de localisation non accordée");
        }
    }

    private void checkNearbyIntersections() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,
                    "Permission de localisation nécessaire",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof ListFragment) {
            ListFragment listFragment = (ListFragment) currentFragment;
            List<Intersection> intersections = listFragment.getIntersections();

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    checkIntersectionsDistance(location, intersections);
                }
            });
        }
    }

    private void checkIntersectionsDistance(Location location, List<Intersection> intersections) {
        for (Intersection intersection : intersections) {
            float[] results = new float[1];
            Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    intersection.getLatitude(), intersection.getLongitude(),
                    results
            );

            // Si l'intersection est à moins de 1000 mètres
            if (results[0] <= 1000) {
                intersection.setDistanceFromUser(results[0]);
                showAlert(intersection);
                break;
            }
        }
    }

    private void showAlert(Intersection intersection) {
        if (alertView != null) {
            alertView.show(intersection);
        }
    }





    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment fragment = null;

            if (itemId == R.id.navigation_map) {
                fragment = new MapFragment();
            } else if (itemId == R.id.navigation_list) {
                fragment = new ListFragment();
            } else if (itemId == R.id.navigation_alerts) {
                fragment = new AlertsFragment();  // Maintenant on utilise AlertsFragment
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // Recharger le fragment actif
                Fragment currentFragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof MapFragment) {
                    loadFragment(new MapFragment());
                }
            } else {
                Toast.makeText(this,
                        "Les permissions de localisation sont nécessaires pour certaines fonctionnalités",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Gérer le bouton retour
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}