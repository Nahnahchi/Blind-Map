package com.example.blind_map_v3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
//import android.support.design.widget.Snackbar;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
//import com.example.blind_map_v3.R;
//import com.mapbox.android.core.R;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.api.tilequery.MapboxTilequery;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;


/**
 * Use the LocationLayerOptions class to customize the LocationComponent's device location icon.
 */
public class MainActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, MapboxMap.OnMapClickListener{

    private static final String SAVED_STATE_CAMERA = "saved_state_camera";
    private static final String SAVED_STATE_RENDER = "saved_state_render";
    private static final String SAVED_STATE_LOCATION = "saved_state_location";
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private static final String RESULT_GEOJSON_SOURCE_ID = "RESULT_GEOJSON_SOURCE_ID";
    private static final String CLICK_CENTER_GEOJSON_SOURCE_ID = "CLICK_CENTER_GEOJSON_SOURCE_ID";
    private static final String CLICK_CENTER_NAVIGATION = "CLICK_CENTER_NAVIGATION";
    private static final String LAYER_ID = "LAYER_ID";
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private Location lastLocation;
    static Location curentLocation;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);
    private Point originPosition;
    private Point destinationPosition;
    private Button curLocationCamera;
    private Button getAddressButton; //TODO only for test
    private Button layerButton; //TODO only for tests
    private Button navigationButton;
    private TextView tilequeryResponseTextView;
    private List<Feature> featureList;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";
    private DirectionsRoute currentRout;
    private Button speak;

   // MapboxTilequery tilequery;
    //private Marker


    public Location getCurentLocation() {
        return curentLocation;
    }

    @CameraMode.Mode
    private int cameraMode = CameraMode.TRACKING;

    @RenderMode.Mode
    private int renderMode = RenderMode.NORMAL;


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        curLocationCamera = findViewById(R.id.curLocationButton);
        getAddressButton = findViewById(R.id.textButton);
        navigationButton = findViewById(R.id.navigationButton);
        // Check and use saved instance state in case of device rotation
        if (savedInstanceState != null) {
            cameraMode = savedInstanceState.getInt(SAVED_STATE_CAMERA);
            renderMode = savedInstanceState.getInt(SAVED_STATE_RENDER);
            lastLocation = savedInstanceState.getParcelable(SAVED_STATE_LOCATION);
            curentLocation = lastLocation;
        }

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        speak = findViewById(R.id.Speak);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak( null, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
            }
        });
        
        curLocationCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(
                        MainActivity.this,
                        getString(R.string.tap_on_map_instruction),
                        Toast.LENGTH_LONG
                ).show();

                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(curentLocation.getLatitude(),curentLocation.getLongitude())) // Sets the new camera position
                        .zoom(18) // Sets the zoom
                        .bearing(curentLocation.getBearing()) // Rotate the camera
                        .tilt(50) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 7000);
            }
        });
/*
        getAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    GeoCoding geoCoding = new GeoCoding(curentLocation.getLongitude(), curentLocation.getLatitude());
                    Map<String,String> addresAndPOI = geoCoding.getAdressAndName(geoCoding.getURL());
                    String name = "";
                    String address = "";
                    for (Map.Entry<String,String> pair : addresAndPOI.entrySet()) {
                        if(pair.getKey().equals("name")){
                            name = pair.getValue();
                        } else if (pair.getKey().equals("address")){
                            address = pair.getValue();
                        } else{
                            name = "ERROR";
                            address = "ERROR";
                        }
                    }

                    Toast.makeText(
                            MainActivity.this,
                            "Address = " + address + " Name =" + name,
                            Toast.LENGTH_LONG
                    ).show();
                } catch (JSONException e){
                    System.err.println(e.getMessage());
                    Toast.makeText(
                            MainActivity.this,
                            "JSON " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }catch (IOException e) {
                    System.err.println(e.getMessage());
                    Toast.makeText(
                            MainActivity.this,
                            "IO" + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
*/
        getAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
