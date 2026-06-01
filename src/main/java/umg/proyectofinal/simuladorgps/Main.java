/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package umg.proyectofinal.simuladorgps;

import algoritmos.Algoritmos;
import modelos.Grafo;

/**
 *
 * @author AMD 5600G
 */
public class Main {
    public static void main(String[] args){
        Algoritmos algoritmos = new Algoritmos();
        Grafo g = algoritmos.cargarRuta("1");
        algoritmos.dijkstra(g, "R1_00", "R1_70");
        g.mostrarDist("R1_00", "R1_70");
    }
}
