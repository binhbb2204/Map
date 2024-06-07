package paint;

import org.jxmapviewer.viewer.GeoPosition;

public class Haversine {
    public static double calculate(GeoPosition pos1, GeoPosition pos2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(pos2.getLatitude() - pos1.getLatitude());
        double lonDistance = Math.toRadians(pos2.getLongitude() - pos1.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                   Math.cos(Math.toRadians(pos1.getLatitude())) * Math.cos(Math.toRadians(pos2.getLatitude())) *
                   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
