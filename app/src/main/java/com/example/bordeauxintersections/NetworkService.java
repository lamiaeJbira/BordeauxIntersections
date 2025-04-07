package com.example.bordeauxintersections;

import android.util.Log;
import com.example.bordeauxintersections.Intersection;
import com.example.bordeauxintersections.api.ApiClient;
import com.example.bordeauxintersections.api.ChantierResponse;
import retrofit2.Call;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkService {
    private static final String TAG = "NetworkService";
    private static final int PAGE_SIZE = 100;

    public List<Intersection> getIntersections() throws Exception {
        List<Intersection> allIntersections = new ArrayList<>();
        int currentPage = 0;
        boolean hasMoreData = true;

        while (hasMoreData) {
            int offset = currentPage * PAGE_SIZE;

            // Appel synchrone à l'API
            Call<ChantierResponse> call = ApiClient.getClient().getChantiers(PAGE_SIZE, offset);
            Response<ChantierResponse> response = call.execute(); // Appel synchrone

            if (response.isSuccessful() && response.body() != null) {
                List<ChantierResponse.Chantier> chantiers = response.body().getResults();

                // Conversion des Chantiers en Intersections
                List<Intersection> pageIntersections = chantiers.stream()
                        .map(this::convertChantierToIntersection)
                        .collect(Collectors.toList());

                allIntersections.addAll(pageIntersections);

                // Vérifier s'il y a encore des données à charger
                hasMoreData = chantiers.size() >= PAGE_SIZE;
                currentPage++;

                Log.d(TAG, "Loaded page " + currentPage + " with " + chantiers.size() + " items");
            } else {
                throw new Exception("Erreur API: " + response.code() + " " + response.message());
            }
        }

        return allIntersections;
    }

    private Intersection convertChantierToIntersection(ChantierResponse.Chantier chantier) {
        return new Intersection(
                chantier.getLocalisation(),
                chantier.getLibelle(),
                determineStatus(chantier.getDateDebut(), chantier.getDateFin()),
                chantier.getGeoPoint().getLatitude(),
                chantier.getGeoPoint().getLongitude()
        );
    }

    private String determineStatus(String dateDebut, String dateFin) {
        // TODO: Implémenter la logique pour déterminer le statut en fonction des dates
        // Pour l'instant, on retourne "En cours" par défaut
        return "En cours";
    }
}