
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
    private final List<MyWaypoint> waypoints = new ArrayList<>();
    private final WaypointPainter<MyWaypoint> waypointPainter = new WaypointRender();
    private EventWaypoint event;
    public Form_Map() {
        initComponents();
        init();
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
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Clear all waypoints on right-click
                    clearWaypoint();
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    // Handle left-click as before
                    GeoPosition clickedGeoPosition = jXMapViewer.convertPointToGeoPosition(e.getPoint());
                    handleMapClick(clickedGeoPosition);
                }
            }
        });
        
        
    }
    private void handleMapClick(GeoPosition position) {
        // Log and display the coordinates of the click
        System.out.println("Map clicked at: " + position.getLatitude() + ", " + position.getLongitude());
        // Add a waypoint at the clicked position
        MyWaypoint newWaypoint = new MyWaypoint("New Waypoint", MyWaypoint.PointType.END, event, position);
        addWaypoint(newWaypoint);
    }
    
    private void addWaypoint(MyWaypoint newWaypoint) {
        // If there are already two waypoints, clear the last one
        if (waypoints.size() >= 2) {
            MyWaypoint lastWaypoint = waypoints.remove(waypoints.size() - 1);
            jXMapViewer.remove(lastWaypoint.getButton());
        }
    
        // Check if the new waypoint is at the same position as an existing one
        for (int i = 0; i < waypoints.size(); i++) {
            MyWaypoint existingWaypoint = waypoints.get(i);
            if (existingWaypoint.getPosition().equals(newWaypoint.getPosition())) {
                // Replace the existing waypoint with the new one
                waypoints.set(i, newWaypoint);
                jXMapViewer.remove(existingWaypoint.getButton());
                initWaypoint();
                return;
            }
        }
    
        // Add the new waypoint if it's unique
        waypoints.add(newWaypoint);
        initWaypoint();
    }
    
    

    private void initWaypoint() {
        // Create a waypoint painter and set the waypoints
        WaypointPainter<MyWaypoint> waypointPainter = new WaypointRender();
        Set<MyWaypoint> waypointSet = new HashSet<>(waypoints);
        waypointPainter.setWaypoints(waypointSet);
    
        // Create a compound painter to combine multiple painters
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();
    
        // Add the waypoint painter to the compound painter
        compoundPainter.addPainter(waypointPainter);
    
        // Create and populate the graph with your waypoints and the distances between them
        Graph graph = new Graph();
        populateGraphWithEdges(graph); // This method will add all the necessary edges to the graph
    
        // Check if there are exactly two waypoints to draw a route
        if (waypoints.size() == 2) {
            // Get the shortest path using Dijkstra's algorithm
            List<GeoPosition> shortestPath = Dijkstra.computeShortestPath(
                graph,
                waypoints.get(0).getPosition(),
                waypoints.get(1).getPosition()
            );
    
            // If a path exists, create a route painter and add it to the compound painter
            if (shortestPath != null && !shortestPath.isEmpty()) {
                RoutePainter routePainter = new RoutePainter(shortestPath);
                compoundPainter.addPainter(routePainter);
            }
        }
    
        // Set the compound painter as the overlay painter on the JXMapViewer
        jXMapViewer.setOverlayPainter(compoundPainter);
    
        // Add buttons for each waypoint
        for (MyWaypoint waypoint : waypoints) {
            jXMapViewer.add(waypoint.getButton());
        }
    
        // Refresh the map viewer to display the new route
        jXMapViewer.repaint();
    }

    private void populateGraphWithEdges(Graph graph) {
        for (int i = 0; i < waypoints.size(); i++) {
            for (int j = i + 1; j < waypoints.size(); j++) {
                double distance = calculateDistance(waypoints.get(i).getPosition(), waypoints.get(j).getPosition());
                graph.addEdge(waypoints.get(i).getPosition(), waypoints.get(j).getPosition(), distance);
                graph.addEdge(waypoints.get(j).getPosition(), waypoints.get(i).getPosition(), distance);
            }
        }
    }
    private double calculateDistance(GeoPosition pos1, GeoPosition pos2) {
        double earthRadius = 6371.01; // Earth's radius in kilometers
        double latDiff = Math.toRadians(pos2.getLatitude() - pos1.getLatitude());
        double lonDiff = Math.toRadians(pos2.getLongitude() - pos1.getLongitude());
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                   Math.cos(Math.toRadians(pos1.getLatitude())) * Math.cos(Math.toRadians(pos2.getLatitude())) *
                   Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void clearWaypoint() {
        for (MyWaypoint d : waypoints) {
            jXMapViewer.remove(d.getButton());
        }
        waypoints.clear();
        initWaypoint();
    }

    private EventWaypoint getEvent() {
        return new EventWaypoint() {
            @Override
            public void selected(MyWaypoint waypoint) {
                JOptionPane.showMessageDialog(Form_Map.this, waypoint.getName());
            }
        };
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
