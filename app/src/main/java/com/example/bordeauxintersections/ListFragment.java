package com.example.bordeauxintersections;
import com.google.android.material.button.MaterialButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.example.bordeauxintersections.IntersectionAdapter;
import com.example.bordeauxintersections.api.ApiClient;
import com.example.bordeauxintersections.api.ChantierResponse;
import com.example.bordeauxintersections.Intersection;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListFragment extends Fragment implements IntersectionAdapter.OnIntersectionClickListener {


    private MaterialButton sortDistanceButton;
    private MaterialButton sortTypeButton;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView recyclerView;
    private ProgressBar loadingIndicator;
    private IntersectionAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private TextInputEditText searchEditText;
    private ChipGroup statusFilterGroup;
    private List<Intersection> allIntersections = new ArrayList<>();
    private int currentPage = 0;
    private static final int PAGE_SIZE = 100; // Augmenté pour charger plus de données
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private MapFragment mapFragment;
    private DataManager dataManager;
    private boolean showDistances = false;  // En haut de votre classe ListFragment
    private IntersectionAlertView alertView;
    private boolean isInAlertMode = false;
    private List<Intersection> lastSortedList = null;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser le DataManager
        dataManager = DataManager.getInstance(requireContext());

        alertView = view.findViewById(R.id.alert_view);

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupStatusFilter();
        setupSortButtons();

        //loadIntersectionsFromApi par loadIntersections
        loadIntersections(true);
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.intersections_recycler_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        searchEditText = view.findViewById(R.id.search_edit_text);
        statusFilterGroup = view.findViewById(R.id.status_filter_group);
//        setupTestAlert(view);

        // Ajout des boutons de tri
        sortDistanceButton = view.findViewById(R.id.sort_distance_button);
        sortTypeButton = view.findViewById(R.id.sort_type_button);

        // Initialisation du client de localisation
        if (getActivity() != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        }
    }

