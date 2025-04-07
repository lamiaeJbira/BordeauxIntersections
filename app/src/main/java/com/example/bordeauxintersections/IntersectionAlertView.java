package com.example.bordeauxintersections;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

public class IntersectionAlertView extends FrameLayout {
    private CardView cardView;
    private TextView titleText;
    private TextView descriptionText;
    private TextView distanceText;
    private ImageButton closeButton;
    private ObjectAnimator slideAnimator;

    public IntersectionAlertView(Context context) {
        super(context);
        init(context);
    }

    public IntersectionAlertView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // Inflater le layout
        View view = LayoutInflater.from(context).inflate(R.layout.alert_intersection, this, true);

        // Initialiser les vues
        cardView = view.findViewById(R.id.card_view);
        titleText = view.findViewById(R.id.alert_title);
        descriptionText = view.findViewById(R.id.alert_description);
        distanceText = view.findViewById(R.id.alert_distance);
        closeButton = view.findViewById(R.id.btn_close);

        // Configurer l'animation
        slideAnimator = ObjectAnimator.ofFloat(this, "translationY", -getHeight(), 0f);
        slideAnimator.setDuration(500);
        slideAnimator.setInterpolator(new DecelerateInterpolator());

        // Gestionnaire de fermeture
        closeButton.setOnClickListener(v -> dismiss());

        // Cacher initialement
        setTranslationY(-1000f);
    }

    public void show(Intersection intersection) {
        titleText.setText(intersection.getTitle());
        descriptionText.setText(intersection.getDescription());
        distanceText.setText("À " + intersection.getFormattedDistance());

        // Lancer l'animation d'entrée
        slideAnimator.start();
    }

    public void dismiss() {
        ObjectAnimator dismissAnimator = ObjectAnimator.ofFloat(
                this, "translationY", getTranslationY(), -getHeight()
        );
        dismissAnimator.setDuration(300);
        dismissAnimator.setInterpolator(new AccelerateInterpolator());
        dismissAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }
        });
        dismissAnimator.start();
    }
}