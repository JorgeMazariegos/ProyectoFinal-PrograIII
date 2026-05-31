package algoritmos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import modelos.Arista;
import modelos.Grafo;
import modelos.Nodo;

/**
 *
 * @author AMD 5600G
 */
public class Algoritmos {
    
    public Grafo cargarRuta(){
        Grafo grafo = new Grafo();
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("rutas/ruta1_nodos.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String linea;
            while((linea = br.readLine()) != null){
                String[] datos = linea.split(",");
                int indice = Integer.parseInt(datos[0]);
                String id = datos[1];
                double lat = Double.parseDouble(datos[2]);
                double lon = Double.parseDouble(datos[3]);
                String nombre = datos[4];
                Nodo nodo = new Nodo(indice, id, nombre, lat, lon);
                grafo.agregarNodo(nodo);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        cargarConexiones(grafo);
        
        return grafo;
    }
    
    private void cargarConexiones(Grafo grafo){
         try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("rutas/ruta1_aristas.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String linea;
            while((linea = br.readLine()) != null){
                String[] datos = linea.trim().split("\\s+");
                String origen = datos[1];
                String destino = datos[2];
                grafo.conectarNodos(origen, destino);
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2){
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        
        double a = Math.pow(Math.sin(dLat / 2), 2) + 
                   Math.pow(Math.sin(dLon / 2), 2) * 
                   Math.cos(lat1) * 
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }
    
    public void dijkstra(Grafo grafo, String origenId, String destinoId){
        int total = grafo.getTotalNodos();
        double[] distancias = new double[total];
        boolean[] visitados = new boolean[total];
        Nodo[] anteriores = new Nodo[total];
        
        for(int i = 0; i < total; i++){
            distancias[i] = Double.MAX_VALUE;
            visitados[i] = false;
            anteriores[i] = null;
        }
        
        Nodo origen = grafo.getNodo(origenId);
        Nodo destino = grafo.getNodo(destinoId);
        
        distancias[origen.getIndice()] = 0;

        // Repetimos una vez por cada nodo
        for(int i = 0; i < total; i++){
            // Buscar el nodo NO visitado
            // con la menor distancia
            int menorIndice = -1;
            double menorDistancia = Double.MAX_VALUE;
            for(int j = 0; j < total; j++){

                if(!visitados[j] &&
                   distancias[j] < menorDistancia){

                    menorDistancia = distancias[j];
                    menorIndice = j;
                }
            }
            // Si no encontro nodo valido, terminar
            if(menorIndice == -1){
                break;
            }
            // Obtener el nodo actual
            Nodo actual = grafo.getNodoPorIndice(menorIndice);
            // Marcar como visitado
            visitados[menorIndice] = true;
            // Recorrer sus aristas/adyacentes
            Arista arista = actual.getAdyacentes();
            while(arista != null){
                Nodo vecino = arista.getDestino();
                int indiceVecino = vecino.getIndice();
                // Si el vecino no ha sido visitado
                if(!visitados[indiceVecino]){
                    // Nueva distancia posible
                    double nuevaDistancia = distancias[menorIndice] + arista.getPeso();
                    // Si encontramos un camino mas corto
                    if(nuevaDistancia < distancias[indiceVecino]){
                        // Actualizar distancia
                        distancias[indiceVecino] = nuevaDistancia;
                        // Guardar desde donde venimos
                        anteriores[indiceVecino] = actual;
                    }
                }
                // Siguiente arista
                arista = arista.getNext();
            }
        }
        int indiceDestino = destino.getIndice();
        System.out.println("===== RUTA MAS CORTA =====");
        System.out.println(
            "Distancia total: "
            + distancias[indiceDestino]
            + " km"
        );
        // Reconstruir camino
        Nodo actual = destino;
        System.out.println("Ruta:");
        while(actual != null){
            System.out.println(
                actual.getId()
                + " - "
                + actual.getNombre()
            );
            actual = anteriores[actual.getIndice()];
        }
    }
}
