/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

import estructuras.TablaHash;
import algoritmos.Algoritmos;

/**
 *
 * @author AMD 5600G
 */
public class Grafo {
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
    
    public Nodo[] getAllNodos(){
        return nodos.getAllNodos();
    }
}
