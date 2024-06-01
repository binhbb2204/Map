package paint;
import org.jxmapviewer.viewer.GeoPosition;

public class DistanceCalculator {
    // Radius of the Earth in kilometers
    private static final double EARTH_RADIUS = 6371; // in kilometers

    // Calculate distance between two GeoPositions using the Haversine formula
    public static double calculateDistance(GeoPosition pos1, GeoPosition pos2) {
        if (pos1.equals(pos2)) {
            // If the positions are the same, return 0.0
            return 0.0;
        }
    
        double lat1 = Math.toRadians(pos1.getLatitude());
        double lon1 = Math.toRadians(pos1.getLongitude());
        double lat2 = Math.toRadians(pos2.getLatitude());
        double lon2 = Math.toRadians(pos2.getLongitude());
    
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
    
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    
        // Distance in kilometers
        double distance = EARTH_RADIUS * c;
        return distance;
    }
    
}
