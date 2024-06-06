
package form;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import com.github.kevinsawicki.http.HttpRequest;

import glasspanepopup.GlassPanePopup;
import model.Model_Error;
import model.Model_GraphHopper;
import model.Model_RouteHopper;
import paint.Dijkstra;
import paint.Graph;
import paint.RoutePainter;
import waypoint.EventWaypoint;
import waypoint.MyWaypoint;
import waypoint.WaypointRender;

public class Form_Map extends javax.swing.JPanel {
    private final List<MyWaypoint> waypoints = new ArrayList<>();
    private final WaypointPainter<MyWaypoint> waypointPainter = new WaypointRender();
    private EventWaypoint event;
    private Model_GraphHopper graphHopper;
    private Model_RouteHopper routeHopper;
    private GeoPosition fromPosition = null;
    private GeoPosition toPosition = null;
    private Graph graph;
    public Form_Map() {
        initComponents();
        init();
        setupTextFields();
        graphHopper = new Model_GraphHopper();
        routeHopper = new Model_RouteHopper();
        graph = new Graph();
    }
        
    private void handleResponse(String response) {
        // Handle the response here, for example, update the map with new waypoints
        System.out.println(response);
        
        // Extract points from the response
        List<List<GeoPosition>> allPaths = graphHopper.extractPoints(response);
        
        // Check if there are any paths
        if (allPaths.isEmpty()) {
            System.out.println("No paths found in the response.");
            // Display a message or handle the absence of paths as needed
        } else {
            // Print out the points for each path
            for (int i = 0; i < allPaths.size(); i++) {
                List<GeoPosition> pathPoints = allPaths.get(i);
                System.out.println("Path " + (i + 1) + " points:");
                for (GeoPosition point : pathPoints) {
                    System.out.println("Latitude: " + point.getLatitude() + ", Longitude: " + point.getLongitude());
                    

                }
                initRoute(graph, pathPoints);
            }
        }
    }

