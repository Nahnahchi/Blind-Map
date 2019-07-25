package com.example.blind_map_v3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class GeoCoding {

   // private MainActivity mainActivity;
    private double latit;
    private double longin;

    GeoCoding(double longin, double latit) {
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

    String getURL (){
        ///geocoding/v5/{endpoint}/{longitude},{latitude}.json

        return String.format(Locale.US,"https://api.mapbox.com/geocoding/v5/mapbox.places/%f,%f.json?types=poi&language=en&access_token=%s",
                longin,
                latit,
                "pk.eyJ1IjoiY29sbG9zIiwiYSI6ImNqeHlzbHZ5ajBjcmUzbW12aHozYWt3ZmwifQ.zqNqUIWpn6uppaykzZY4Qw");
    }

    String getAddress(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            String address = null;
            try {
                JSONArray features = json.getJSONArray("features");
                for (int i = 0; i < features.length(); i++) {
                    JSONObject item = features.getJSONObject(i);
                    try {
                        JSONObject properties = item.getJSONObject("properties");
                        address = properties.getString("address");
                        break;
                    } catch (JSONException ignored) {
                    }
                }
                if (address == null) {
                    return null;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return address;
        }
    }
}
