package Modules;

/**
 * Created by Admon on 31/01/2017.
 */

public class Distance {

    public String text;
    public int value;

    public Distance(String text, int value) {  // objeto para extraer distancia a recorrer en la ruta segun informacion del JSON
        this.text = text;
        this.value = value;
    }
}