package Modules;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;


/**
 * Created by Admon on 31/01/2017.
 */

// Objeto para extraer informacion de la ruta segun archivo JSON que proporciona google

public class Route {

    public Distance distance;

    public Duration duration;

    public String endAddress;

    public LatLng endLocation;

    public String startAddress;

    public LatLng startLocation;

    public List<LatLng> points;
}