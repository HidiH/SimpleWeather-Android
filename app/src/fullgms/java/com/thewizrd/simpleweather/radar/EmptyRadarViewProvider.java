package com.thewizrd.simpleweather.radar;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.GoogleMap;

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
    public void updateRadarView() {
        getMapView().getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        super.onMapReady(googleMap);
    }
}