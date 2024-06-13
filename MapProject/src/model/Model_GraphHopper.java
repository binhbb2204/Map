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
    private int visitedNodesCount;
    private String apiKey = "6f0523e9-8519-431a-9f9b-01910e7616bd";
    public Model_GraphHopper() {
   
    }
    public Model_GraphHopper(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getRoute(GeoPosition start, GeoPosition end, String profile) throws Exception {
        String jsonPayload = createJsonPayload(start, end, profile);
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
    
        // Determine routing mode
        String routingMode = "Flexible Mode"; // Since ch.disable=true & lm.disable=true
        System.out.println("Routing Mode: " + routingMode);
    
        // Update visited nodes count
        visitedNodesCount = calculateVisitedNodesCount(responseBody);
        System.out.println("Visited Nodes Count: " + visitedNodesCount);
    
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

    private String createJsonPayload(GeoPosition start, GeoPosition end, String profile) {
        return String.format(
            "{"
            + "\"profile\":\"%s\","  // Use the profile parameter
            + "\"points\":[[%f,%f],[%f,%f]],"
            + "\"snap_preventions\":[\"motorway\",\"ferry\",\"tunnel\"],"
            + "\"details\":[\"road_class\",\"surface\"],"
            + "\"ch.disable\": true"
            + "}",
            profile,  // Pass the profile parameter here
            start.getLongitude(), start.getLatitude(),
            end.getLongitude(), end.getLatitude()
        );
    }
    

    public int calculateVisitedNodesCount(String jsonResponse) {
        int totalCoordinates = 0;

        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);

            if (jsonObject.has("paths")) {
                JSONArray pathsArray = jsonObject.getJSONArray("paths");

                for (int i = 0; i < pathsArray.length(); i++) {
                    JSONObject pathObject = pathsArray.getJSONObject(i);

                    if (pathObject.has("points")) {
                        String pointsString = pathObject.getString("points");
                        List<GeoPosition> pathPoints = parsePolyline(pointsString);
                        totalCoordinates += pathPoints.size();
                    }
                }
            }
        } catch (JSONException e) {
            System.err.println("Error parsing JSON response: " + e.getMessage());
        }

        return totalCoordinates;
    }

    public int getVisitedNodesCount() {
        return visitedNodesCount;
    }

    public double calculateDistance(GeoPosition start, GeoPosition end) {
        // Calculate distance between two points (start and end)
        double earthRadius = 6371.01; // Earth's radius in kilometers
        double lat1 = Math.toRadians(start.getLatitude());
        double lon1 = Math.toRadians(start.getLongitude());
        double lat2 = Math.toRadians(end.getLatitude());
        double lon2 = Math.toRadians(end.getLongitude());

        double latDiff = lat2 - lat1;
        double lonDiff = lon2 - lon1;

        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }
}