    private void showError(Exception e) {
        // Show error message to the user
        System.out.println(this + "Error: " + e.getMessage()+ "Error");
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        GlassPanePopup.showPopup(Error);
        
        Error.setData(new Model_Error(this + "Error: " + e.getMessage()+ "Error"));
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
                    txtFrom.setText("");
                    txtTo.setText("");
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    // Handle left-click as before
                    GeoPosition clickedGeoPosition = jXMapViewer.convertPointToGeoPosition(e.getPoint());
                    handleMapClick(clickedGeoPosition);
                }
            }
        });
        
        
    }
    
    private void setupTextFields() {
        // Add key listener to txtFrom
        txtFrom.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    handleLocationInput(txtFrom.getText(), true);
                }
            }
        });

        // Add key listener to txtTo
        txtTo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    handleLocationInput(txtTo.getText(), false);
                }
            }
        });
    }

    private void updateTextFields() {
        GeoPosition fromPosition = null;
        GeoPosition toPosition = null;

        if (waypoints.size() > 0) {
            MyWaypoint firstWaypoint = waypoints.get(0);
            String location = getLocation(firstWaypoint.getPosition());
            txtFrom.setText(location);
            fromPosition = firstWaypoint.getPosition();
        } else {
            txtFrom.setText("");
        }

        if (waypoints.size() > 1) {
            MyWaypoint secondWaypoint = waypoints.get(1);
            String location = getLocation(secondWaypoint.getPosition());
            txtTo.setText(location);
            toPosition = secondWaypoint.getPosition();
        } else {
            txtTo.setText("");
        }

        if (fromPosition != null && toPosition != null) {
            // Fetch the route information in a separate thread to avoid blocking the UI
            GeoPosition finalFromPosition = fromPosition;
            GeoPosition finalToPosition = toPosition;
            SwingUtilities.invokeLater(() -> {
                try {
                    String routeInfo = graphHopper.getRoute(finalFromPosition, finalToPosition);
                    String routeCoor = routeHopper.getConnection(finalFromPosition, finalToPosition);
                    System.out.println();
                    handleResponse(routeInfo);
                    System.out.println();
                    handleResponse(routeCoor);
                } catch (Exception e) {
                    showError(e);
                }
            });
        }
    }

    private void handleLocationInput(String locationName, boolean isFrom) {
    try {
        GeoPosition position = getLocationFromAPI(locationName);
        System.out.println("Location: " + locationName);
        System.out.println("Latitude: " + position.getLatitude() + ", Longitude: " + position.getLongitude());

        if (isFrom) {
            fromPosition = position;
        } else {
            toPosition = position;
        }
        // Create a new waypoint with the retrieved position
        MyWaypoint newWaypoint = new MyWaypoint("New Waypoint", MyWaypoint.PointType.END, event, position);

        // Add the new waypoint
        addWaypoint(newWaypoint);

        // If both from and to positions are set, fetch the route
        if (fromPosition != null && toPosition != null) {
            // Fetch the route information in a separate thread to avoid blocking the UI
            new Thread(() -> {
                try {
                    String routeInfo = graphHopper.getRoute(fromPosition, toPosition);
                    String routeCoor = routeHopper.getConnection(fromPosition, toPosition);
                    SwingUtilities.invokeLater(() -> handleResponse(routeCoor));
                    System.out.println();
                    SwingUtilities.invokeLater(() -> handleResponse(routeInfo));
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> showError(e));
                }
            }).start();
        }
    } catch (JSONException e) {
        System.out.println("Error retrieving location: " + e.getMessage());
    } catch (Exception e) {
        showError(e);
    }
}
    
    private void handleMapClick(GeoPosition position) {
        // Log and display the coordinates of the click
        System.out.println("Map clicked at: " + position.getLatitude() + ", " + position.getLongitude());

        try {
            // Get and print the location based on the clicked position
            String location = getLocation(position);
            System.out.println("Location: " + location);
            int id = getOSM_ID(position);
            System.out.println(id);

        } 
        catch (JSONException e) {
            System.err.println("Failed to parse JSON response: " + e.getMessage());
        } 
        
        // Add a waypoint at the clicked position
        MyWaypoint newWaypoint = new MyWaypoint("New Waypoint", MyWaypoint.PointType.END, event, position);
        addWaypoint(newWaypoint);
    }
    
    public String getLocation(GeoPosition pos) throws JSONException{
        String body = HttpRequest.get("https://nominatim.openstreetmap.org/reverse?lat=" + pos.getLatitude() + "&lon=" + pos.getLongitude() + "&format=json").body();
        JSONObject json = new JSONObject(body);
        return json.getString("display_name");
    }
    public int getOSM_ID(GeoPosition pos) throws JSONException{
        String body = HttpRequest.get("https://nominatim.openstreetmap.org/reverse?lat=" + pos.getLatitude() + "&lon=" + pos.getLongitude() + "&format=json").body();
        JSONObject json = new JSONObject(body);
        return json.getInt("osm_id");
    }
    private GeoPosition getLocationFromAPI(String query) throws JSONException {
    if (query == null || query.trim().isEmpty()) {
        System.out.println("Query is empty or null.");
        return null; // Or return a default GeoPosition if appropriate
    }

    try {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        String url = "https://nominatim.openstreetmap.org/search?q=" + encodedQuery + "&format=json";
        System.out.println("API URL: " + url); // Print the full API URL for debugging

        String body = HttpRequest.get(url).body();
        System.out.println("Raw JSON response: " + body);

        // Check if the response is an empty array
        if (body.trim().equals("[]")) {
            System.out.println("No results found for the query: " + query);
            Error.setData(new Model_Error("No results found for the query: " + query));
            GlassPanePopup.showPopup(Error);
            return null; // Or return a default GeoPosition if appropriate
        }

        JSONArray jsonArray = new JSONArray(body.trim());
        if (jsonArray.length() == 0) {
            System.out.println("No results found in the JSON array.");
            return null; // Or return a default GeoPosition if appropriate
        }

        JSONObject json = jsonArray.getJSONObject(0); // Get the first result
        double lat = json.getDouble("lat");
        double lon = json.getDouble("lon");
        return new GeoPosition(lat, lon);
    } 
    catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}

    

    public GeoPosition setLocationFrom(String name) throws JSONException {
        return getLocationFromAPI(txtFrom.getText());
    }

    public GeoPosition setLocationTo(String name) throws JSONException {
        return getLocationFromAPI(txtTo.getText());
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
                updateTextFields();
                initWaypoint();
                return;
            }
        }
    
        // Add the new waypoint if it's unique
        waypoints.add(newWaypoint);
        updateTextFields();
        initWaypoint();
    }
    
    private void initRoute(Graph graph, List<GeoPosition> pathPoints) {
        // Create a compound painter to combine multiple painters
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();
    
        // Check if there are path points to draw a route
        if (pathPoints != null && !pathPoints.isEmpty()) {
            // Create a RoutePainter with the path points
            RoutePainter routePainter = new RoutePainter(pathPoints);
    
            // Add the RoutePainter to the compound painter
            compoundPainter.addPainter(routePainter);
        } else {
            // Calculate the shortest path if pathPoints are not provided
            GeoPosition source = waypoints.get(0).getPosition();
            GeoPosition destination = waypoints.get(waypoints.size() - 1).getPosition();
            List<GeoPosition> shortestPath = Dijkstra.computeShortestPath(graph, source, destination);
    
            // Create a RoutePainter with the shortest path
            RoutePainter routePainter = new RoutePainter(shortestPath);
    
            // Add the RoutePainter to the compound painter
            compoundPainter.addPainter(routePainter);
        }
    
        // Set the compound painter as the overlay painter on the JXMapViewer
        jXMapViewer.setOverlayPainter(compoundPainter);
    
        // Refresh the map viewer to display the new route
        jXMapViewer.repaint();
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
    
        // Set the compound painter as the overlay painter on the JXMapViewer
        jXMapViewer.setOverlayPainter(compoundPainter);
    
        // Add buttons for each waypoint
        for (MyWaypoint waypoint : waypoints) {
            jXMapViewer.add(waypoint.getButton());
        }
    
        // Refresh the map viewer to display the waypoints
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

        Error = new component.PanelError();
        jXMapViewer = new org.jxmapviewer.JXMapViewer();
        comboMapType = new combo_suggestion.ComboBoxSuggestion();
        jLabel1 = new javax.swing.JLabel();
        txtFrom = new swing.MyTextField();
        jLabel2 = new javax.swing.JLabel();
        txtTo = new swing.MyTextField();

        comboMapType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Open Street", "Virtual Earth", "Hybrid", "Statelite" }));
        comboMapType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboMapTypeActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel1.setText("From");

        jLabel2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("To");

        javax.swing.GroupLayout jXMapViewerLayout = new javax.swing.GroupLayout(jXMapViewer);
        jXMapViewer.setLayout(jXMapViewerLayout);
        jXMapViewerLayout.setHorizontalGroup(
            jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXMapViewerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTo, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 300, Short.MAX_VALUE)
                .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jXMapViewerLayout.setVerticalGroup(
            jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jXMapViewerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(comboMapType, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(389, 389, 389))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
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
    private component.PanelError Error;
    private combo_suggestion.ComboBoxSuggestion comboMapType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private org.jxmapviewer.JXMapViewer jXMapViewer;
    private swing.MyTextField txtFrom;
    private swing.MyTextField txtTo;
    // End of variables declaration//GEN-END:variables
}
