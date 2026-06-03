/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

import java.io.Serializable;

/**
 *
 * @author AMD 5600G
 */
public class Arista implements Serializable{
    Nodo destino;
    double peso;   
    Arista next;

    public Arista(Nodo destino, double peso) {
        this.destino = destino;
        this.peso = peso;
    }

    public Arista getNext() {
        return next;
    }

    public void setNext(Arista next) {
        this.next = next;
    }

    public Nodo getDestino() {
        return destino;
    }

    public void setDestino(Nodo destino) {
        this.destino = destino;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }
    
    
}
