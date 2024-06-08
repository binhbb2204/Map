package paint;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

public class RoutePainter implements Painter<JXMapViewer> {
    private final List<GeoPosition> track;
    private BufferedImage pinImage;

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
        // System.out.println("Paint method called"); 
        // Convert from viewport to world bitmap
        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        // Do the drawing
        g.setColor(new Color(0, 150, 255, 128));
        g.setStroke(new BasicStroke(8));

        Point2D lastPoint = null;
        for (GeoPosition gp : track) {
            // Convert geo-coordinate to world bitmap pixel
            Point2D point = map.getTileFactory().geoToPixel(gp, map.getZoom());
            //System.out.println("GeoPosition in track: " + gp);
            if (lastPoint != null) {
                g.drawLine((int) lastPoint.getX(), (int) lastPoint.getY(), (int) point.getX(), (int) point.getY());
            }
            int index = track.indexOf(gp);
            if (index % 5 == 0 && pinImage != null) {
                int pinX = (int) point.getX() - pinImage.getWidth() / 2;
                int pinY = (int) point.getY() - pinImage.getHeight();
                g.drawImage(pinImage, pinX, pinY, null);
            }

            lastPoint = point;
        }

        // Dispose the copy of the graphics object
        g.dispose();
    }
    
}
