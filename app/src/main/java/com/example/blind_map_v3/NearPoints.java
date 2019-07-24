package com.example.blind_map_v3;

import com.mapbox.geojson.Feature;

import java.util.List;

public class NearPoints {
    private List<Feature> featureList;

    public NearPoints(List<Feature> featureList) {
        this.featureList = featureList;
    }

    public String getClosestFeatureName (){
        for (Feature feature : featureList) {
            String name = feature.getStringProperty("name");
            if(name != null){
                return String.format("%s %s Distance %d meters", name,getFeatureType(feature),getDistance(feature));
            }

        }
        return "There are no points of interest around";
    }

    private int getDistance(Feature feature){
        int distance;
        String s = feature.getProperty("tilequery").toString();
        int indexStart = s.indexOf("\"distance\":")+11;
        int indexEnd = s.indexOf(',',indexStart);
                s = s.substring(indexStart,indexEnd);
        distance = (int)Double.parseDouble(s);
        return distance;
    }

    private String getFeatureType(Feature feature){
        String type = feature.getStringProperty("category_en");

        if(type == null){
            return "";
        }
        type = type.replaceAll("_", " ");
        return type;
    }
}
