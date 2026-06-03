package umg.proyectofinal.simuladorgps;

import algoritmos.Algoritmos;
import estructuras.Cola;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import modelos.Arista;
import modelos.Grafo;
import modelos.Nodo;

public class PrimaryController {
    Algoritmos algoritmos = new Algoritmos();
    Grafo g;
    Nodo[] ruta;       
    Cola recorrido = new Cola();
    
    @FXML
    private Slider speedSlider;
    
    @FXML
    private TextFlow routeTextFlow;
    
    @FXML
    private WebView webView = new WebView();

    @FXML
    private ComboBox<String> rutaComboBox;
    
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
        rutaComboBox.getItems().addAll(
            "Ruta 1",
            "Ruta 2",
            "Ruta 3"
        );
    }
    
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    
    @FXML
    private void loadNodos(){
        String rutaE = rutaComboBox.getValue().replaceAll("\\D+", "");
        if(rutaE.isEmpty()){
            return;
        }
        loadRuta(rutaE);
        mostrarNodos();
    }
    
    @FXML
    private void cargarRutaMasCorta(){
        loadDikjstra();
        mostrarParadas();
        startSimulation();
    }
    
    @FXML
    private void guardarRuta() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Guardar ruta");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Archivos DAT",
                "*.dat"
            )
        );

        File archivo = fileChooser.showSaveDialog(
            webView.getScene().getWindow()
        );

        if(archivo == null){
            return;
        }

        try(
            ObjectOutputStream out =
                new ObjectOutputStream(
                    new FileOutputStream(archivo)
                )
        ){
            out.writeObject(recorrido);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    
    @FXML
    private void cargarRuta() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir ruta");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Archivos DAT",
                "*.dat"
            )
        );
        File archivo = fileChooser.showOpenDialog(
            webView.getScene().getWindow()
        );

        if(archivo == null){
            return;
        }
        try(
            ObjectInputStream in =
                new ObjectInputStream(
                    new FileInputStream(archivo)
                )
        ){
            recorrido = (Cola) in.readObject();
            ruta = recorrido.toArray();
            mostrarRutaGuardada(ruta);
            startSimulation();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void loadDikjstra(){
        recorrido.clear();
        WebEngine engine = webView.getEngine();
        Nodo origen = g.getNodoByNombre("Plaza Tigo Torre 3");
        Nodo destino = g.getNodoByNombre("UMG Antigua Guatemala");
        
        ruta = algoritmos.dijkstra(g, origen.getId(), destino.getId());
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
            "{color:'gold', weight:8}).addTo(graphLayer);"
        );
        for(Nodo n : ruta){
            recorrido.enqueue(n);
        }
    }
    
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
            int paradaActual = i;
            Platform.runLater(() -> {
                actualizarParadaActual(paradaActual);
            });
            Nodo actual = ruta[i];
            Nodo siguiente = ruta[i + 1];

            double lat1 = actual.getLat();
            double lon1 = actual.getLng();

            double lat2 = siguiente.getLat();
            double lon2 = siguiente.getLng();

            double incremento = 0.002 * speedSlider.getValue();

            for(double t = 0; t <= 1; ){
                double lat = lat1 + (lat2 - lat1) * t;
                double lon = lon1 + (lon2 - lon1) * t;

                Platform.runLater(() -> {
                    webView.getEngine().executeScript(
                        "moverVehiculo(" + lat + "," + lon + ");"
                    );
                });

                try {
                    Thread.sleep(16);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                t += 0.002 * speedSlider.getValue();
            }
        }
    }).start();
    }
    
    private void mostrarNodos(){
        if(g == null){
            return;
        }
        WebEngine engine = webView.getEngine();
        engine.executeScript("limpiarMapa();");

        for(Nodo n : g.getAllNodos()){
            engine.executeScript(
                "agregarMarcador(" +
                n.getLat() + "," +
                n.getLng() + "," +
                "'" + n.getNombre() + "'" +
                ");"
            );
            Arista arista = n.getAdyacentes();
            while(arista != null){
                engine.executeScript(
                    "agregarArista(" +
                    n.getLat() + "," +
                    n.getLng() + "," +
                    arista.getDestino().getLat() + "," +
                    arista.getDestino().getLng() +
                    ");"
                );
                arista = arista.getNext();
            }
        }
    }
    
    private void mostrarRutaGuardada(Nodo[] ruta){
        WebEngine engine = webView.getEngine();
        engine.executeScript("limpiarMapa();");
        for(Nodo n : ruta){
            engine.executeScript(
                "agregarMarcador(" +
                n.getLat() + "," +
                n.getLng() + "," +
                "'" + n.getNombre() + "'" +
                ");"
            );
        }
        StringBuilder puntos = new StringBuilder();
        for(Nodo n : ruta){
            puntos.append("[")
                  .append(n.getLat())
                  .append(",")
                  .append(n.getLng())
                  .append("],");
        }
        engine.executeScript(
            "L.polyline([" + puntos + "],{color:'gold',weight:8}).addTo(graphLayer);"
        );
    }
    
    private void mostrarParadas(){
        routeTextFlow.getChildren().clear();

        for(int i = 0; i < ruta.length; i++){
            Text txt = new Text(
                (i + 1) + ". " +
                ruta[i].getNombre() +
                "\n"
            );

            txt.setFont(
                Font.font(
                    "Segoe UI",
                    FontWeight.NORMAL,
                    14
                )
            );

            txt.setFill(Color.web("#d0d0d0"));

            routeTextFlow.getChildren().add(txt);
        }
    }
    
    private void actualizarParadaActual(int indice){
        for(int i = 0; i < routeTextFlow.getChildren().size(); i++){
            Text txt = (Text) routeTextFlow.getChildren().get(i);
            if(i == indice){
                txt.setFill(Color.web("#FFD54F"));
                txt.setStyle(
                    "-fx-font-weight:bold;"
                );
            }
            else{
                txt.setFill(Color.web("#B0BEC5"));
                txt.setStyle("");
            }
        }
    }
    
    private void loadRuta(String ruta){
       g = algoritmos.cargarRuta(ruta);
    }
}
