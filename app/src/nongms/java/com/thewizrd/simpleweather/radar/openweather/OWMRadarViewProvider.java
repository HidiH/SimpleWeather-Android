package com.thewizrd.simpleweather.radar.openweather;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.thewizrd.shared_resources.di.UtilsModuleKt;
import com.thewizrd.shared_resources.utils.Colors;
import com.thewizrd.shared_resources.utils.StringUtils;
import com.thewizrd.shared_resources.weatherdata.WeatherAPI;
import com.thewizrd.simpleweather.radar.MapTileRadarViewProvider;
import com.thewizrd.weather_api.keys.Keys;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class OWMRadarViewProvider extends MapTileRadarViewProvider {
    private TilesOverlay tilesOverlay;

    public OWMRadarViewProvider(@NonNull Context context, @NonNull ViewGroup rootView) {
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

        if (tilesOverlay == null) {
            MapTileProviderBase tileProvider = new MapTileProviderBasic(getContext(), new OWMTileProvider(), new TileWriter());
            tilesOverlay = new TilesOverlay(tileProvider, getContext(), false, false);
            tilesOverlay.setLoadingLineColor(Colors.TRANSPARENT);
            tilesOverlay.setLoadingBackgroundColor(Colors.TRANSPARENT);
            getMapView().getOverlays().add(tilesOverlay);
        }

        getMapView().postInvalidate();
    }

    private static class OWMTileProvider extends XYTileSource {
        public OWMTileProvider() {
            super("OWM", MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL, 256, ".png",
                    new String[]{"https://tile.openweathermap.org/"});
        }

        @Override
        public String getTileURLString(long pMapTileIndex) {
            int zoom = MapTileIndex.getZoom(pMapTileIndex);
            int x = MapTileIndex.getX(pMapTileIndex);
            int y = MapTileIndex.getY(pMapTileIndex);

            /* Define the URL pattern for the tile images */
            return String.format(Locale.ROOT,
                    "https://tile.openweathermap.org/map/precipitation_new/%d/%d/%d.png?appid=%s", zoom, x, y,
                    getKey()
            );
        }

        private String getKey() {
            String key = UtilsModuleKt.getSettingsManager().getAPIKey(WeatherAPI.OPENWEATHERMAP);

            if (StringUtils.isNullOrWhitespace(key))
                return Keys.getOWMKey();

            return key;
        }
    }
}
