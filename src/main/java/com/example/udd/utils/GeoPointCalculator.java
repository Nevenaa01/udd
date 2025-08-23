package com.example.udd.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GeoPointCalculator {
    public static GeoPoint Calculate(String address) throws IOException {
        String urlStr = "https://nominatim.openstreetmap.org/search?q="
                + URLEncoder.encode(address, StandardCharsets.UTF_8)
                + "&format=json&limit=1";

        // Open connection
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // Nominatim requires a user-agent
        conn.connect();

        // Read response
        Scanner sc = new Scanner(url.openStream());
        StringBuilder inline = new StringBuilder();
        while (sc.hasNext()) {
            inline.append(sc.nextLine());
        }
        sc.close();

        // Parse JSON
        JSONArray jsonArray = new JSONArray(inline.toString());

        if (jsonArray.length() > 0) {
            JSONObject location = jsonArray.getJSONObject(0);
            double lat = Double.parseDouble(location.getString("lat"));
            double lon = Double.parseDouble(location.getString("lon"));
            return new GeoPoint(lat, lon);
        }

        return null;
    }

    public static String GetAddresFromGeoPoint(GeoPoint geoPoint) throws IOException {
        String urlStr = "https://nominatim.openstreetmap.org/reverse?lat=" + geoPoint.getLat()
                + "&lon=" + geoPoint.getLon() + "&format=json&addressdetails=1&accept-language=sr-Latn";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0"); // Required for Nominatim
        conn.connect();

        Scanner sc = new Scanner(url.openStream());
        StringBuilder inline = new StringBuilder();
        while (sc.hasNext()) {
            inline.append(sc.nextLine());
        }
        sc.close();

        JSONObject json = new JSONObject(inline.toString());
        JSONObject address = json.getJSONObject("address");

        // Get street name, city, postcode, etc.
        String road = address.has("road") ? address.getString("road") : "";
        String city = address.has("city") ? address.getString("city") : "";
        String houseNumber = address.has("house_number") ? address.getString("house_number") : "";

        return road + " " + houseNumber + ", " + city;
    }
}
