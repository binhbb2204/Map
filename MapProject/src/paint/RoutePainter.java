package paint;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

public class RoutePainter implements Painter<JXMapViewer> {
    private final List<GeoPosition> track;

    public RoutePainter(List<GeoPosition> track) {
        this.track = track;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        g = (Graphics2D) g.create();
        // System.out.println("Paint method called"); 
        // Convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        // Do the drawing
        g.setColor(new Color(255, 0, 0, 128));
        g.setStroke(new BasicStroke(6));

        Point2D lastPoint = null;
        for (GeoPosition gp : track) {
            // Convert geo-coordinate to world bitmap pixel
            Point2D point = map.getTileFactory().geoToPixel(gp, map.getZoom());
            //System.out.println("GeoPosition in track: " + gp);
            if (lastPoint != null) {
                g.drawLine((int) lastPoint.getX(), (int) lastPoint.getY(), (int) point.getX(), (int) point.getY());
            }

            lastPoint = point;
        }

        // Dispose the copy of the graphics object
        g.dispose();
    }
    
}
