package form;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import algorithms.DistanceCalculator;
//import algorithms.AStar;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

import algorithms.AStar;
import algorithms.Dijkstra;
import glasspanepopup.GlassPanePopup;
import model.*;
import paint.*;
import waypoint.*;

public class Form_Map extends javax.swing.JPanel {
    private final List<MyWaypoint> waypoints = new ArrayList<>();
    private final WaypointPainter<MyWaypoint> waypointPainter = new WaypointRender();
    private EventWaypoint event;
    private Model_GraphHopper graphHopper;
    private Model_RouteHopper routeHopper;
    private GeoPosition fromPosition = null;
    private GeoPosition toPosition = null;
    private Graph graph;
    private String transportationMode = "car";
    //private DistanceCalculator distanceCal;
    
    public Form_Map() {
        initComponents();
        init();
        setupTextFields();
        setupRadioButtons();
        graphHopper = new Model_GraphHopper();
        routeHopper = new Model_RouteHopper();
        graph = new Graph();
        carOption.setSelected(true);
    }
    private void setupRadioButtons() {
        walkingOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transportationMode = "foot";
            }
        });
        carOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transportationMode = "car";
            }
        });

        truckOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transportationMode = "truck";
            }
        });
    }
        
    private void handleResponse(String response) {
        List<List<GeoPosition>> allPaths = graphHopper.extractPoints(response);

        for (List<GeoPosition> pathPoints : allPaths) {
            double distance = calculateTotalDistance(pathPoints);
            System.out.printf("Distance between points: %.3f km%n", distance);

            int visitedNodesCount = graphHopper.calculateVisitedNodesCount(response);
            System.out.println("Visited Nodes Count: " + visitedNodesCount);

            initRoute(graph, pathPoints);
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
        jXMapViewer.setZoom(10);
        
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

        if (!waypoints.isEmpty()) {
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
            String finalTransportationMode = transportationMode;
            SwingUtilities.invokeLater(() -> {
                try {
                    String routeInfo = graphHopper.getRoute(finalFromPosition, finalToPosition, finalTransportationMode);
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
            if (position == null) {
                return; // Stop further execution if the location is not found
            }

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
            String finalTransportationMode = transportationMode;
            // If both from and to positions are set, fetch the route
            if (fromPosition != null && toPosition != null) {
                // Fetch the route information in a separate thread to avoid blocking the UI
                new Thread(() -> {
                    try {
                        String routeInfo = graphHopper.getRoute(fromPosition, toPosition, finalTransportationMode);
                        String routeCoor = routeHopper.getConnection(fromPosition, toPosition);
                        SwingUtilities.invokeLater(() -> handleResponse(routeCoor));
                        System.out.println();
                        SwingUtilities.invokeLater(() -> handleResponse(routeInfo));

                        // Calculate and print the distance between the two points
                        double distance = graphHopper.calculateDistance(fromPosition, toPosition);
//                        System.out.printf("Distance between points: %.3f km%n", distance);

                        // Print the number of nodes visited
                        System.out.println("Number of nodes visited: " + graphHopper.getVisitedNodesCount());
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

        // Print distance if both fromPosition and toPosition are set
        // if (fromPosition != null && toPosition != null) {
        //     double distance =  graphHopper.calculateDistance(fromPosition, toPosition);
        //     System.out.println("Distance between points: " + distance + " km");
        // }
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
        // Create a waypoint painter and set the waypoints
        WaypointPainter<MyWaypoint> waypointPainter = new WaypointRender();
        Set<MyWaypoint> waypointSet = new HashSet<>(waypoints);
        waypointPainter.setWaypoints(waypointSet);
    
        // Create a compound painter to combine multiple painters
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();
    
        // Add the waypoint painter to the compound painter
        compoundPainter.addPainter(waypointPainter);
    
        // Check if there are path points to draw a route
        if (pathPoints != null && !pathPoints.isEmpty()) {
            // Create a RoutePainter with the path points
            RoutePainter routePainter = new RoutePainter(pathPoints);
    
            // Add the RoutePainter to the compound painter
            compoundPainter.addPainter(routePainter);
        } else {
            // Calculate the shortest path if pathPoints are not provided
            GeoPosition start = waypoints.get(0).getPosition();
            GeoPosition end = waypoints.get(waypoints.size() - 1).getPosition();
            //List<GeoPosition> shortestPath = Dijkstra.computeShortestPath(graph, start, end);
            //List<GeoPosition> shortestPath = AStar.computeShortestPath(graph, start, end);
            // Create a RoutePainter with the shortest path
            //RoutePainter routePainter = new RoutePainter(shortestPath);
    
            // Add the RoutePainter to the compound painter
            //compoundPainter.addPainter(routePainter);
            //System.out.println(distanceCal.calculateDistance(start,end));
            List<GeoPosition> shortestPath = new ArrayList<>();

            int index = getIndex();
            System.out.println("Selected Index: " + index);
            switch (index) {
                case 0:
                    shortestPath = Dijkstra.computeShortestPath(graph, start, end);
                    System.out.println("Choose Dijkstra");
                    break;
                case 1:
                    shortestPath = AStar.computeShortestPath(graph, start, end);
                    System.out.println("Choose A*");
                    break;
                case 2:
                    //shortestPath = DFS.computeShortestPath(graph, start, end);
                    System.out.println("Choose DFS");
                    break;
                default:
                    System.err.println("Unknown algorithm selected.");
                    return;
            }
            RoutePainter routePainter = new RoutePainter(shortestPath);
            // Add the RoutePainter to the compound painter
            compoundPainter.addPainter(routePainter);

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
                double distance = graphHopper.calculateDistance(waypoints.get(i).getPosition(), waypoints.get(j).getPosition());
                graph.addEdge(waypoints.get(i).getPosition(), waypoints.get(j).getPosition(), distance);
                graph.addEdge(waypoints.get(j).getPosition(), waypoints.get(i).getPosition(), distance);
            }
        }
    }
    
    private double calculateTotalDistance(List<GeoPosition> pathPoints) {
        double totalDistance = 0;
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            GeoPosition currentPoint = pathPoints.get(i);
            GeoPosition nextPoint = pathPoints.get(i + 1);
            totalDistance += graphHopper.calculateDistance(currentPoint, nextPoint);
        }
        return totalDistance;
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
        buttonGroup1 = new javax.swing.ButtonGroup();
        jXMapViewer = new org.jxmapviewer.JXMapViewer();
        comboMapType = new combo_suggestion.ComboBoxSuggestion();
        jLabel1 = new javax.swing.JLabel();
        txtFrom = new swing.MyTextField();
        jLabel2 = new javax.swing.JLabel();
        txtTo = new swing.MyTextField();
        comboAlgorithmType = new combo_suggestion.ComboBoxSuggestion();
        panelBorder1 = new swing.PanelBorder();
        walkingOption = new radio_button.RadioButton();
        carOption = new radio_button.RadioButton();
        truckOption = new radio_button.RadioButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

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

        comboAlgorithmType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Dijkstra ", "A*", "DFS" }));
        comboAlgorithmType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboAlgorithmTypeActionPerformed(evt);
            }
        });

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
                .addGroup(jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(comboMapType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(comboAlgorithmType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addGroup(jXMapViewerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(comboAlgorithmType, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(296, Short.MAX_VALUE))
        );

        panelBorder1.setBackground(new java.awt.Color(255, 255, 255));

        buttonGroup1.add(walkingOption);
        walkingOption.setForeground(new java.awt.Color(127, 127, 127));
        walkingOption.setText("Walking");
        walkingOption.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N

        carOption.setBackground(new java.awt.Color(0, 255, 51));
        buttonGroup1.add(carOption);
        carOption.setForeground(new java.awt.Color(127, 127, 127));
        carOption.setText("Car");
        carOption.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N

        truckOption.setBackground(new java.awt.Color(255, 51, 51));
        buttonGroup1.add(truckOption);
        truckOption.setForeground(new java.awt.Color(127, 127, 127));
        truckOption.setText("Truck");
        truckOption.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/walking (1).png"))); // NOI18N

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/car.png"))); // NOI18N

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cargo-truck.png"))); // NOI18N

        javax.swing.GroupLayout panelBorder1Layout = new javax.swing.GroupLayout(panelBorder1);
        panelBorder1.setLayout(panelBorder1Layout);
        panelBorder1Layout.setHorizontalGroup(
            panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBorder1Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(walkingOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(70, 70, 70)
                .addComponent(carOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(70, 70, 70)
                .addComponent(truckOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelBorder1Layout.setVerticalGroup(
            panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelBorder1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelBorder1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(walkingOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(carOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(truckOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jXMapViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelBorder1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jXMapViewer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(5, 5, 5)
                .addComponent(panelBorder1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    int index = 0;
    private void comboAlgorithmTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboAlgorithmTypeActionPerformed
        index = comboAlgorithmType.getSelectedIndex();
        // Just to see which algorithm is selected
        System.out.println("Selected Algorithm Index: " + index);

    }//GEN-LAST:event_comboAlgorithmTypeActionPerformed
    private int getIndex(){
        return index;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.PanelError Error;
    private javax.swing.ButtonGroup buttonGroup1;
    private radio_button.RadioButton carOption;
    private combo_suggestion.ComboBoxSuggestion comboAlgorithmType;
    private combo_suggestion.ComboBoxSuggestion comboMapType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private org.jxmapviewer.JXMapViewer jXMapViewer;
    private swing.PanelBorder panelBorder1;
    private radio_button.RadioButton truckOption;
    private swing.MyTextField txtFrom;
    private swing.MyTextField txtTo;
    private radio_button.RadioButton walkingOption;
    // End of variables declaration//GEN-END:variables
}
