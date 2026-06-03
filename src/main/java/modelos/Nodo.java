package modelos;

import java.io.Serializable;

/**
 *
 * @author AMD 5600G
 */
public class Nodo implements Serializable {
    private int indice;
    private String id;
    private String nombre;
    private double lat;
    private double lng;
    private Arista adyacentes;

    public Nodo(int indice, String id, String nombre, double lat, double lng) {
        this.indice = indice;
        this.id = id;
        this.nombre = nombre;
        this.lat = lat;
        this.lng = lng;
    }

    public void agregarArista(Arista arista){
        if(adyacentes == null){
            adyacentes = arista;
        }else{
            Arista actual = adyacentes;

            while(actual.getNext() != null){
                actual = actual.getNext();
            }

            actual.setNext(arista);
        }
    }

    public int getIndice() {
        return indice;
    }

    public void setIndice(int indice) {
        this.indice = indice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
    
    public Arista getAdyacentes() {
        return adyacentes;
    }
}