//    private void setupTestAlert(View view) {
//        MaterialButton testButton = view.findViewById(R.id.test_alert);
//        testButton.setOnClickListener(v -> {
//            if (!isInAlertMode) {
//                // Sauvegarder l'état actuel
//                lastSortedList = new ArrayList<>(adapter.getIntersections());
//                // Simuler une alerte
//                isInAlertMode = true;
//                testButton.setText("Retour à la liste");
//                if (!allIntersections.isEmpty()) {
//                    Intersection testIntersection = allIntersections.get(0);
//                    testIntersection.setDistanceFromUser(0);
//                    showAlert(testIntersection);
//                }
//            } else {
//                // Restaurer l'état précédent
//                isInAlertMode = false;
//                testButton.setText("Tester Alerte");
//                if (lastSortedList != null) {
//                    adapter.updateData(lastSortedList,showDistances);
//                }
//            }
//        });
//    }



    private void setupSortButtons() {
        sortDistanceButton.setOnClickListener(v -> {
            v.animate().alpha(0.5f).setDuration(100).withEndAction(() -> {
                v.animate().alpha(1f).setDuration(100);
                sortByDistance();
            }).start();
        });

        sortTypeButton.setOnClickListener(v -> {
            v.animate().alpha(0.5f).setDuration(100).withEndAction(() -> {
                v.animate().alpha(1f).setDuration(100);
                sortByType();
            }).start();
        });
    }

    private void sortByDistance() {
        sortDistanceButton.setSelected(true);
        sortTypeButton.setSelected(false);
        showDistances = true;

        if (getContext() == null) return;

        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(),
                    "Permission de localisation nécessaire pour trier par distance",
                    Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                for (Intersection intersection : allIntersections) {
                    float[] results = new float[1];
                    Location.distanceBetween(
                            location.getLatitude(), location.getLongitude(),
                            intersection.getLatitude(), intersection.getLongitude(),
                            results
                    );
                    intersection.setDistanceFromUser(results[0]);
                }

                List<Intersection> sortedList = new ArrayList<>(allIntersections);
                sortedList.sort((i1, i2) ->
                        Float.compare(i1.getDistanceFromUser(), i2.getDistanceFromUser()));

                adapter.updateData(sortedList, true); // Nouveau paramètre pour afficher la distance
            }
        });
    }

    private void sortByType() {
        List<Intersection> sortedList = new ArrayList<>(allIntersections);
        sortedList.sort((i1, i2) -> i1.getStatus().compareTo(i2.getStatus()));
        adapter.updateData(sortedList,showDistances);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new IntersectionAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadIntersectionsFromApi(false);
                    }
                }
            }
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String searchQuery = s.toString().toLowerCase();
                filterIntersectionsAndUpdateMap(searchQuery);
            }
        });
    }

    public List<Intersection> getIntersections() {
        return new ArrayList<>(allIntersections);
    }

    private void showAlert(Intersection intersection) {
        if (alertView != null && getContext() != null) {
            alertView.setVisibility(View.VISIBLE);
            alertView.show(intersection);
        }
    }

    private void filterIntersectionsAndUpdateMap(String searchQuery) {
        String selectedStatus = getSelectedStatus();

        List<Intersection> filteredList = allIntersections.stream()
                .filter(intersection ->
                        (intersection.getTitle().toLowerCase().contains(searchQuery) ||
                                intersection.getDescription().toLowerCase().contains(searchQuery)) &&
                                (selectedStatus.equals("Tous") || intersection.getStatus().equals(selectedStatus))
                )
                .collect(Collectors.toList());

        adapter.updateData(filteredList,showDistances);

        // Update map markers
        if (mapFragment != null) {
            List<String> filteredLocalisations = filteredList.stream()
                    .map(Intersection::getTitle)
                    .collect(Collectors.toList());
            mapFragment.highlightMarkers(filteredLocalisations);
        }
    }

    private void loadIntersections(boolean isRefresh) {
        if (isLoading) return;
        isLoading = true;

        Activity activity = getActivity();
        if (activity == null) return;

        DataManager dataManager = DataManager.getInstance(requireContext());


        new Thread(() -> {
            try {

                // Ensuite charger les données
                List<Intersection> intersections = dataManager.loadIntersections();
                Log.d("ListFragment", "Chargement de " + intersections.size() + " intersections");


                if (activity != null && !activity.isFinishing()) {
                    activity.runOnUiThread(() -> {
                        if (!isAdded()) return;

                        isLoading = false;
                        loadingIndicator.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        if (isRefresh) {
                            allIntersections.clear();
                        }
                        allIntersections.addAll(intersections);
                        filterIntersectionsAndUpdateMap(searchEditText.getText().toString().toLowerCase());
                    });
                }
            } catch (Exception e) {
                Log.e("ListFragment", "Erreur: " + e.getMessage());
            }
        }).start();
    }
    // Modifier le SwipeRefreshLayout
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            // Plus besoin de réinitialiser currentPage car plus de pagination
            loadIntersections(true);
        });
    }

    private void setupStatusFilter() {
        statusFilterGroup.setOnCheckedChangeListener((group, checkedId) ->
                filterIntersectionsAndUpdateMap(searchEditText.getText().toString().toLowerCase()));
    }



    private void loadIntersectionsFromApi(boolean isRefresh) {
        if (isLoading) return;
        isLoading = true;

        if (isRefresh) {
            loadingIndicator.setVisibility(View.VISIBLE);
        }

        // On demande un grand nombre d'entrées en une seule fois
        ApiClient.getClient().getChantiers(1000, 0).enqueue(new Callback<ChantierResponse>() {
            @Override
            public void onResponse(Call<ChantierResponse> call, Response<ChantierResponse> response) {
                isLoading = false;
                loadingIndicator.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<ChantierResponse.Chantier> chantiers = response.body().getResults();
                    Log.d("ListFragment", "Reçu " + chantiers.size() + " chantiers de l'API");

                    List<Intersection> intersections = chantiers.stream()
                            .map(chantier -> new Intersection(
                                    chantier.getLocalisation(),
                                    chantier.getLibelle(),
                                    "En cours",
                                    chantier.getGeoPoint().getLatitude(),
                                    chantier.getGeoPoint().getLongitude()
                            ))
                            .collect(Collectors.toList());

                    Log.d("ListFragment", "Converti en " + intersections.size() + " intersections");

                    // Sauvegarder dans la base de données
                    DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    try {
                        // Avant d'insérer, vérifier l'état de la base
                        Cursor cursor = db.query(DatabaseHelper.TABLE_INTERSECTIONS,
                                null, null, null, null, null, null);
                        Log.d("ListFragment", "Nombre d'entrées avant insertion : " + cursor.getCount());
                        cursor.close();

                        // Vider la table avant d'insérer les nouvelles données
                        int deletedRows = db.delete(DatabaseHelper.TABLE_INTERSECTIONS, null, null);
                        Log.d("ListFragment", "Nombre de lignes supprimées : " + deletedRows);

                        int insertCount = 0;
                        for (Intersection intersection : intersections) {
                            ContentValues values = new ContentValues();
                            values.put(DatabaseHelper.COLUMN_TITLE, intersection.getTitle());
                            values.put(DatabaseHelper.COLUMN_DESCRIPTION, intersection.getDescription());
                            values.put(DatabaseHelper.COLUMN_STATUS, intersection.getStatus());
                            values.put(DatabaseHelper.COLUMN_LATITUDE, intersection.getLatitude());
                            values.put(DatabaseHelper.COLUMN_LONGITUDE, intersection.getLongitude());

                            long id = db.insert(DatabaseHelper.TABLE_INTERSECTIONS, null, values);
                            if (id != -1) {
                                insertCount++;
                            } else {
                                Log.e("ListFragment", "Échec de l'insertion pour : " + intersection.getTitle());
                            }
                        }

                        Log.d("ListFragment", "Nombre d'intersections insérées : " + insertCount);

                        // Vérifier après insertion
                        cursor = db.query(DatabaseHelper.TABLE_INTERSECTIONS,
                                null, null, null, null, null, null);
                        Log.d("ListFragment", "Nombre d'entrées après insertion : " + cursor.getCount());
                        cursor.close();

                    } catch (Exception e) {
                        Log.e("ListFragment", "Erreur lors de la sauvegarde en base : " + e.getMessage());
                    } finally {
                        db.close();
                    }

                    if (isRefresh) {
                        allIntersections.clear();
                    }
                    allIntersections.addAll(intersections);
                    filterIntersectionsAndUpdateMap(searchEditText.getText().toString().toLowerCase());
                } else {
                    showError("Erreur lors du chargement des données");
                }
            }

            @Override
            public void onFailure(Call<ChantierResponse> call, Throwable t) {
                isLoading = false;
                loadingIndicator.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                showError("Erreur de connexion : " + t.getMessage());
            }
        });
    }
    private String getSelectedStatus() {
        if (statusFilterGroup == null) return "Tous";

        int checkedChipId = statusFilterGroup.getCheckedChipId();
        if (checkedChipId == R.id.chip_all) return "Tous";
        if (checkedChipId == R.id.chip_in_progress) return "En cours";
        if (checkedChipId == R.id.chip_planned) return "Planifié";
        if (checkedChipId == R.id.chip_completed) return "Terminé";
        return "Tous";
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onIntersectionClick(Intersection intersection) {
        mapFragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString("selectedLocalisation", intersection.getTitle());
        args.putDouble("latitude", intersection.getLatitude());
        args.putDouble("longitude", intersection.getLongitude());
        mapFragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, mapFragment)
                .addToBackStack(null)
                .commit();
    }


}