package estructuras;

import java.io.Serializable;
import modelos.Nodo;

/**
 *
 * @author AMD 5600G
 */
public class TablaHash implements Serializable {
    
    //Nodo de la hash table
    private class NodoHash implements Serializable{
        String idNodo;
        Nodo nodo;
        NodoHash next;
        
        public NodoHash(String idNodo, Nodo nodo){
            this.idNodo = idNodo;
            this.nodo = nodo;
        }
    }
    
    private NodoHash[] buckets;
    private int numBuckets;
    private int size;
    
    public TablaHash(){    
        numBuckets = 10;
        size = 0;
        buckets = new NodoHash[numBuckets];     
    }
    
    public int size(){ 
        return size; 
    }
    
    public boolean isEmpty(){ 
        return size() == 0; 
    }
      
    // Funcion hash para encontrar el index de una llave
    private int getBucketIndex(String id){
        return Math.abs(id.hashCode() % numBuckets);
    }

    public Nodo remove(String id){
        int bucketIndex = getBucketIndex(id);
        NodoHash head = buckets[bucketIndex];

        NodoHash prev = null;
        while (head != null) {
            if (head.idNodo.equals(id))
                break;
            prev = head;
            head = head.next;
        }

        if (head == null)
            return null;

        size--;

        if (prev != null)
            prev.next = head.next;
        else
            buckets[bucketIndex] = head.next;

        return head.nodo;
    }

    public Nodo get(String id){
        int bucketIndex = getBucketIndex(id);
        NodoHash head = buckets[bucketIndex];

        while (head != null) {
            if (head.idNodo.equals(id))
                return head.nodo;
            head = head.next;
        }

        return null;
    }

    public void add(String id, Nodo valor){
        int bucketIndex = getBucketIndex(id);
        NodoHash head = buckets[bucketIndex];

        while (head != null) {
            if (head.idNodo.equals(id)) {
                head.nodo = valor;
                return;
            }
            head = head.next;
        }

        size++;
        head = buckets[bucketIndex];
        NodoHash newNode
            = new NodoHash(id, valor);
        newNode.next = head;
        buckets[bucketIndex] = newNode;

        if ((1.0 * size) / numBuckets >= 0.7) {
            NodoHash[] temp = buckets;
            buckets = new NodoHash[2 * numBuckets];
            numBuckets = 2 * numBuckets;
            size = 0;
            for (NodoHash headNode : temp) {
                while (headNode != null) {
                    add(headNode.idNodo, headNode.nodo);
                    headNode = headNode.next;
                }
            }
        }
    }

    public Nodo getNodoPorIndice(int indice){
        for (NodoHash actual : buckets) {
            while(actual != null){
                if(actual.nodo.getIndice() == indice){
                    return actual.nodo;
                }
                actual = actual.next;
            }
        }
        return null;
    }
    
    public Nodo[] getAllNodos() {
        Nodo[] resultado = new Nodo[size];
        int i = 0;
        for (NodoHash bucket : buckets) {
            NodoHash actual = bucket;
            while (actual != null) {
                resultado[i++] = actual.nodo;
                actual = actual.next;
            }
        }
        return resultado;
    }
}
