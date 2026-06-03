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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    private Random random = new Random();
    
    @FXML
    private Slider speedSlider;
    
    @FXML
    private TextField nombreNodoField;

    @FXML
    private TextField latField;

    @FXML
    private TextField lngField;
    
    @FXML
    private TextFlow routeTextFlow;
    
    @FXML
    private WebView webView = new WebView();
    
    @FXML
    private TextArea txtHeader;
    
    @FXML
    private ScrollPane routeScrollPane;

    @FXML
    private ComboBox<String> rutaComboBox , origenComboBox, destinoComboBox, buscarComboBox , destComboBox, orgComboBox;
    
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
            "Ruta 3",
            "Ruta 4",
            "Ruta 5",
            "Ruta 6"
        );
    }
    
    @FXML
    private void loadNodos(){
        String rutaE = rutaComboBox.getValue().replaceAll("\\D+", "");
        if(rutaE.isEmpty()){
            return;
        }
        loadRuta(rutaE);
        actualizarInterfaz();
    }
    
    @FXML
    private void cargarRutaMasCorta(){
        loadDikjstra();
        mostrarParadas();
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
            out.writeObject(g);
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
            g = (Grafo) in.readObject();
            mostrarNodos();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    @FXML
    private void simulacion(){
        startSimulation();
    }
    
    @FXML
    private void buscarNodo(){
        String nombre = buscarComboBox.getValue();
        Nodo nodo = g.getNodoByNombre(nombre);

        if(nodo == null){
            return;
        }

        enfocarNodo(nodo);
    }
    
    @FXML
    private void eliminarNodo() {
        String nombre = buscarComboBox.getValue();

        if(nombre == null || nombre.isEmpty()){
            return;
        }

        Nodo nodo = g.getNodoByNombre(nombre);

        if(nodo == null){
            return;
        }

        g.eliminarNodo(nodo);

        actualizarInterfaz();
    }
    
    @FXML
    private void agregarNodo() {
        String nombre = nombreNodoField.getText();
        if(nombre == null || nombre.isEmpty()){
            return;
        }
        double lat = Double.parseDouble(latField.getText());
        double lng = Double.parseDouble(lngField.getText());
        int indice = generarIndice();
        String id = generarId();
        
        Nodo nuevo = new Nodo(indice, id, nombre, lat, lng);
        g.agregarNodo(nuevo);
        actualizarInterfaz();
    }
    
    @FXML
    private void agregarArista(){
        String origenName = orgComboBox.getValue();
        String destinoName = destComboBox.getValue();

        if(origenName == null || destinoName == null){
            return;
        }

        Nodo origen = g.getNodoByNombre(origenName);
        Nodo destino = g.getNodoByNombre(destinoName);

        if(origen == null || destino == null){
            return;
        }

        g.conectarNodos(origen.getId(), destino.getId());

        actualizarInterfaz();
    }
    
    private void loadDikjstra(){
        recorrido.clear();
        WebEngine engine = webView.getEngine();
        engine.executeScript("limpiarRuta();");
        Nodo origen = g.getNodoByNombre(origenComboBox.getValue());
        Nodo destino = g.getNodoByNombre(destinoComboBox.getValue());
        
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
            "{color:'gold', weight:8}).addTo(routeLayer);"
        );
        for(Nodo n : ruta){
           
            recorrido.enqueue(n);
        }
    }
    
    private void startSimulation(){
        updateHeader();
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
            Platform.runLater(() -> {
                webView.getEngine().executeScript(
                    "moverVehiculo(" +
                    lat2 + "," +
                    lon2 +
                    ");"
                );
            });
        }
        Platform.runLater(() -> {
            actualizarParadaActual(ruta.length - 1);
        });
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
        initComboBoxes(origenComboBox);
        initComboBoxes(destinoComboBox);
        initComboBoxes(buscarComboBox);
        initComboBoxes(destComboBox);
        initComboBoxes(orgComboBox);
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
                txt.setFill(Color.web("blue"));
                txt.setStyle(
                    "-fx-font-weight:bold;"
                );
                double y = txt.getBoundsInParent().getMinY();
                double alturaContenido =
                    routeTextFlow.getBoundsInLocal().getHeight();

                routeScrollPane.setVvalue(
                    y / alturaContenido
                );
            }
            else{
                txt.setFill(Color.web("black"));
                txt.setStyle("");
            }
        }
    }
    
    private void loadRuta(String ruta){
       g = algoritmos.cargarRuta(ruta);
    }
    
    private void initComboBoxes(ComboBox<String> comboBox){
        ObservableList<String> todosNodos = FXCollections.observableArrayList();

        for(Nodo n : g.getAllNodos()){
            todosNodos.add(n.getNombre());
        }
        FilteredList<String> filtrados = new FilteredList<>(todosNodos, p -> true);
        comboBox.setItems(filtrados);
        comboBox.setEditable(true);
        comboBox.getEditor().textProperty().addListener(
            (obs, oldText, newText) -> {
                filtrados.setPredicate(nombre ->
                    nombre.toLowerCase()
                          .contains(newText.toLowerCase())
                );
            }
        );
        comboBox.getSelectionModel().clearSelection();
    }
    
    private void enfocarNodo(Nodo nodo){
        webView.getEngine().executeScript(
            "hacerZoom(" +
            nodo.getLat() + "," +
            nodo.getLng() +
            ");"
        );
    }
    
    private void actualizarInterfaz(){
        mostrarNodos();
        txtHeader.setText("");
        routeTextFlow.getChildren().clear();
    }
    
    private int generarIndice() {
        List<Integer> indices = new ArrayList<>();
        for (Nodo n : g.getAllNodos()) {
            indices.add(n.getIndice());
        }
        Collections.sort(indices);
        int expected = 0;
        for (int actual : indices) {
            if (actual > expected) {
                // gap found
                return expected;
            }
            if (actual == expected) {
                expected++;
            }
        }
        // no gaps found → append at end
        return expected;
    }

    private String generarId(){
        String id;
        do{
            id = "N" + random.nextInt(1000000);
        }while(existeId(id));   
        return id;
    }
    
    private boolean existeId(String id){
        for(Nodo n : g.getAllNodos()){
            if(n.getId().equals(id)){
                return true;
            }
        }
        return false;
    }
    
    private void updateHeader(){
        Nodo origen = g.getNodoByNombre(origenComboBox.getValue());
        Nodo destino = g.getNodoByNombre(destinoComboBox.getValue());
        
        String desde = origen.getNombre();
        String hasta = destino.getNombre();
        
        double km = 0;
        for (int i = 0; i < ruta.length - 1; i++) {
            Nodo actual = ruta[i];
            Nodo siguiente = ruta[i + 1];

            Arista arista = actual.getAdyacentes();

            while (arista != null) {
                if (arista.getDestino().equals(siguiente)) {
                    km += arista.getPeso();
                    break;
                }
                arista = arista.getNext();
            }
        }
        
        String rutaMain = rutaComboBox.getValue();
        if(rutaMain == null){
            rutaMain = "Ruta especial";
        }
         txtHeader.setText(rutaMain +"\n" + desde + " -> " + hasta + "\n" + "Distancia recorrida: " + String.format("%.2f km", km));
    }
}