//                    if(featureList.isEmpty()){
//                        System.out.println("ERRROE");
//                    }
//                    for (Feature feature : featureList) {
//                        System.out.println(feature.toString());
//                    }
//
//                    Toast.makeText(
//                            MainActivity.this,
//                            new NearPoints(featureList).getClosestFeatureName(),
//                            Toast.LENGTH_LONG
//                    ).show();
                }
            }
        });

        navigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRout)
                        .shouldSimulateRoute(true)
                        .build();
                NavigationLauncher.startNavigation(MainActivity.this,options);
            }
        });
    }

    public void speak(String locale, String toSpeak) {
        new Speaker(this, locale, toSpeak);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/collos/cjxyyo0kz0g7j1cow76pb9i50"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
//                        VectorSource vectorSource = new VectorSource(
//                                "trees-source",
//                                "http://api.mapbox.com/v4/mapbox.mapbox-streets-v8.json?access_token=" + getString(R.string.mapbox_access_token)
//                        );
//                        style.addSource(vectorSource);
//                        CircleLayer circleLayer = new CircleLayer("trees-style", "trees-source");
//// replace street-trees-DC-9gvg5l with the name of your source layer
//                        circleLayer.setSourceLayer("pois");
//                        circleLayer.withProperties(
//                                circleOpacity(1.6f),
//                                circleColor(Color.parseColor("#D81B60")),
//                                circleRadius(
//                                        interpolate(exponential(1.0f), get("DBH"),
//                                                stop(0, 0f),
//                                                stop(1, 1f),
//                                                stop(110, 11f)
//                                        )
//                                )
//                        );
//                        style.addLayer(circleLayer);


                        enableLocationComponent(style);
                        //addClickLayer(style);
                        addClickLayer2(style);
                        addResultLayer(style);

                        // Toast instructing user to tap on the map
                        Toast.makeText(MainActivity.this,getString
                                (R.string.tap_on_map_instruction),
                                Toast.LENGTH_LONG).show();
                        mapboxMap.addOnMapClickListener(MainActivity.this);
                    }

                });
    }


    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        Speaker.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

//        outState.putInt(SAVED_STATE_CAMERA, cameraMode);
//        outState.putInt(SAVED_STATE_RENDER, renderMode);
//        if (locationComponent != null) {
//            outState.putParcelable(SAVED_STATE_LOCATION, locationComponent.getLastKnownLocation());
//        }

    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);

            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;

        LocationChangeListeningActivityLocationCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            MainActivity activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();
                curentLocation = location;
                if (location == null) {
                    return;
                }

                // Create a Toast which displays the new location's coordinates
//                Toast.makeText(activity, String.format(activity.getString(R.string.new_location),
//                        String.valueOf(result.getLastLocation().getLatitude()),
//                        String.valueOf(result.getLastLocation().getLongitude())),
//                        Toast.LENGTH_SHORT).show();

                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

