
package model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.jxmapviewer.viewer.GeoPosition;

public class Model_RouteHopper {
    private String api_key = "6f0523e9-8519-431a-9f9b-01910e7616bd";

    public String getConnection(GeoPosition start, GeoPosition end) throws IOException, InterruptedException {
        double startLat = start.getLatitude();
        double startLon = start.getLongitude();
        double endLat = end.getLatitude();
        double endLon = end.getLongitude();

        // Construct the URL with the coordinates
        String url = String.format("https://graphhopper.com/api/1/route?point=%.6f,%.6f&point=%.6f,%.6f&profile=car&locale=de&calc_points=false&key=%s",
                                    startLat, startLon, endLat, endLon, api_key);

        // Create an HttpClient
        HttpClient client = HttpClient.newHttpClient();

        // Create an HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(url))
                                         .GET()
                                         .build();

        // Send the request and receive the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        System.out.println("Response Body: " + responseBody);
        // Return the response body
        return responseBody;
    }
}