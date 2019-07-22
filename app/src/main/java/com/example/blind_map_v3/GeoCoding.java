package com.example.blind_map_v3;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.GeometryCollection;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class GeoCoding {

   // private MainActivity mainActivity;
    private double latit;
    private double longin;

    public GeoCoding(double longin, double latit) {
        this.latit = latit;
        this.longin = longin;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public String getURL (){
        ///geocoding/v5/{endpoint}/{longitude},{latitude}.json

        return String.format(Locale.US,"https://api.mapbox.com/geocoding/v5/mapbox.places/%f,%f.json?types=poi&language=en&access_token=%s",
                longin,
                latit,
                "pk.eyJ1IjoiY29sbG9zIiwiYSI6ImNqeHlzbHZ5ajBjcmUzbW12aHozYWt3ZmwifQ.zqNqUIWpn6uppaykzZY4Qw");
    }

    public Map<String,String> getAdressAndName (String url) throws IOException, JSONException {
        System.err.println(url);
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            System.err.println(jsonText);
            /*int adrIndex = jsonText.indexOf("\"address\":\"") + 11;
            int end  = jsonText.indexOf('"',adrIndex);
            int nameIndex = jsonText.indexOf("\"text_en\":\"") + 11;
            int nameEnd = jsonText.indexOf('"',nameIndex);
            String address = jsonText.substring(adrIndex, end);
            String name = jsonText.substring(nameIndex,nameEnd);*/
            JSONObject json = new JSONObject(jsonText);
            String address;
            try {
                address = json.getJSONObject("features").getJSONObject("properties").getString("address");
            } catch (JSONException e) {
                return null;
            }
            Map<String,String> addressAndName = new HashMap<>();
            addressAndName.put("address", address);
            //addressAndName.put("name",name);

            //FeatureCollection geoJson = FeatureCollection.fromJson(jsonText);
            //GeometryCollection geometryCollection = GeometryCollection.fromJson(jsonText);
            //JSONObject json = new JSONObject(jsonText);
            return addressAndName;
        } finally {
            is.close();
        }
    }

    public static void main(String[] args) throws IOException, JSONException {
        FeatureCollection geoJson = readJsonFromUrl("https://api.mapbox.com/geocoding/v5/mapbox.places/24.11511,%2056.95240.json?types=poi&language=en&access_token=pk.eyJ1IjoiY29sbG9zIiwiYSI6ImNqeHlzbHZ5ajBjcmUzbW12aHozYWt3ZmwifQ.zqNqUIWpn6uppaykzZY4Qw");
        System.out.println(geoJson.toString());
        System.out.println(geoJson.features().size());
        System.out.println(geoJson.features().get(0).getProperty("text"));
        List<Feature> featureList;
        String s = "{\"type\":\"FeatureCollection\",\"query\":[-73.989,40.733],\"features\":[{\"id\":\"poi.2302102478536\",\"type\":\"Feature\",\"place_type\":[\"poi\"],\"relevance\":1,\"properties\":{\"landmark\":true,\"address\":\"120 E 13th St\",\"category\":\"business, service, office\"},\"text\":\"Worn\",\"place_name\":\"Worn, 120 E 13th St, New York, New York 10003, United States\",\"center\":[-73.98885,40.733027],\"geometry\":{\"coordinates\":[-73.98885,40.733027],\"type\":\"Point\"},\"context\":[{\"id\":\"neighborhood.2103290\",\"text\":\"Greenwich Village\"},{\"id\":\"locality.12696928000137850\",\"wikidata\":\"Q11299\",\"text\":\"Manhattan\"},{\"id\":\"postcode.13482670360296810\",\"text\":\"10003\"},{\"id\":\"place.15278078705964500\",\"wikidata\":\"Q60\",\"text\":\"New York\"},{\"id\":\"region.14044236392855570\",\"short_code\":\"US-NY\",\"wikidata\":\"Q1384\",\"text\":\"New York\"},{\"id\":\"country.9053006287256050\",\"short_code\":\"us\",\"wikidata\":\"Q30\",\"text\":\"United States\"}]}],\"attribution\":\"NOTICE: © 2019 Mapbox and its suppliers. All rights reserved. Use of this data is subject to the Mapbox Terms of Service (https://www.mapbox.com/about/maps/). This response and the information it contains may not be retained. POI(s) provided by Foursquare.\"}\n";
       String d = "{\"type\":\"FeatureCollection\",\"query\":[24.11511,56.9524],\"features\":[{\"id\":\"poi.2370821992842\",\"type\":\"Feature\",\"place_type\":[\"poi\"],\"relevance\":1,\"properties\":{\"landmark\":true,\"address\":\"Brīvības bulvāris 32\",\"category\":\"food and drink, shop\"},\"text_en\":\"Narvesen   Sakta\",\"place_name_en\":\"Narvesen  Sakta, Brīvības bulvāris 32, Riga, Riga, Latvia\",\"text\":\"Narvesen   Sakta\",\"place_name\":\"Narvesen  Sakta, Brīvības bulvāris 32, Riga, Riga, Latvia\",\"center\":[24.114968,56.952349],\"geometry\":{\"coordinates\":[24.114968,56.952349],\"type\":\"Point\"},\"context\":[{\"id\":\"place.5595941408395980\",\"wikidata\":\"Q1773\",\"text_en\":\"Riga\",\"language_en\":\"en\",\"text\":\"Riga\",\"language\":\"en\"},{\"id\":\"region.12112275291687060\",\"short_code\":\"LV-RIX\",\"wikidata\":\"Q1773\",\"text_en\":\"Riga\",\"language_en\":\"en\",\"text\":\"Riga\",\"language\":\"en\"},{\"id\":\"country.11193360610267300\",\"short_code\":\"lv\",\"wikidata\":\"Q211\",\"text_en\":\"Latvia\",\"language_en\":\"en\",\"text\":\"Latvia\",\"language\":\"en\"}]}],\"attribution\":\"NOTICE: © 2019 Mapbox and its suppliers. All rights reserved. Use of this data is subject to the Mapbox Terms of Service (https://www.mapbox.com/about/maps/). This response and the information it contains may not be retained. POI(s) provided by Foursquare.\"}\n";
        // JSONObject jsonObject = new JSONObject(s);
        //GeometryCollection gc = GeometryCollection.fromJson(s);
        //System.out.println(gc.toString());
        int adrIndex = d.indexOf("\"address\":\"") + 11;
        int end  = d.indexOf('"',adrIndex);
        int nameIndex = d.indexOf("\"text_en\":\"") + 11;
        int nameEnd = d.indexOf('"',nameIndex);

        System.out.println(adrIndex);
        System.out.println(end);
        String address = d.substring(adrIndex, end);
        System.out.println(address);
        String name = d.substring(nameIndex,nameEnd);
        System.out.println(name);

    }


    //TODO delete
    public static FeatureCollection readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            System.out.println(jsonText);

            FeatureCollection geoJson = FeatureCollection.fromJson(jsonText);
            //GeometryCollection geometryCollection = GeometryCollection.fromJson(jsonText);
            //JSONObject json = new JSONObject(jsonText);
            return geoJson;
        } finally {
            is.close();
        }
    }
}
