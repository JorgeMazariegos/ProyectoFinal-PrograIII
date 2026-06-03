package umg.proyectofinal.simuladorgps;

import algoritmos.Algoritmos;
import java.io.IOException;
import java.util.EnumSet;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import modelos.Arista;
import modelos.Grafo;
import modelos.Nodo;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewPanel;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.javafx.FxGraphRenderer;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.camera.Camera;
import org.graphstream.ui.view.util.InteractiveElement;

public class SecondaryController {
    Algoritmos algoritmos = new Algoritmos();
    Grafo g;
    Nodo[] ruta;

    @FXML
    private AnchorPane graphPane;
    
    @FXML
    public void initialize(){
        loadRuta("1");
        mostrarGraphStream(g);
        
    }
    
    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
    
    public Graph construirGraphStream(Grafo grafo){
        Graph graph = new SingleGraph("GPS");
        Nodo[] nodos = grafo.getAllNodos();
        // Create nodes
        for(Nodo nodo : nodos){
            Node gsNode = graph.addNode(nodo.getId());
            gsNode.setAttribute(
                "ui.label",
                nodo.getNombre()
            );
        }
        // Create edges
        for(Nodo nodo : nodos){
            Arista arista = nodo.getAdyacentes();
            while(arista != null){
                Nodo destino = arista.getDestino();
                String edgeId =
                    nodo.getId() + "_" +
                    destino.getId();
                if(graph.getEdge(edgeId) == null){
                    graph.addEdge(
                        edgeId,
                        nodo.getId(),
                        destino.getId(),
                        true
                    );
                }
                arista = arista.getNext();
            }
        }
        return graph;
    }
    
    public void mostrarGraphStream(Grafo grafo){
        final boolean[] draggingNode = {false};
        final double[] lastMouse = new double[2];
        
        Graph graph = construirGraphStream(grafo);
        graph.setAttribute(
            "ui.stylesheet",
            "node {" +
            "   fill-color: blue;" +
            "   size: 20px;" +
            "   text-size: 12;" +
            "   text-alignment: above;" +
            "}" +
            "edge {" +
            "   fill-color: gray;" +
            "   size: 2px;" +
            "}"
        );
        FxViewer viewer = new FxViewer(
                graph,
                Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD
        );
        
        
        FxViewPanel viewPanel = (FxViewPanel) viewer.addDefaultView(false);
        viewPanel.setFocusTraversable(true);
        viewer.enableAutoLayout();
        viewPanel.requestFocus();
        viewPanel.setOnMousePressed(event -> {

            lastMouse[0] = event.getX();
            lastMouse[1] = event.getY();
        });
        
        viewPanel.setOnMousePressed(event -> {

            lastMouse[0] = event.getX();
            lastMouse[1] = event.getY();

            GraphicElement element =
                viewPanel.findGraphicElementAt(
                    EnumSet.of(InteractiveElement.NODE),
                    event.getX(),
                    event.getY()
                );

            draggingNode[0] = (element != null);
        });
        
        // Zoom
        viewPanel.setOnScroll(event -> {

            double zoom = viewPanel.getCamera().getViewPercent();

            if (event.getDeltaY() > 0) {
                zoom *= 0.9;
            } else {
                zoom *= 1.1;
            }

            viewPanel.getCamera().setViewPercent(zoom);
        });

        // Drag / Pan
        viewPanel.setOnMouseDragged(event -> {
            // If dragging a node, do NOT move camera
            if(draggingNode[0]){
                return;
            }
            double dx = event.getX() - lastMouse[0];
            double dy = event.getY() - lastMouse[1];

            Camera camera = viewPanel.getCamera();

            Point3 center = camera.getViewCenter();

            double zoom = camera.getViewPercent();

            double sensitivity = 0.1 * zoom;

            camera.setViewCenter(
                center.x - dx * sensitivity,
                center.y + dy * sensitivity,
                center.z
            );

            lastMouse[0] = event.getX();
            lastMouse[1] = event.getY();
        });
        
        viewPanel.setOnMouseReleased(event -> {
            draggingNode[0] = false;
        });
        
        graphPane.getChildren().clear();
        graphPane.getChildren().add(viewPanel);
        AnchorPane.setTopAnchor(viewPanel, 0.0);
        AnchorPane.setBottomAnchor(viewPanel, 0.0);
        AnchorPane.setLeftAnchor(viewPanel, 0.0);
        AnchorPane.setRightAnchor(viewPanel, 0.0);
    }
    
    private void loadRuta(String ruta){
       g = algoritmos.cargarRuta(ruta);
    }
}