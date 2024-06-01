package paint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

public class RoutePainter implements Painter<JXMapViewer> {
    private final List<GeoPosition> track;

    public RoutePainter(List<GeoPosition> track) {
        this.track = track;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
        g = (Graphics2D) g.create();
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(2));

        for (int i = 0; i < track.size() - 1; i++) {
            GeoPosition gp1 = track.get(i);
            GeoPosition gp2 = track.get(i + 1);

            Point2D pt1 = map.getTileFactory().geoToPixel(gp1, map.getZoom());
            Point2D pt2 = map.getTileFactory().geoToPixel(gp2, map.getZoom());

            // Convert Point2D to Point
            Point point1 = new Point((int) pt1.getX(), (int) pt1.getY());
            Point point2 = new Point((int) pt2.getX(), (int) pt2.getY());

            g.drawLine(point1.x, point1.y, point2.x, point2.y);
        }

        g.dispose();
    }
}
