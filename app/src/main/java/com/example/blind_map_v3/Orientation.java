package com.example.blind_map_v3;

import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.mapbox.api.tilequery.MapboxTilequery;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.example.blind_map_v3.Constance.RESULT_GEOJSON_SOURCE_ID;

public class Orientation {
    Location curentLocation;
    List<Feature> featureList;
    double lastHeading;             //degrees of orientation sensor

    double pointLatitude;           //feature's X coordinate
    double pointLongitude;          //features's Y coordinate

    double curentLatitude;
    double curentLongitude;


    public Orientation(Location curentLocation, List<Feature> featureList, double lastHeading) {
        this.curentLocation = curentLocation;
        this.featureList = featureList;
        this.lastHeading = lastHeading;
//        double curentLatitude=curentLocation.getLatitude();
//        double curentLongitude=curentLocation.getLongitude();
    }


    public String getOrientationPoint() {
        curentLatitude = curentLocation.getLatitude();
        curentLongitude = curentLocation.getLongitude();

        String suitable;//="nothing found yet";
        for (Feature f : featureList) {

            if (((f.getProperty("name")) != null) || (f.getProperty("coordinates") != null)) {

                //TODO get coordinates
                Point p = (Point) f.geometry();

                pointLatitude = p.latitude();
                pointLongitude = p.longitude();

                System.err.printf("Lat %.10f Lon %.10f",p.longitude(),p.latitude());

                //TODO calculate the angle
                double angle;
                angle = Math.toDegrees(Math.atan(( pointLongitude- curentLongitude) / ( pointLatitude-curentLatitude )));

                System.err.println("Angle  " + String.valueOf(angle));

                //TODO find segment
                int feturesSegment = getFeaturesSegment(pointLongitude,pointLatitude);

                System.err.println("Segment  " + String.valueOf(feturesSegment));           //!!!!!!!!!!!!!

                //TODO normalize angle values in range (0;180),(0; -180);
                angle = normalizedAngle(feturesSegment, angle);

                System.err.println("normalizedAngle  " + String.valueOf(angle));


                //TODO compare features's angle and last heading angle
                double difference = Math.abs(lastHeading-angle);
                //get the smallest angle

                if (difference>180){
                    difference=360-difference;
                }

                System.err.println("difference  " + String.valueOf(difference));


                if (difference <= 20) {
                    // TODO save feature's data if is suitable
                    //distance
                    int distance;
                    String s = f.getProperty("tilequery").toString();
                    int indexStart = s.indexOf("\"distance\":") + 11;
                    int indexEnd = s.indexOf(',', indexStart);
                    s = s.substring(indexStart, indexEnd);
                    distance = (int) Double.parseDouble(s);
                    //type
                    String type = f.getStringProperty("category_en");
                    type = type.replaceAll("_", " ");
                    //name
                    String name = f.getStringProperty("name");

                    suitable = (String.format("%s %s Distance %d meters", name, type, distance) + " the angle is: " + difference);
                    return suitable;

                }

                else {
                    //suitable = ("within 15 degrees nothing found");
                    continue;
                }
            }
        }

//            } else
//                //может все же стоит возвращать номера тех домов, которых пустой "name"?
//             suitable="No objects in this area";
//            }


     return "nothing found yet";


    }

        //normalize angle
        public double normalizedAngle (int feturesSegment, double angle){
            double normAngle=angle;
            if (feturesSegment == 1){
                normAngle=angle;
            } else if (feturesSegment==2){
                normAngle=180+angle;
            }else if (feturesSegment ==3){
                normAngle=angle-180;
            }else if (feturesSegment==4){
                normAngle=angle;
            }

            else if (feturesSegment==5){
                normAngle=0;
            } else if (feturesSegment==6){
                angle=90;
            }else if (feturesSegment==7){
                angle=180;
            }else if (feturesSegment==8){
                angle=-90;
            }

            return normAngle;
        }

        // compare features's angle and last heading angle
        public double getDifference (double angle){
            double difference;
            difference = Math.abs(lastHeading-angle);
            //get the smallest angle
            if (difference>180){
                difference=360-difference;
            }
            return difference;
        }

        public int getFeaturesSegment(double pointLongitude, double pointLattitude){
            int featureSegment=0;
            try {
            if ((curentLongitude<pointLongitude) &&(curentLatitude<pointLattitude)){
                featureSegment=1;
            }
            if ((curentLongitude<pointLongitude) &&(curentLatitude>pointLattitude)){
                featureSegment=2;
            }
            if ((curentLongitude>pointLongitude) &&(curentLatitude>pointLattitude)){
                featureSegment=3;
            }
            if ((curentLongitude>pointLongitude) &&(curentLatitude<pointLattitude)){
                featureSegment=4;
            }
            if ((curentLongitude==pointLongitude) &&(curentLatitude<pointLattitude)){
                featureSegment=5;
            }
            if ((curentLongitude<pointLongitude) &&(curentLatitude==pointLattitude)){
                featureSegment=6;
            }
            if ((curentLongitude==pointLongitude) &&(curentLatitude>pointLattitude)){
                featureSegment=7;
            }
            if ((curentLongitude>pointLongitude) &&(curentLatitude==pointLattitude)){
                featureSegment=7;
            }} catch (Exception E){
                E.getMessage();
            }
            return featureSegment;
        }


    public static void main(String[] args) {


        Location curentLocation = new Location(LocationManager.GPS_PROVIDER);
        curentLocation.setLatitude(56.9708);
        curentLocation.setLongitude(24.1604);


        List<Feature> features = new ArrayList<>();

        Orientation or = new Orientation(curentLocation, features, 90d);
        or.makeTilequeryApiCall(new LatLng(curentLocation.getLatitude(),curentLocation.getLongitude()),curentLocation);
        String suitableFeature= or.getOrientationPoint();




    }

    private void makeTilequeryApiCall( @NonNull LatLng point, Location location) {
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
                System.out.println(new Orientation(location,featureList,90d).getOrientationPoint());
                System.err.println(featureList.size());
               // GeoJsonSource resultSource = style.getSourceAs(RESULT_GEOJSON_SOURCE_ID);
              //  if (resultSource != null && response.body().features() != null) {
                   // resultSource.setGeoJson(FeatureCollection.fromFeatures(response.body().features()));

              //  }
            }

            @Override
            public void onFailure(Call<FeatureCollection> call, Throwable throwable) {
                Timber.d("Request failed: %s", throwable.getMessage());
            }
        });
    }


}