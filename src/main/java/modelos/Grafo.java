/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

import estructuras.TablaHash;
import algoritmos.Algoritmos;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author AMD 5600G
 */
public class Grafo implements Serializable {
    Algoritmos algoritmos = new Algoritmos();
    private final TablaHash nodos;
    
    public Grafo(){
        nodos = new TablaHash();
    }
    
    public void agregarNodo(Nodo nodo){
        nodos.add(nodo.getId(), nodo);
    }
    
    public void conectarNodos(String origen, String destino){
        Nodo desde = nodos.get(origen);
        Nodo hasta = nodos.get(destino);
        if(desde != null && hasta != null){
            double lat1 = desde.getLat();
            double lon1 = desde.getLng();
            double lat2 = hasta.getLat();
            double lon2 = hasta.getLng();                
            double km = algoritmos.calcularDistancia(lat1, lon1, lat2, lon2);
            Arista arista = new Arista(hasta, km);
            desde.agregarArista(arista);
        }
    }
    
    public Nodo getNodo(String id){
        return nodos.get(id);
    }
    
    public Nodo getNodoPorIndice(int indice){
        return nodos.getNodoPorIndice(indice);
    }
    
    public int getTotalNodos(){
        return nodos.size();
    }
    
    public void mostrarDist(String origen, String destino){
        Nodo desde = nodos.get(origen);
        Nodo hasta = nodos.get(destino);
        double lat1 = desde.getLat();
        double lon1 = desde.getLng();
        double lat2 = hasta.getLat();
        double lon2 = hasta.getLng();                
        double km = algoritmos.calcularDistancia(lat1, lon1, lat2, lon2);
        System.out.println("---------- DISTANCIA ENTRE DOS PUNTOS -----------");
        System.out.println(km + " km");
    }
    
    public Nodo getNodoByNombre(String nombre){
        for(Nodo n: nodos.getAllNodos()){
            if(n.getNombre().trim().equals(nombre.trim())){
                return n;
            }
        }
        return null;
    }
    
    public Nodo[] getAllNodos(){
        return nodos.getAllNodos();
    }
    
    public void eliminarNodo(Nodo nodoEliminar){
        if(nodoEliminar == null){
            return;
        }

        // Remove incoming edges
        for(Nodo n : getAllNodos()){
            eliminarAristasHacia(n, nodoEliminar);
        }

        // Remove node from graph
        nodos.remove(nodoEliminar.getId());
    }

    private void eliminarAristasHacia(Nodo origen,Nodo destinoEliminar){
        Arista actual = origen.getAdyacentes();
        Arista anterior = null;

        while(actual != null){

            if(actual.getDestino() == destinoEliminar){

                if(anterior == null){
                    origen.setAdyacentes(
                        actual.getNext()
                    );
                }
                else{
                    anterior.setNext(
                        actual.getNext()
                    );
                }
            }
            else{
                anterior = actual;
            }

            actual = actual.getNext();
        }
    }
}
