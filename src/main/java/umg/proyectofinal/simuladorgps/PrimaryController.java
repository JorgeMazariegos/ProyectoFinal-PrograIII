package umg.proyectofinal.simuladorgps;

import algoritmos.Algoritmos;
import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import modelos.Arista;
import modelos.Grafo;
import modelos.Nodo;

public class PrimaryController {
    Algoritmos algoritmos = new Algoritmos();
    Grafo g;
    Nodo[] ruta;
    
    @FXML
    private WebView webView = new WebView();

    @FXML
    public void initialize(){
        WebEngine engine = webView.getEngine();
        URL url = getClass().getResource("/mapa.html");
        engine.load(url.toExternalForm());
        webView.getEngine().getLoadWorker().stateProperty().addListener(
            (obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    webView.getEngine().executeScript(
                        "setTimeout(function() { map.invalidateSize(); }, 500);"
                    );
                }
            }
        );
    }
    
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    
    @FXML
    private void loadNodos(){
        WebEngine engine = webView.getEngine();
        loadRuta("2");
        for(Nodo n : g.getAllNodos()){
            engine.executeScript(
                "L.marker(["+ n.getLat() +","+ n.getLng() +"])" +
                ".addTo(map)" +
                ".bindPopup('"+ n.getNombre() +"');"
            );
            Arista arista = n.getAdyacentes();
            while(arista != null){
                engine.executeScript(
                    "L.polyline([" +
                    "["+n.getLat()+","+n.getLng()+"]," +
                    "["+arista.getDestino().getLat()+","+arista.getDestino().getLng()+"]" +
                    "]).addTo(map);"
                );
                arista = arista.getNext();
            }
        }
    }
    
    @FXML
    private void loadDikjstra(){
        WebEngine engine = webView.getEngine();
        ruta = algoritmos.dijkstra(g, "R2_00", "R2_99");
        StringBuilder puntos = new StringBuilder();
        for(Nodo n : ruta){
            puntos.append("[")
                  .append(n.getLat())
                  .append(",")
                  .append(n.getLng())
                  .append("],");
        }
        engine.executeScript(
            "L.polyline(" +
            "[" + puntos + "]," + 
            "{color:'gold', weight:8}).addTo(map);"
        );
    }
    
    @FXML
    private void startSimulation(){
        new Thread(() -> {
            Platform.runLater(() -> {
                webView.getEngine().executeScript(
                    "hacerZoom(" +
                    ruta[0].getLat() + "," +
                    ruta[0].getLng() +
                    ");"
                );
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for(int i = 0; i < ruta.length - 1; i++){
                Nodo actual = ruta[i];
                Nodo siguiente = ruta[i + 1];
                double lat1 = actual.getLat();
                double lon1 = actual.getLng();

                double lat2 = siguiente.getLat();
                double lon2 = siguiente.getLng();
                for(int paso = 0; paso <= 20; paso++){
                    double t = paso / 20.0;
                    double lat = lat1 + (lat2 - lat1) * t;
                    double lon = lon1 + (lon2 - lon1) * t;
                    Platform.runLater(() -> {
                        webView.getEngine().executeScript(
                            "moverVehiculo(" + lat + "," + lon + ");"
                        );
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        System.getLogger(PrimaryController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
            }
        }).start();
    }
    
    private void loadRuta(String ruta){
       g = algoritmos.cargarRuta(ruta);
    }
}
