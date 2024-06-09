package paint;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class RoutePainter implements Painter<JXMapViewer> {
    private final List<GeoPosition> track;
    private BufferedImage pinImage;
    private static final double ANGLE_THRESHOLD = 10.0; // Adjusted angle threshold for better detection

    public RoutePainter(List<GeoPosition> track) {
        this.track = track;
        try {
            pinImage = ImageIO.read(getClass().getResource("/icons/pin.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();
        // Convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        // Do the drawing
        g.setColor(new Color(0, 150, 255, 128));
        g.setStroke(new BasicStroke(8));

        Point2D lastPoint = null;
        Point2D lastLastPoint = null;
        for (GeoPosition gp : track) {
            Point2D point = map.getTileFactory().geoToPixel(gp, map.getZoom());

            if (lastPoint != null) {
                g.drawLine((int) lastPoint.getX(), (int) lastPoint.getY(), (int) point.getX(), (int) point.getY());

                if (lastLastPoint != null) {
                    double angle = calculateAngle(lastLastPoint, lastPoint, point);
                    if (Math.abs(angle) > ANGLE_THRESHOLD && pinImage != null) {
                        int pinX = (int) point.getX() - pinImage.getWidth() / 2;
                        int pinY = (int) point.getY() - pinImage.getHeight();
                        g.drawImage(pinImage, pinX, pinY, null);
                        //System.out.println("Pin placed at GeoPosition: " + gp + " with angle: " + angle);
                    }
                }
            }

            lastLastPoint = lastPoint;
            lastPoint = point;
        }

        g.dispose();
    }

    private double calculateAngle(Point2D p1, Point2D p2, Point2D p3) {
        double dx1 = p2.getX() - p1.getX();
        double dy1 = p2.getY() - p1.getY();
        double dx2 = p3.getX() - p2.getX();
        double dy2 = p3.getY() - p2.getY();

        double angle1 = Math.atan2(dy1, dx1);
        double angle2 = Math.atan2(dy2, dx2);

        double angle = Math.toDegrees(angle2 - angle1);

        // Normalize the angle to the range (-180, 180)
        if (angle > 180) {
            angle -= 360;
        } else if (angle < -180) {
            angle += 360;
        }

        return angle;
    }
}
