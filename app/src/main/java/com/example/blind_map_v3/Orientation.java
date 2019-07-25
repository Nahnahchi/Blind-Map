package com.example.blind_map_v3;

import android.location.Location;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import java.util.ArrayList;
import java.util.List;


class Orientation {
    private Location curentLocation;
    private List<Feature> featureList;
    private double lastHeading;             //degrees of orientation sensor

    private double curentLatitude;
    private double curentLongitude;


    Orientation(Location curentLocation, List<Feature> featureList, double lastHeading) {
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
                    System.err.println("normalizedAngle  " + angle);
                    //compare features's angle and last heading angle
                    double actualDifference = actualDifference(f);
                    double normalDifference = difference(actualDifference);
                    System.err.println("difference  " + normalDifference);

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
            /*
                1 if suitable features are found
             */
            if (!suitableFeatures.isEmpty()) {

                double minAngle = difference(actualDifference(suitableFeatures.get(0)));
                int minDistance = getDistance(suitableFeatures.get(0));
                int minAngleIndex = 0;
                int minDistanceIndex = 0;

                //TODO 1.1 find two objects among all suitable: nearest by distance and nearest by angle
                for (int i = 0; i < (suitableFeatures.size() - 1); i++) {
                    int distanceI = getDistance(suitableFeatures.get(i));
                    double differenceI = difference(actualDifference(suitableFeatures.get(i)));
                    String typeI = type(suitableFeatures.get(i));
                    String nameI = name(suitableFeatures.get(i));
                    if (differenceI < minAngle) {         //if this object is nearer than nearest
                        minAngle = differenceI;            // then this ANGLE is as minimal
                        minAngleIndex = i;
                        suitable = String.format(Vocabulary.FeaterFormatText, nameI, typeI, Vocabulary.DISTANCE, distanceI, Vocabulary.METERS);
                    }
                    if (distanceI < minDistance) {        //if the next object is nearer than nearest
                        minDistance = distanceI;         // then this object's DISTANCE is as minimal
                        minDistanceIndex = i;
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
                    if ((angle < getNormalizedAngleOffeature(suitableFeatures.get(minDistanceIndex)))
                            && (distance < getDistance(suitableFeatures.get(minAngleIndex)))) {
                        suitable = String.format(Vocabulary.FeaterFormatText, name, type, Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                    }
                }
            }

            //TODO 2 if there are no features in this area, find the nearest in entire list of features
            if (suitableFeatures.isEmpty()) {
                int nearestFeaturesIndex = 0;
                //set in initial value of the smallest angle to 0
                double diff0 = 0;

                for (int i = 0; i < featureList.size(); i++) {
                    if (((featureList.get(i).getProperty("name")) != null) || (featureList.get(i).getProperty("coordinates") != null)) {
                        diff0 = difference(actualDifference(featureList.get(i)));
                        nearestFeaturesIndex = i;
                        break;
                    }
                }

                //TODO 2.1. Find feature with smallest angle
                for (int i = 0; i < (featureList.size() - 1); i++) {
                    if (((featureList.get(i).getProperty("name")) != null) || (featureList.get(i).getProperty("coordinates") != null)) {
                        double diffI = difference(actualDifference(featureList.get(i)));
                        if (diffI < diff0) {
                            nearestFeaturesIndex = i;
                        }
                    }
                }

                //TODO 2.2. if this feature is accessable, find range of the angle of this feature if this feature is accessable
                if (((featureList.get(nearestFeaturesIndex).getProperty("name")) != null)
                        || (featureList.get(nearestFeaturesIndex).getProperty("coordinates") != null)) {

                    double actualDifference = actualDifference(featureList.get(nearestFeaturesIndex));
                    if (actualDifference <= 90 && actualDifference > 20) {
                        int distance = getDistance(featureList.get(nearestFeaturesIndex));
                        String type = type(featureList.get(nearestFeaturesIndex));
                        String name = name(featureList.get(nearestFeaturesIndex));
                        suitable = Vocabulary.TURN_RIGHT + Vocabulary.THERE_IS
                                + String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);

                    } else if (actualDifference > 270 && actualDifference < 360) {
                        int distance = getDistance(featureList.get(nearestFeaturesIndex));
                        String type = type(featureList.get(nearestFeaturesIndex));
                        String name = name(featureList.get(nearestFeaturesIndex));
                        suitable = Vocabulary.TURN_LEFT + Vocabulary.THERE_IS +
                                String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                    } else if (actualDifference > 90 && actualDifference <= 160) {
                        int distance = getDistance(featureList.get(nearestFeaturesIndex));
                        String type = type(featureList.get(nearestFeaturesIndex));
                        String name = name(featureList.get(nearestFeaturesIndex));
                        suitable = Vocabulary.TURN_BACK_RIGHT +Vocabulary.THERE_IS
                                + String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                    } else if (actualDifference > 200 && actualDifference <= 270) {
                        int distance = getDistance(featureList.get(nearestFeaturesIndex));
                        String type = type(featureList.get(nearestFeaturesIndex));
                        String name = name(featureList.get(nearestFeaturesIndex));

                        suitable = Vocabulary.TURN_BACK_LEFT + Vocabulary.THERE_IS
                                + String.format(Vocabulary.FeaterFormatText, name, type,Vocabulary.DISTANCE, distance, Vocabulary.METERS);
                    } else if (actualDifference > 160 && actualDifference <= 200) {
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

    private String name(Feature f) {
        String name;
        name = f.getStringProperty("name");
        return name;
    }

    private String type(Feature f) {
        return f.getStringProperty("category_en");
    }

    private int getDistance(Feature f) {
        int distance;
        String s = f.getProperty("tilequery").toString();
        int indexStart = s.indexOf("\"distance\":") + 11;
        int indexEnd = s.indexOf(',', indexStart);
        s = s.substring(indexStart, indexEnd);
        distance = (int) Double.parseDouble(s);
        return distance;
    }


    // compare features's angle and last heading angle
    private double difference(double actualDifference) {
        double normalDifference;
        normalDifference = actualDifference;
        //get the smallest angle (f.e. not like 195 or 260 deg (actual), but 165 or 100 deg (normalized))
        if (actualDifference > 180d) {
            normalDifference = 360d - actualDifference;
        }
        return normalDifference;
    }

    private double actualDifference(Feature f) {
        double actualDifference;
        actualDifference = Math.abs(lastHeading - getNormalizedAngleOffeature(f));
        return actualDifference;
    }


    // Calculate angle by feature's parameteres
    private double getNormalizedAngleOffeature(Feature f) {

        //TODO 1 get coordinates
        Point p = (Point) f.geometry();
        assert p != null;
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
    private double normalizedAngle(int feturesSegment, double angle) {
        double normAngle = angle;
         if (feturesSegment == 2) {
            normAngle = 180 + angle;
        } else if (feturesSegment == 3) {
            normAngle = angle - 180;
        } else if (feturesSegment == 5) {
            normAngle = 0;
        } else if (feturesSegment == 6) {
             normAngle = 90;
        } else if (feturesSegment == 7) {
             normAngle = 180;
        } else if (feturesSegment == 8) {
             normAngle = -90;
        }
        return normAngle;
    }


    private int getFeaturesSegment(double pointLongitude, double pointLattitude) {
        int featureSegment = 0;

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

        return featureSegment;
    }
}