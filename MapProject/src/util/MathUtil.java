
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
        return Math.sqrt(xdiff * xdiff + ydiff * ydiff); // return the distance (pytago :V)
    }

    // continue ... tomorrow, im going to sleep
    /*in this method, it will check whether the the line(x1, y1 - x2, y2) and line(x3, y3 - x4, y4) are intersected or not.
    *  return true if the two lines intersects, false otherwise
    */

    public static boolean isIntersected(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
        return findIntersection(x1, y1, x2, y2, x3, y3, x4, y4) != null;
    }

    /* calc the line(x1, y1 - x2, y2) and line(x3, y3 - x4, y4) and return instersection (x, y)
    * return null if no intersection
    */ 
    
    public static double[] findIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
        double den = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if(den != 0){
            double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / den;
            double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / den;
            if(ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1){
                double intersection[] = new double[2];
                intersection[0] = x1 + ua * (x2 - x1);
                intersection[1] = y1 + ua * (y2 - y1); 
                return intersection;
            }
        }
        return null;
    }

    public static double[] coordinate(double x, double y, double direction, double distance) {
		double coordinate[] = new double[2];
		double xoffset = distance * Math.cos(direction);
		double yoffset = distance * Math.sin(direction);
		coordinate[0] = x + xoffset;
		coordinate[1] = y + yoffset;
		return coordinate;
	}

    public static double normalizeAngle180(double angle){
        double theta = angle % 360;
        if(theta < 0) theta += 360;
        if(theta > 180) theta = 360 - theta;

        return theta;
    }
}
