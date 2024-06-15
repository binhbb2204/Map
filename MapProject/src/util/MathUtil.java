
package util;

public class MathUtil {
    

    public static double direction(double x1, double y1, double x2, double y2){
        double xdiff = x2 - x1;
        double ydiff = y2 - y1;

        return Math.atan2(ydiff, xdiff); // return the direction in radians
    }

    public static double distance(double x1, double y1, double x2, double y2){
        double xdiff = x2 - x1;
        double ydiff = y2 - y1;
        return Math.sqrt(xdiff*xdiff + ydiff*ydiff); // return the distance
    }

    // continue ... tomorrow, im going to sleep
}
