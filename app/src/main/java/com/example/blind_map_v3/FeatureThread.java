package com.example.blind_map_v3;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mapbox.api.tilequery.MapboxTilequery;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.example.blind_map_v3.Constance.CLICK_CENTER_GEOJSON_SOURCE_ID;
import static com.example.blind_map_v3.Constance.RESULT_GEOJSON_SOURCE_ID;
import static com.example.blind_map_v3.MainActivity.curentLocation;

public class FeatureThread extends Thread {
    MapboxMap mapboxMap;
    MainActivity mainActivity;
    private List<Feature> featureList;



    public FeatureThread(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;


    }

    private void creatFeatureList(){
        LatLng point = new LatLng(curentLocation.getLatitude(),curentLocation.getLongitude());
        Style style = mapboxMap.getStyle();
        if (style != null) {
            // Move and display the click center layer's red marker icon to wherever the map was clicked on

            GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_CENTER_GEOJSON_SOURCE_ID);
            if (clickLocationSource != null) {
                clickLocationSource.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }
            // Use the map click location to make a Tilequery API call
            makeTilequeryApiCall(style, point);
        }
    }

    @Override
    public void run() {
        LatLng point = new LatLng(curentLocation.getLatitude(),curentLocation.getLongitude());
        Style style = mapboxMap.getStyle();
        if (style != null) {
            // Move and display the click center layer's red marker icon to wherever the map was clicked on

            GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_CENTER_GEOJSON_SOURCE_ID);
            if (clickLocationSource != null) {
                clickLocationSource.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }
            // Use the map click location to make a Tilequery API call
            makeTilequeryApiCall(style, point);
        }
    }

    private void makeTilequeryApiCall(@NonNull final Style style, @NonNull LatLng point) {
        MapboxTilequery tilequery = MapboxTilequery.builder()
                .accessToken("pk.eyJ1IjoiY29sbG9zIiwiYSI6ImNqeHlzbHZ5ajBjcmUzbW12aHozYWt3ZmwifQ.zqNqUIWpn6uppaykzZY4Qw")
                .mapIds("mapbox.mapbox-streets-v8")
                .query(Point.fromLngLat(point.getLongitude(), point.getLatitude()))
                .radius(100)
                .limit(10)
                .geometry("point")
                .dedupe(true)
                .layers("poi_label")
                .build();

        tilequery.enqueueCall(new Callback<FeatureCollection>() {
            @Override
            public void onResponse(Call<FeatureCollection> call, Response<FeatureCollection> response) {
                //tilequeryResponseTextView.setText(response.body().toJson());
                System.err.println(response.body().toString());
                System.err.println(response.body().features().get(0).getProperty("name"));
                System.err.println(response.body().features().get(0).getProperty("tilequery"));
                featureList = response.body().features();
                //System.err.println(featureList.size());
                //toastMSG(new NearPoints(featureList).getClosestFeatureName());
               // speak(null,new NearPoints(featureList).getClosestFeatureName());

                GeoJsonSource resultSource = style.getSourceAs(RESULT_GEOJSON_SOURCE_ID);
                if (resultSource != null && response.body().features() != null) {
                    resultSource.setGeoJson(FeatureCollection.fromFeatures(response.body().features()));

                }
            }

            @Override
            public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
                Timber.d("Request failed: %s", throwable.getMessage());
                //Toast.makeText(MainActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
