package estructuras;

import java.io.Serializable;
import modelos.Nodo;

public class Cola implements Serializable{
    private Nodo[] cola;
    private int inicio;
    private int fin;
    private int size;
    
    private final int defaultSize = 10;
    
    public Cola(){
        cola = new Nodo[defaultSize];
        inicio = 0;
        fin = 0;
        size = 0;
    }    
    
    public void enqueue(Nodo nodo){
        if(size == cola.length){
            resize();
        }
        cola[fin] = nodo;
        fin = (fin + 1) % cola.length;
        size++;
    }
    
    public Nodo dequeue(){
        if(isEmpty()){
            //Error de cola vacia. No deberia pasar
            return null;
        }
        Nodo nodo = cola[inicio];
        cola[inicio] = null;
        inicio = (inicio + 1) % cola.length;
        size--;        
        return nodo;
    }

    private void resize() {
        Nodo[] nuevaCola = new Nodo[cola.length * 2];
        for(int i = 0; i < size; i++){
            nuevaCola[i] = cola[(inicio + i) % cola.length];
        }
        cola = nuevaCola;
        inicio = 0;
        fin = size;
    }
    
    public boolean isEmpty(){
        return size==0;
    }
    
    public int size(){
        return size;
    }
    
    public void clear(){
        for(int i = 0; i < cola.length; i++){
            cola[i] = null;
        }
        inicio = 0;
        fin = 0;
        size = 0;
    }
    
    public Nodo[] toArray(){
        Nodo[] resultado = new Nodo[size];

        for(int i = 0; i < size; i++){
            resultado[i] = cola[(inicio + i) % cola.length];
        }

        return resultado;
    }
}