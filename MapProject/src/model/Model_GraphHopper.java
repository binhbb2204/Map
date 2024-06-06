package model;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmapviewer.viewer.GeoPosition;

public class Model_GraphHopper {
    private String apiKey = "6f0523e9-8519-431a-9f9b-01910e7616bd";

    public String getRoute(GeoPosition start, GeoPosition end) throws Exception {
        String jsonPayload = createJsonPayload(start, end);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://graphhopper.com/api/1/route?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
    
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int responseCode = response.statusCode();
        System.out.println("Response Code: " + responseCode);
        String responseBody = response.body();
        
        //System.out.println("Response Body: " + responseBody);
    
        return responseBody;
    }
    public List<List<GeoPosition>> extractPoints(String jsonResponse) {
        List<List<GeoPosition>> allPaths = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            if (jsonObject.has("paths")) {
                JSONArray pathsArray = jsonObject.getJSONArray("paths");

                for (int i = 0; i < pathsArray.length(); i++) {
                    JSONObject pathObject = pathsArray.getJSONObject(i);

                    if (pathObject.has("points")) {
                        String pointsString = pathObject.getString("points");
                        List<GeoPosition> pathPoints = parsePolyline(pointsString);
                        allPaths.add(pathPoints);
                    }
                }
            } else {
                System.out.println("No 'paths' field found in the JSON response.");
            }
        } catch (JSONException e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
        }

        return allPaths;
    }

    private List<GeoPosition> parsePolyline(String polyline) {
        List<GeoPosition> pathPoints = new ArrayList<>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < polyline.length()) {
            int shift = 0;
            int result = 0;
            int byteValue;

            do {
                byteValue = polyline.charAt(index++) - 63;
                result |= (byteValue & 0x1F) << shift;
                shift += 5;
            } while (byteValue >= 0x20);

            int deltaLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLat;

            shift = 0;
            result = 0;

            do {
                byteValue = polyline.charAt(index++) - 63;
                result |= (byteValue & 0x1F) << shift;
                shift += 5;
            } while (byteValue >= 0x20);

            int deltaLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += deltaLng;

            double latitude = lat / 1E5;
            double longitude = lng / 1E5;
            GeoPosition point = new GeoPosition(latitude, longitude);
            pathPoints.add(point);
        }

        return pathPoints;
    }

    private String createJsonPayload(GeoPosition start, GeoPosition end) {
        return String.format(
                "{\"profile\":\"car\",\"points\":[[%f,%f],[%f,%f]],\"snap_preventions\":[\"motorway\",\"ferry\",\"tunnel\"],\"details\":[\"road_class\",\"surface\"]}",
                start.getLongitude(), start.getLatitude(),
                end.getLongitude(), end.getLatitude()
        );
    }
}
