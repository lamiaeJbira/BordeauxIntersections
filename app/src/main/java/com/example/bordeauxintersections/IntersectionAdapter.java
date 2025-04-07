package com.example.bordeauxintersections;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bordeauxintersections.R;
import com.example.bordeauxintersections.Intersection;
import com.example.bordeauxintersections.databinding.ItemIntersectionBinding;

import java.util.ArrayList;
import java.util.List;
public class IntersectionAdapter extends RecyclerView.Adapter<IntersectionAdapter.ViewHolder> {
    private List<Intersection> intersections;
    private final OnIntersectionClickListener listener;
    private boolean showDistance = false;

    public interface OnIntersectionClickListener {
        void onIntersectionClick(Intersection intersection);
    }

    public List<Intersection> getIntersections() {
        return new ArrayList<>(intersections); // Retourne une copie de la liste
    }

    public IntersectionAdapter(List<Intersection> intersections, OnIntersectionClickListener listener) {
        this.intersections = intersections;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIntersectionBinding binding = ItemIntersectionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(intersections.get(position), showDistance);
    }

    @Override
    public int getItemCount() {
        return intersections.size();
    }

    // Supprimez cette méthode car elle est dupliquée
    // public void updateData(List<Intersection> newIntersections) {
    //     this.intersections = newIntersections;
    //     notifyDataSetChanged();
    // }

    public void updateData(List<Intersection> newIntersections, boolean showDistance) {
        this.intersections = newIntersections;
        this.showDistance = showDistance;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemIntersectionBinding binding;

        ViewHolder(ItemIntersectionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onIntersectionClick(intersections.get(position));
                }
            });
        }

        void bind(Intersection intersection, boolean showDistance) {
            binding.textTitle.setText(intersection.getTitle());
            binding.textDescription.setText(intersection.getDescription());
            binding.textStatus.setText(intersection.getStatus());

            // Gestion de l'affichage de la distance
            if (showDistance && binding.textDistance != null) {
                binding.textDistance.setVisibility(View.VISIBLE);
                binding.textDistance.setText(intersection.getFormattedDistance());
            } else if (binding.textDistance != null) {
                binding.textDistance.setVisibility(View.GONE);
            }
        }
    }
}