// Toast instructing user to tap on the map

        Style style = mapboxMap.getStyle();
        if (style != null) {
// Move and display the click center layer's red marker icon to wherever the map was clicked on
            GeoJsonSource clickLocationSource = style.getSourceAs(CLICK_CENTER_NAVIGATION);
            if (clickLocationSource != null) {
                clickLocationSource.setGeoJson(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
            }

// Use the map click location to make a Tilequery API call
            //makeTilequeryApiCall(style, point);
            destinationPosition = Point.fromLngLat(point.getLongitude(),point.getLatitude());
            navigationButton.setEnabled(true);
            navigationButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.mapboxBlueDark));
            getRoute(Point.fromLngLat(curentLocation.getLongitude(),curentLocation.getLatitude()),destinationPosition);

            return true;
        }

        return false;

    }

    private void getRoute(Point origin, Point destination){
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body() == null){
                            Log.e(TAG,"No routes found, check right user and accesss token");
                            return;
                        } else if(response.body().routes().size() == 0){
                            Log.e(TAG,"No routes found");
                            toastMSG("No routes found");
                            return;
                        }
                        currentRout = response.body().routes().get(0);
                        if(navigationMapRoute != null){
                            navigationMapRoute.updateRouteArrowVisibilityTo(false);
                            navigationMapRoute.updateRouteVisibilityTo(false);
                            } else{
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap);
                        }
                        navigationMapRoute.addRoute(currentRout);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                            Log.e(TAG,"Error: " + t.getMessage());
                    }
                });
    }


    //TODO DELETE
    private void toggleLayer() {
        Layer layer = mapboxMap.getStyle().getLayer("trees-style");
        if (layer != null) {
            if (VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(NONE));
            } else {
                layer.setProperties(visibility(VISIBLE));
            }
        }
    }

    /**
     * Use the Java SDK's MapboxTilequery class to build a API request and use the API response
     *
     * @param point the center point that the the tilequery will originate from.
     */
    private void makeTilequeryApiCall(@NonNull final Style style, @NonNull LatLng point) {
        MapboxTilequery tilequery = MapboxTilequery.builder()
                .accessToken(getString(R.string.mapbox_access_token))
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
//                System.err.println(response.body().toString());
//                System.err.println(response.body().features().get(0).getProperty("name"));
//                System.err.println(response.body().features().get(0).getProperty("tilequery"));
                featureList = response.body().features();
                System.err.println(featureList.size());
                toastMSG(new NearPoints(featureList).getClosestFeatureName());
                GeoJsonSource resultSource = style.getSourceAs(RESULT_GEOJSON_SOURCE_ID);
                if (resultSource != null && response.body().features() != null) {
                    resultSource.setGeoJson(FeatureCollection.fromFeatures(response.body().features()));

                }
            }

            @Override
            public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
                Timber.d("Request failed: %s", throwable.getMessage());
                Toast.makeText(MainActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Add a map layer which will show a marker icon where the map was clicked
     */

    //TODO DELETE
    private void addClickLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("CLICK-ICON-ID", BitmapFactory.decodeResource(
                MainActivity.this.getResources(), R.drawable.green_marker));

        loadedMapStyle.addSource(new GeoJsonSource(CLICK_CENTER_GEOJSON_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[] {})));

        loadedMapStyle.addLayer(new SymbolLayer("click-layer", CLICK_CENTER_GEOJSON_SOURCE_ID).withProperties(
                iconImage("CLICK-ICON-ID"),
                iconOffset(new Float[] {0f, -12f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    private void addClickLayer2(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("CLICK-ICON-ID-2", BitmapFactory.decodeResource(
                MainActivity.this.getResources(), R.drawable.purple_marker));

        loadedMapStyle.addSource(new GeoJsonSource(CLICK_CENTER_NAVIGATION,
                FeatureCollection.fromFeatures(new Feature[] {})));

        loadedMapStyle.addLayer(new SymbolLayer("click-layer-navig", CLICK_CENTER_NAVIGATION).withProperties(
                iconImage("CLICK-ICON-ID-2"),
                iconOffset(new Float[] {0f, -12f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    /**
     * Add a map layer which will show marker icons for all of the Tilequery API results
     */
    private void addResultLayer(@NonNull Style loadedMapStyle) {
// Add the marker image to map
        loadedMapStyle.addImage("RESULT-ICON-ID", BitmapFactory.decodeResource(
                MainActivity.this.getResources(), R.drawable.blue_marker));

// Retrieve GeoJSON information from the Mapbox Tilequery API
        loadedMapStyle.addSource(new GeoJsonSource(RESULT_GEOJSON_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[] {})));

        loadedMapStyle.addLayer(new SymbolLayer(LAYER_ID, RESULT_GEOJSON_SOURCE_ID).withProperties(
                iconImage("RESULT-ICON-ID"),
                iconOffset(new Float[] {0f, -12f}),
                iconIgnorePlacement(true),
                iconAllowOverlap(true)
        ));
    }

    private void toastMSG(String string){
        Toast.makeText(
                  MainActivity.this,
                   string,
                    Toast.LENGTH_LONG
                    ).show();
    }

}
