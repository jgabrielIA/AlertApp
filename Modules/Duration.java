package Modules;

/**
 * Created by Admon on 31/01/2017.
 */

public class Duration {

    public String text;
    public int value;

    public Duration(String text, int value) {  // objeto para extraer tiempo a recorrer en la ruta segun informacion del JSON
        this.text = text;
        this.value = value;
    }
}
