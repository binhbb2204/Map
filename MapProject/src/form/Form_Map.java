
package form;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.viewer.WaypointRenderer;

import paint.Dijkstra;
import paint.DistanceCalculator;
import paint.Graph;
import paint.RoutePainter;
import waypoint.EventWaypoint;
import waypoint.MyWaypoint;
import waypoint.WaypointRender;


public class Form_Map extends javax.swing.JPanel {
    private final Graph graph;
    private final List<MyWaypoint> waypoints = new ArrayList<>();
    private EventWaypoint event;
    public Form_Map() {
        initComponents();
        init();
        initRouteFinding();
        graph = new Graph();
        initGraph();
        
    }


    public void init(){
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        jXMapViewer.setTileFactory(tileFactory);
        GeoPosition geo = new GeoPosition(10.8775848,106.7990447);
        jXMapViewer.setAddressLocation(geo);
        jXMapViewer.setZoom(12);

        MouseInputListener mm = new PanMouseInputListener(jXMapViewer);
        jXMapViewer.addMouseListener(mm);
        jXMapViewer.addMouseMotionListener(mm);
        jXMapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(jXMapViewer));

        jXMapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    GeoPosition clickedGeoPosition = jXMapViewer.convertPointToGeoPosition(e.getPoint());
                    handleMapClick(clickedGeoPosition);
                } else {
                    clearWaypoints();
                } 
            }
        });
        
    }
    private void initGraph() {
        graph.clear();
    
        System.out.println("Waypoints list: " + waypoints);
    
        for (int i = 0; i < waypoints.size() - 1; i++) {
            MyWaypoint current = waypoints.get(i);
            MyWaypoint next = waypoints.get(i + 1);
    
            graph.addVertex(current);
            graph.addVertex(next);
    
            double distance = DistanceCalculator.calculateDistance(current.getPosition(), next.getPosition());
            System.out.println("Distance between " + current + " and " + next + ": " + distance);
    
            graph.addEdge(current, next, distance);
            graph.addEdge(next, current, distance);
        }
    
        System.out.println("Adjacency List after adding vertices and edges: " + graph.adjacencyList);
    }
    
    
    
    
    private void initRouteFinding() {
        jXMapViewer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (SwingUtilities.isLeftMouseButton(evt)) {
                    GeoPosition clickedGeoPosition = jXMapViewer.convertPointToGeoPosition(evt.getPoint());
                    handleMapClick(clickedGeoPosition);
                } else {
                    clearWaypoints();
                }
            }
        });
    }
    private void handleMapClick(GeoPosition position) {
        System.out.println("Clicked position: " + position.getLatitude() + ", " + position.getLongitude());
    
        // Add a waypoint at the clicked position
        MyWaypoint newWaypoint = new MyWaypoint("New Waypoint", MyWaypoint.PointType.END, event, position);
        addWaypoint(newWaypoint);
    
        // Update the map visualization
        updateMapVisualization();
    }
    private void updateMapVisualization() {
        // Create a set of waypoints to be painted on the map
        Set<MyWaypoint> waypointSet = new HashSet<>(waypoints);
    
        // Create a WaypointPainter to paint the waypoints
        WaypointPainter<MyWaypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypointSet);
    
        // Define a WaypointRenderer to specify how to draw each waypoint
        waypointPainter.setRenderer(new WaypointRenderer<MyWaypoint>() {
            @Override
            public void paintWaypoint(Graphics2D g, JXMapViewer map, MyWaypoint waypoint) {
                // Draw the waypoint icon, for example, a red circle
                g.setColor(Color.RED);
                g.fillOval(-5, -5, 10, 10);
            }
        });
    
        // Set the WaypointPainter on the JXMapViewer
        jXMapViewer.setOverlayPainter(waypointPainter);
    
        // Repaint the JXMapViewer to show the new waypoints
        jXMapViewer.repaint();
    }
    
    
    private void findAndDrawShortestPath(MyWaypoint source, MyWaypoint destination) {
        System.out.println("Source: " + source);
        System.out.println("Destination: " + destination);
    
        Map<MyWaypoint, Double> distances = Dijkstra.shortestPath(graph, source);
        System.out.println("Distances: " + distances);
    
        if (distances.containsKey(destination)) {
            MyWaypoint current = destination;
            List<MyWaypoint> path = new ArrayList<>();
            path.add(current);
    
            while (current != source) {
                current = graph.getPredecessor(current, distances);
                path.add(current);
            }
    
            Collections.reverse(path);
            System.out.println("Path: " + path);
    
            drawRoute(path);
        } else {
            JOptionPane.showMessageDialog(this, "No path found from source to destination.");
        }
    }
    
    

    private void addWaypoint(MyWaypoint waypoint) {
        System.out.println("Adding waypoint: " + waypoint.getName() + " at " + waypoint.getPosition());
        if (waypoints.size() >= 2) {
            // Remove the second waypoint (old waypoint B)
            MyWaypoint secondWaypoint = waypoints.get(1);
            jXMapViewer.remove(secondWaypoint.getButton());
            waypoints.remove(1);
            graph.removeVertex(secondWaypoint);
        }

        // Add the new waypoint as waypoint B
        waypoints.add(0, waypoint);
        graph.addVertex(waypoint);

        // If there are two waypoints, find and draw the shortest path
        if (waypoints.size() == 2) {
            MyWaypoint source = waypoints.get(0);
            MyWaypoint destination = waypoints.get(1);
            double distance = DistanceCalculator.calculateDistance(source.getPosition(), destination.getPosition());
            graph.addEdge(source, destination, distance);
            graph.addEdge(destination, source, distance); // For undirected graph
            findAndDrawShortestPath(source, destination);
        }
    }
    

    private void initWaypoints() {
        WaypointPainter<MyWaypoint> wp = new WaypointRender();
        wp.setWaypoints(new HashSet<>(waypoints)); // Convert to HashSet for WaypointPainter
        jXMapViewer.setOverlayPainter(wp);
        for (MyWaypoint d : waypoints) {
            jXMapViewer.add(d.getButton());
        }
    }

    private void drawRoute(List<MyWaypoint> path) {
        List<GeoPosition> route = new ArrayList<>();
        for (MyWaypoint waypoint : path) {
            route.add(waypoint.getPosition());
        }
    
        RoutePainter routePainter = new RoutePainter(route);
        jXMapViewer.setOverlayPainter(routePainter);
    }

    private void clearWaypoints() {
        for (MyWaypoint d : waypoints) {
            jXMapViewer.remove(d.getButton());
        }
        waypoints.clear();
        initWaypoints();
    }

    private EventWaypoint getEvent() {
        return new EventWaypoint() {
            @Override
            public void selected(MyWaypoint waypoint) {
                JOptionPane.showMessageDialog(Form_Map.this, waypoint.getName());
            }
        };
    }

    private MyWaypoint getSourceWaypoint() {
        // Implement logic to determine the source waypoint, e.g., select the first waypoint
        if (!waypoints.isEmpty()) {
            return waypoints.get(0);
        } else {
            return null; // No waypoints available
        }
    }

    private MyWaypoint getDestinationWaypoint() {
        // Implement logic to determine the destination waypoint, e.g., select the last waypoint
        if (waypoints.size() >= 2) {
            return waypoints.get(waypoints.size() - 1);
        } else {
            return null; // Not enough waypoints available
        }
    }

    private void calculateAndAddEdge(MyWaypoint source, MyWaypoint destination) {
        // Print out the coordinates of the source and destination waypoints
        System.out.println("Source waypoint position: " + source.getPosition().getLatitude() + ", " + source.getPosition().getLongitude());
        System.out.println("Destination waypoint position: " + destination.getPosition().getLatitude() + ", " + destination.getPosition().getLongitude());
    
        // Calculate the distance between the source and destination waypoints
        double distance = DistanceCalculator.calculateDistance(source.getPosition(), destination.getPosition());
    
        // Print out the calculated distance
        System.out.println("Calculated distance: " + distance);
    
        // Add the edge to the graph if the distance is greater than zero
        if (distance > 0) {
            graph.addEdge(source, destination, distance);
            graph.addEdge(destination, source, distance); 
        } else {
            System.out.println("The distance between waypoints is zero, not adding edge.");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jXMapViewer = new org.jxmapviewer.JXMapViewer();
        comboMapType = new combo_suggestion.ComboBoxSuggestion();
        header1 = new component.Header();

        comboMapType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Open Street", "Virtual Earth", "Hybrid", "Statelite" }));
        comboMapType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMapTypeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jXMapViewerLayout = new javax.swing.GroupLayout(jXMapViewer);
        jXMapViewer.setLayout(jXMapViewerLayout);
        jXMapViewerLayout.setHorizontalGroup(
            jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jXMapViewerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(header1, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 328, Short.MAX_VALUE)
                .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jXMapViewerLayout.setVerticalGroup(
            jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXMapViewerLayout.createSequentialGroup()
                .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jXMapViewerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(header1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(368, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jXMapViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jXMapViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void comboMapTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboMapTypeActionPerformed
        TileFactoryInfo info = null;
        int index = comboMapType.getSelectedIndex();
        if(index == 0){
            info = new OSMTileFactoryInfo();
        }
        else if(index == 1){
            info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.MAP);
        }
        else if(index == 2){
            info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.HYBRID);
        }
        else if(index == 3){
            info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.SATELLITE);
        }
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        jXMapViewer.setTileFactory(tileFactory);
    }//GEN-LAST:event_comboMapTypeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private combo_suggestion.ComboBoxSuggestion comboMapType;
    private component.Header header1;
    private org.jxmapviewer.JXMapViewer jXMapViewer;
    // End of variables declaration//GEN-END:variables
}
