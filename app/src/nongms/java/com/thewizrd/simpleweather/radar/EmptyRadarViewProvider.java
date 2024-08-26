package com.thewizrd.simpleweather.radar;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class EmptyRadarViewProvider extends MapTileRadarViewProvider {
    public EmptyRadarViewProvider(@NonNull Context context, @NonNull ViewGroup rootView) {
        super(context, rootView);
    }

    @Override
    public void onCreateView(@Nullable Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        if (getViewContainer().getChildCount() == 0) {
            getViewContainer().addView(getMapView());
        }
    }

    @Override
    public void onMapReady() {
        super.onMapReady();

        IGeoPoint cameraPosition = getMapCameraPosition();
        if (cameraPosition != null) {
            if (interactionsEnabled()) {
                if (locationMarker == null) {
                    locationMarker = new Marker(getMapView());
                    locationMarker.setDefaultIcon();
                    getMapView().getOverlays().add(locationMarker);
                }

                locationMarker.setPosition(new GeoPoint(cameraPosition.getLatitude(), cameraPosition.getLongitude()));
            }
        }

        getMapView().postInvalidate();
    }
}
