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
    private Location curentLocation;
    private List<Feature> featureList;
    private double lastHeading;             //degrees of orientation sensor

    private double curentLatitude;
    private double curentLongitude;


    public Orientation(Location curentLocation, List<Feature> featureList, double lastHeading) {
        this.curentLocation = curentLocation;
        this.featureList = featureList;
        this.lastHeading = lastHeading;
    }

    String getOrientationPoint() {

        String suitable = Vocabulary.THERE_ARE_NO_POINTS_OF_INTEREST_AROUND;
        //TODO todoo. Check if featureList is not empty

        if (featureList.isEmpty()) {
            suitable = Vocabulary.THERE_ARE_NO_POINTS_OF_INTEREST_AROUND;
        } else {
            curentLatitude = curentLocation.getLatitude();
            curentLongitude = curentLocation.getLongitude();

            List<Feature> suitableFeatures = new ArrayList<>();

            for (Feature f : featureList) {
                if (((f.getProperty("name")) != null) || (f.getProperty("coordinates") != null)) {
                    double angle = getNormalizedAngleOffeature(f);
                    System.err.println("normalizedAngle  " + String.valueOf(angle));
                    //compare features's angle and last heading angle
                    double actualDifference = actualDifference(f);
                    double normalDifference = difference(angle, actualDifference);
                    System.err.println("difference  " + String.valueOf(normalDifference));

                    if (normalDifference <= 25) {
                        // save feature's data to the <<"suitableFeatures">> list if is suitable
                        suitableFeatures.add(f);
                        int distance = getDistance(f);
                        String type = type(f);
                        String name = name(f);
                        suitable = String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                    }
                }
            }
            /**
              *  1 if suitable features are found
             */
            if (!suitableFeatures.isEmpty()) {

                int theMostSuitableIndex = 0;
                double minAngle = difference(getNormalizedAngleOffeature(suitableFeatures.get(0)), actualDifference(suitableFeatures.get(0)));
                int minDistance = getDistance(suitableFeatures.get(0));
                int minAngleIndex = 0;
                int minDistanceIndex = 0;

                //TODO 1.1 find two objects among all suitable: nearest by distance and nearest by angle
                for (int i = 0; i < (suitableFeatures.size() - 1); i++) {
                    int distanceI = getDistance(suitableFeatures.get(i));
                    double differenceI = difference(getNormalizedAngleOffeature(suitableFeatures.get(i)), actualDifference(suitableFeatures.get(i)));
                    String typeI = type(suitableFeatures.get(i));
                    String nameI = name(suitableFeatures.get(i));
                    if (differenceI < minAngle) {         //if this object is nearer than nearest
                        minAngle = differenceI;            // then this ANGLE is as minimal
                        minAngleIndex = i;
                        theMostSuitableIndex = i;
                        suitable = String.format(Vocabulary.FeaterFormatText, nameI, typeI, Vocabulary.DISTANCE, distanceI, Vocabulary.METERS);
                    }
                    if (distanceI < minDistance) {        //if the next object is nearer than nearest
                        minDistance = distanceI;         // then this object's DISTANCE is as minimal
                        minDistanceIndex = i;
                        theMostSuitableIndex = i;
                        suitable = String.format(Vocabulary.FeaterFormatText, nameI, typeI, Vocabulary.DISTANCE, distanceI, Vocabulary.METERS);
                    }
                }

            /*TODO 1.2
            //Compare each object with both founded objects - if it is nearerByDistance than nearest by angle
            // and nearerByAngle than nearest by distance - this is the best
            */
                for (int i = 0; i < (suitableFeatures.size()); i++) {
                    String type = type(suitableFeatures.get(i));
                    String name = name(suitableFeatures.get(i));
                    int distance = getDistance(suitableFeatures.get(i));
                    double angle = getNormalizedAngleOffeature(suitableFeatures.get(i));
                    double diff = difference(angle, actualDifference(suitableFeatures.get(i)));
                    if ((angle < getNormalizedAngleOffeature(suitableFeatures.get(minDistanceIndex)))
                            && (distance < getDistance(suitableFeatures.get(minAngleIndex)))) {
                        theMostSuitableIndex = i;
                        suitable = String.format(Vocabulary.FeaterFormatText, name, type, Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                       // System.out.println("\n This is the best of the best of the best " + suitable);
                    }
                }
                //        String type = type(suitableFeatures.get(theMostSuitableIndex));
                //        String name = name(suitableFeatures.get(theMostSuitableIndex));
                //        int distance=getDistance(suitableFeatures.get(theMostSuitableIndex));
                //        double angle=getNormalizedAngleOffeature(suitableFeatures.get(theMostSuitableIndex));
                //        suitable = (String.format("%s %s Distance %d meters", name, type, distance)+" the angle is: " + angle);
            }

            //TODO 2 if there are no features in this area, find the nearest in entire list of features
            if (suitableFeatures.isEmpty()) {
                String theSide = "";
                int nearestFeaturesIndex = 0;
                //set in initial value of the smallest angle to 0
                double diff0 = 0;

                for (int i = 0; i < featureList.size(); i++) {
                    if (((featureList.get(i).getProperty("name")) != null) || (featureList.get(i).getProperty("coordinates") != null)) {
                        diff0 = difference((getNormalizedAngleOffeature(featureList.get(i))), actualDifference(featureList.get(i)));
                        nearestFeaturesIndex = i;
                        break;
                    }
                }

                //TODO 2.1. Find feature with smallest angle
                for (int i = 0; i < (featureList.size() - 1); i++) {
                    if (((featureList.get(i).getProperty("name")) != null) || (featureList.get(i).getProperty("coordinates") != null)) {
                        double angle = getNormalizedAngleOffeature(featureList.get(i));
                        double diffI = difference(angle, actualDifference(featureList.get(i)));
                        if (diffI < diff0) {
                            nearestFeaturesIndex = i;
                        }
                    }
                }

                //TODO 2.2. if this feature is accessable, find range of the angle of this feature if this feature is accessable
                if (((featureList.get(nearestFeaturesIndex).getProperty("name")) != null)
                        || (featureList.get(nearestFeaturesIndex).getProperty("coordinates") != null)) {

                    double angle = getNormalizedAngleOffeature(featureList.get(nearestFeaturesIndex));
                    System.err.println("normalizedAngle  " + String.valueOf(angle));
                    double actualDifference = actualDifference(featureList.get(nearestFeaturesIndex));
                    double normalDifference = difference(angle, actualDifference);
                    System.err.println("difference  " + String.valueOf(normalDifference));
                    if (actualDifference <= 90 && actualDifference > 20) {
                        theSide = "right";
                        int distance = getDistance(featureList.get(nearestFeaturesIndex));
                        String type = type(featureList.get(nearestFeaturesIndex));
                        String name = name(featureList.get(nearestFeaturesIndex));
                        suitable = Vocabulary.TURN_RIGHT + Vocabulary.THERE_IS
                                + String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);

                    } else if (actualDifference > 270 && actualDifference < 360) {
                        theSide = "left";
                        int distance = getDistance(featureList.get(nearestFeaturesIndex));
                        String type = type(featureList.get(nearestFeaturesIndex));
                        String name = name(featureList.get(nearestFeaturesIndex));
                        suitable = Vocabulary.TURN_LEFT + Vocabulary.THERE_IS +
                                String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                    } else if (actualDifference > 90 && actualDifference <= 160) {
                        theSide = "back right";
                        int distance = getDistance(featureList.get(nearestFeaturesIndex));
                        String type = type(featureList.get(nearestFeaturesIndex));
                        String name = name(featureList.get(nearestFeaturesIndex));
                        suitable = Vocabulary.TURN_BACK_RIGHT +Vocabulary.THERE_IS
                                + String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                    } else if (actualDifference > 200 && actualDifference <= 270) {
                        theSide = "back left";
                        int distance = getDistance(featureList.get(nearestFeaturesIndex));
                        String type = type(featureList.get(nearestFeaturesIndex));
                        String name = name(featureList.get(nearestFeaturesIndex));

                        suitable = Vocabulary.TURN_BACK_LEFT + Vocabulary.THERE_IS
                                + String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                    } else if (actualDifference > 160 && actualDifference <= 200) {
                        theSide = "back";
                        int distance = getDistance(featureList.get(nearestFeaturesIndex));
                        String type = type(featureList.get(nearestFeaturesIndex));
                        String name = name(featureList.get(nearestFeaturesIndex));

                        suitable = Vocabulary.TURN_BACK + Vocabulary.THERE_IS
                                + String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                    }

                    //TODO 2.2. if this feature is not accessable, than... umh
                } else {
                    suitable = ("What a desert...");
                }
            }
        }
        return suitable;
    }


    //////////
    /**
     *
     * Helping methods
     *
     * */
    ////////

    public String name(Feature f) {
        String name;
        name = f.getStringProperty("name");
        return name;
    }

    public String type(Feature f) {
        String type = f.getStringProperty("category_en");
        //type = type.replaceAll("_", " ");
        return type;
    }

    public int getDistance(Feature f) {
        int distance = 0;
        String s = f.getProperty("tilequery").toString();
        int indexStart = s.indexOf("\"distance\":") + 11;
        int indexEnd = s.indexOf(',', indexStart);
        s = s.substring(indexStart, indexEnd);
        distance = (int) Double.parseDouble(s);
        return distance;
    }


    // compare features's angle and last heading angle
    public double difference(double angle, double actualDifference) {
        double normalDifference;
        normalDifference = actualDifference;
        //get the smallest angle (f.e. not like 195 or 260 deg (actual), but 165 or 100 deg (normalized))
        if (actualDifference > 180d) {
            normalDifference = 360d - actualDifference;
        }
        return normalDifference;
    }

    public double actualDifference(Feature f) {
        double actualDifference;
        actualDifference = Math.abs(lastHeading - getNormalizedAngleOffeature(f));
        return actualDifference;
    }


    // Calculate angle by feature's parameteres
    public double getNormalizedAngleOffeature(Feature f) {

        //TODO 1 get coordinates
        Point p = (Point) f.geometry();
        double pointLatitude = p.latitude();
        double pointLongitude = p.longitude();
        //System.err.printf("Lat %.10f Lon %.10f", p.longitude(), p.latitude());

        //TODO 3 find segment
        int feturesSegment = getFeaturesSegment(pointLongitude, pointLatitude);
       // System.err.println("Segment  " + String.valueOf(feturesSegment));           //!!!!!!!!!!!!!

        //TODO 2 calculate the angle
        double angle;
        angle = Math.toDegrees(Math.atan((pointLongitude - curentLongitude) / (pointLatitude - curentLatitude)));
       // System.err.println("Angle  " + String.valueOf(angle));

        //TODO 4 normalize angle values in range (0;180),(0; -180);
        angle = normalizedAngle(feturesSegment, angle);
        return angle;
    }

    //normalize angle
    public double normalizedAngle(int feturesSegment, double angle) {
        double normAngle = angle;
        if (feturesSegment == 1) {
            normAngle = angle;
        } else if (feturesSegment == 2) {
            normAngle = 180 + angle;
        } else if (feturesSegment == 3) {
            normAngle = angle - 180;
        } else if (feturesSegment == 4) {
            normAngle = angle;
        } else if (feturesSegment == 5) {
            normAngle = 0;
        } else if (feturesSegment == 6) {
            angle = 90;
        } else if (feturesSegment == 7) {
            angle = 180;
        } else if (feturesSegment == 8) {
            angle = -90;
        }

        return normAngle;
    }


    public int getFeaturesSegment(double pointLongitude, double pointLattitude) {
        int featureSegment = 0;
        try {
            if ((curentLongitude < pointLongitude) && (curentLatitude < pointLattitude)) {
                featureSegment = 1;
            }
            if ((curentLongitude < pointLongitude) && (curentLatitude > pointLattitude)) {
                featureSegment = 2;
            }
            if ((curentLongitude > pointLongitude) && (curentLatitude > pointLattitude)) {
                featureSegment = 3;
            }
            if ((curentLongitude > pointLongitude) && (curentLatitude < pointLattitude)) {
                featureSegment = 4;
            }
            if ((curentLongitude == pointLongitude) && (curentLatitude < pointLattitude)) {
                featureSegment = 5;
            }
            if ((curentLongitude < pointLongitude) && (curentLatitude == pointLattitude)) {
                featureSegment = 6;
            }
            if ((curentLongitude == pointLongitude) && (curentLatitude > pointLattitude)) {
                featureSegment = 7;
            }
            if ((curentLongitude > pointLongitude) && (curentLatitude == pointLattitude)) {
                featureSegment = 7;
            }
        } catch (Exception E) {
            E.getMessage();
        }
        return featureSegment;
    }
}