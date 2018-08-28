package jgabolord.prueba1;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;


/**
 *
 *  ACTIVIDAD PRINCIPAL
 *
 **/

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        DirectionFinderListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {  // Eventos implementados


/**
 *
 *  VARIABLES
 *
 **/

    private GoogleMap mMap;  // Variable , Objeto mapa

    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1972;  // error conexion api google

    TextView centro;  // donde se mustra el icono de la mira para agregar marcador
    TextView vel;  // para ver velocidad en la intefaz
    TextView presi;  // para ver la precicion horizontal o sobre el mapa
    TextView bearin_local;
    TextView bearin_prox;

    ImageButton agregar;  // boton para agregar marcador nuevo a la BD

    RadioGroup reporte;  // para crear el grupo de opciones a reportar
    RadioButton rb;  // define objeto radio buton para establecer
    // su estado = selccionado / deseleccionado = true/false

    boolean centr_mapa = true;  // para que cuando quiero ubicar la mira de
    // agregar no se anime el mapa

    int item_selc = 0;  // cambia segun la opcion seleccionada en el radiogroup

    long add_rama_foto;  // para generar la key nueva = este valor + 1 para agregar
    // objeto nueva_marca en la rama fotoradar
    long add_rama_retn;  // para generar la key nueva = este valor + 1 para agregar
    // objeto nueva_marca en la rama  reten
    long add_rama_accidt;  // para generar la key nueva = este valor + 1 para agregar
    // objeto nueva_marca en la rama acci

    // condicion para dibujar nuevos marcadores:

    int r_foto;
    int r_ret;
    int r_acci;

    // para saber cuantos marcadores ya estan pintados en el mapa:

    long cmdm_foto;
    long cmdm_reten;
    long cmdm_acci;

    // para que no se pueda reportar hasta que se elija que se quiere
    // ver: fotoradar, retes, accidentes o ttodo;

    int hab_report = 0;

    Handler handler;  // contador para eliminar de la BD los retenes y accidentes
    Handler handlerkm;  // contador para revisar fotoradares cercanos

    goalert_dos alertasss;  // proceso en backgraund para generar alertas


    // variables para consulta y escritura de la base de datos:
    // referencia a la rama principal y sub ramas principales:

    DatabaseReference raiz = FirebaseDatabase.getInstance().getReference();

    DatabaseReference fotoradar = raiz.child("fotoradar");
    DatabaseReference acci = raiz.child("acci");
    DatabaseReference reten = raiz.child("reten");

    //  Cliente Api Google:

    GoogleApiClient mGoogleApiClient;  // cliente googleapi
    Location mLastLocation;

    // Para trabajar con el sistema de localizacion GPS

    Location location;
    LocationManager locationManager;


    AlertDialog alert = null;  // cuando no esta activado el GPS y pregunta


    //  Para generar las alertas:

    float speedkm = 0.0f;
    float sentido = 0.0f;
    float distanciaA = 0.0f;  // distancia entre marcador y ubicacion actual
    float precision = 0.0f;  // presicion en metros del gps
    float sentidoA = 0.0f;  // sentido de direccion entre dos ubicaciones
    Location lmarcador = new Location(GPS_PROVIDER);
    Location lactual = new Location(GPS_PROVIDER);
    public String [] copi;
    Location L_actual_timer = new Location(GPS_PROVIDER);

    // para contar las alertas a cero kmh

    int cma = 0;

    // para las alertas sonoras

    int reproducir = 0;
    int play_sound;
    int aux_play_sound;

    MediaPlayer mediaPlayer;

    // para calcular rutas:

    Button btnFindPath;  // boton buscar rutas
    Button btn_ubiact;  // boton para utilizar posicion actual
    private EditText etOrigin;  // ingresar lugar de origen
    private EditText etDestination;  // ingresar lugar de destino
    TextView tempo;  // para ver informacion de distancia de la ruta
    TextView dist;  // tiempo en recorrer la ruta en automovil
    private List<Marker> originMarkers = new ArrayList<>();  // lista con puntos
    // de origen sacadas del json
    private List<Marker> destinationMarkers = new ArrayList<>();  // lista con puntos
    // de destino sacadas del json
    private List<Polyline> polylinePaths = new ArrayList<>();  // lista con los otros
    // marcadores sacados del json
    private ProgressDialog progressDialog;  // espera o barra de progreso mientras calcula ruta
    int mensajes_ruta = 0;

    // para la autenticacion por medio de firebase
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mFirebaseUser;

    // para mostrar datos de usuario en menu desplegable

    TextView usuario_r;
    TextView correo_r;
    ImageView foto_perfil;
    Bitmap fot_perf;  // foto se prepresenta como mapa de bits
    Uri photoUrl;  // Donde esta la foto de usuario


    /**
     *
     * INICIALIZA ACTIVIDAD PRINCIPAL
     *
     **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Ini_Comp();  // carga inicial interfaz
        Bundle datos = this.getIntent().getExtras();

        if(datos != null){
            play_sound = datos.getInt("timer_cero_kph");
            r_foto = datos.getInt("marcadores_foto");
            hab_report = 1;
        }

        // prueba cargar datos al cambiar camara
        if(savedInstanceState!= null){
            play_sound = savedInstanceState.getInt("timer_cero_kph");
            r_foto = savedInstanceState.getInt("marcadores_foto");
        }

        User_Firebase();  // Objeto Usuario de Firebas
        Carga_Mapa();  // verifica conexion a google services y si es correcto carga mapa
        Pregunta_Gps();  // Revisa si el GPS esta activado
        Permisos_A();  // Revisa permisos para consultar la ultima posicion conocida
        Radio_grupo();  // se activa cuando detecta un cambio de estado
        // en alguno de los RadioButtons


        //  Varios listener para los diferentes eventos para un cambio de
        // localizacion o de estado del GPS:
        locationManager.requestLocationUpdates(GPS_PROVIDER, 1, 1, new LocationListener(){

            // "Location location" es la posicion actual
            @Override
            public void onLocationChanged(Location location) {

                try{

                    if (location != null) {  // si la location es diferente de null:

                        speedkm = (((location.getSpeed())*3600)/1000);  // velocidad en km/h
                        sentido = location.getBearing();  // en grados
                        lactual = location;  // posicion actual
                        precision = location.getAccuracy();  // se obtiene la exactitud
                        // estimada de esta ubicacion, en metros.
                        vel.setText("Velocidad: " + speedkm + " Kmph");
                        presi.setText("Presición:" + precision + " m");
                        // bearin_local.setText("Bearing: " + sentido + "  Bearing_to:" + sentidoA);
                        bearin_prox.setText("Play_sound: " + aux_play_sound + "");

                        // para iniciar alertas:

                        if((speedkm > 30.0f)){
                            // && (aux_play_sound == 0)
                            if((aux_play_sound == 0)){

                                //aux_play_sound = 1;
                                cma = 0;
                                alertasss = new goalert_dos();
                                alertasss.execute();
                            }
                        }

                        float zoom = mMap.getCameraPosition().zoom;  // zoom actual:
                        if ((zoom >= 14) && (centr_mapa)){
                            goToLocation(location.getLatitude(), location.getLongitude(), zoom);
                            // actualiza camara en las coordenadas actuales
                            // y aplica zoom, double, double, float
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error posicion antes y despues", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

                // lo que pasa cuando cambia el estado
                Log.d("CAMBIO","en GPS");

            }

            @Override
            public void onProviderEnabled(String provider) {

                // cuando esta activo el proveedor de ubicaciones (GPS)
                Log.d("REANIMAR","ProviderEnabled");

            }

            @Override
            public void onProviderDisabled(String provider) {

                // cuando esta desactiva el proveedor de ubicaciones (GPS)
                Log.d(" hey!"," que paso con el GPS");

            }
        });


        // Grfica marcadores nuevos en el mapa:
        fotoradar.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{

                    if(r_foto == 1) {

                        // averiguar cuantas ramas hay en fotoradar y con un for
                        // obtener los datos para pintar las marcas en el mapa

                        long childrenCount = dataSnapshot.getChildrenCount();
                        // funciona: retorna cantidad de ramas en fotoradar

                        copi = new String[((int)childrenCount) + 1];
                        int cc = 1;

                        while (cc <= childrenCount){
                            marcadores marca = dataSnapshot.child("mf" + String.valueOf(cc))
                                    .getValue(marcadores.class);
                            copi[cc]= marca.getCoor();
                            cc++;
                        }


                        int i = (int) cmdm_foto;

                        while (i <= childrenCount){  // mientras i sea menor o igual al numero
                            // total de ramas den fotoradar:

                            marcadores marca = dataSnapshot.child("mf" + String.valueOf(i))
                                    .getValue(marcadores.class);  // funciona  marca es
                            // un objeto de la clase marcadores

                            // A el objeto marca lee y escribe los tres datos que se requieren
                            // en los marcadores: coordenada, direccion y fecha

                            // el objeto marca se puede utilizar sobre la rama fotoradar
                            // especificando el .chil("mfx")

                            if(marca != null){

                                String [] resulcoor;  // vector para recibir string }
                                // de las coordenadas

                                resulcoor = String_Coord_Marca(marca.getCoor());
                                // se reciben las coordenadas

                                double lati, longi;  // para convertir de string a double

                                lati = Double.parseDouble(resulcoor[0]);
                                // se convierte de string a double
                                longi = Double.parseDouble(resulcoor[1]);

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lati, longi))
                                        .title(marca.getDirec())
                                        .snippet(marca.getFecha())
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.ic_ffoto1))
                                        // icono conservo transparencia png
                                        .anchor(0.5f, 1)
                                        .flat(false));  // aplana el marcador
                                cmdm_foto = i;
                            }
                            i++;  // aumenta contador de ramas
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error escritura en BD", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Firebase", "Error en agrgar a la BD", databaseError.toException());
            }
        });  // evento para pintar
        // en el mapa nuevos elementos foto recien introducidos/tiempo real a la BD:
        reten.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{

                    if(r_ret == 1) {

                        long childrenCount = dataSnapshot.getChildrenCount();
                        long i = cmdm_reten;

                        while (i <= childrenCount){

                            marcadores marca = dataSnapshot.child("rt" + String.valueOf(i))
                                    .getValue(marcadores.class);

                            if (marca != null){

                                String [] resulcoor;
                                resulcoor = String_Coord_Marca(marca.getCoor());

                                double lati, longi;

                                lati = Double.parseDouble(resulcoor[0]);
                                longi = Double.parseDouble(resulcoor[1]);

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lati, longi))
                                        .title(marca.getDirec())
                                        .snippet(marca.getFecha())
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.ic_ppoli2))
                                        .anchor(0.5f, 1)
                                        .flat(false));
                                cmdm_reten = i;
                            }
                            i++;
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error escritura en BD", Toast
                            .LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Firebase", "Error en agrgar a la BD", databaseError.toException());
            }
        });  // evento para pintar
        // en el mapa nuevos elementos reten recien introducidos/tiempo real a la BD:
        acci.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{

                    if(r_acci == 1) {

                        long childrenCount = dataSnapshot.getChildrenCount();
                        long i = cmdm_acci;

                        while (i <= childrenCount){

                            marcadores marca = dataSnapshot.child("ac" + String.valueOf(i))
                                    .getValue(marcadores.class);

                            if(marca != null){

                                String [] resulcoor;
                                resulcoor = String_Coord_Marca(marca.getCoor());

                                double lati, longi;

                                lati = Double.parseDouble(resulcoor[0]);
                                longi = Double.parseDouble(resulcoor[1]);

                                mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(lati, longi))
                                        .title(marca.getDirec())
                                        .snippet(marca.getFecha())
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(R.drawable.ic_aacci4))
                                        .anchor(0.5f, 1)
                                        .flat(false));
                                cmdm_acci = i;
                            }
                            i++;
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error escritura en BD", Toast
                            .LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Firebase", "Error en agrgar a la BD", databaseError.toException());
            }
        });  // evento para pintar
        // en el mapa nuevos elementos accidente introducidos/tiempo real a la BD:


        // Agrega informacion nueva a la base de datos:
        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { insmarcador(); }
        });


        // Boton para introducir posicion actual al calcular ruta:
        btn_ubiact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gestion de permisos
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1){
                    mLastLocation = LocationServices.FusedLocationApi
                            .getLastLocation(mGoogleApiClient);
                    // ultima ubicacion conocida = posicion actual
                    String lati = String.valueOf(mLastLocation.getLatitude());
                    // string del valor latitud
                    String longi = String.valueOf(mLastLocation.getLongitude());
                    // string del valor longitud
                    etOrigin.setText(lati + ", " + longi);
                    // adecuacion de informacion de punto de origen
                }else{
                    ///PERMISO CONCENDIDO
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                        mLastLocation = LocationServices.FusedLocationApi
                                .getLastLocation(mGoogleApiClient);
                        // ultima ubicacion conocida = posicion actual
                        String lati = String.valueOf(mLastLocation.getLatitude());
                        // string del valor latitud
                        String longi = String.valueOf(mLastLocation.getLongitude());
                        // string del valor longitud
                        etOrigin.setText(lati + ", " + longi);
                        // adecuacion de informacion de punto de origen
                        Toast.makeText(getApplicationContext(), "1 Permiso Concedido", Toast
                                .LENGTH_SHORT).show();
                    }
                }
            }
        });


        // Boton para iniciar calculo de ruta:
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { sendRequest(); }
        });
        // inicia solicitud de datos JSON de la ruta entre origen y destino


        // TIMER : Eliminar elementos Reten Y Accidentes de la Base de Datos:

        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // revision de reportes retenes y accidentes:
                // se ejecuta revision una unica vez despues de 15s de ejecutada
                // la aplicacion y luego cada 3 horas
                // si no es un reporte del dia actual se elimina de l base de datos
                // se revisa cada ve que inicie la aplicacion y luego hasta
                // maximo 8 veces en un lapso de 24 horas
                reten.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        try{

                            long childrenCount = dataSnapshot.getChildrenCount();
                            // funciona: retorna cantidad de ramas en reten
                            long i = 1;

                            while (i <= childrenCount){

                                marcadores marca = dataSnapshot.child("rt" + String.valueOf(i))
                                        .getValue(marcadores.class);

                                if(marca != null){

                                    String [] resulfecha;
                                    resulfecha = GetFecha(marca.getFecha());
                                    // devuelve vector string con dia, mes, año

                                    int dia_bd = Integer.parseInt(resulfecha[0]);
                                    int mes_bd = Integer.parseInt(resulfecha[1]);
                                    int ano_bd = Integer.parseInt(resulfecha[2]);

                                    // sacar fecha actual del movil:

                                    Calendar cal = Calendar.getInstance();

                                    int dia = cal.get(Calendar.DATE);
                                    int mes = (cal.get(Calendar.MONTH)+1);
                                    int ano = cal.get(Calendar.YEAR);

                                    String time = "" + cal.get(Calendar.HOUR_OF_DAY) +
                                            ":" + cal.get(Calendar.MINUTE);

                                    String date = "" + cal.get(Calendar.DATE) + "/"
                                            + (cal.get(Calendar.MONTH)+1) + "/"
                                            + cal.get(Calendar.YEAR);

                                    // ubicacion de relleno

                                    String crelleno = "37.804121,-122.429958";
                                    String drelleno = "Fort Mason" + " San Francisco, California";
                                    String frelleno = time + " " + date;

                                    // comparar fecha datos BD y fecha actual dispositivo:

                                    if(ano > ano_bd){
                                        reten.removeValue();  // elimina
                                        marcadores relleno_marca =
                                                new marcadores(crelleno, drelleno, frelleno);
                                        reten.child("rt1").setValue(relleno_marca);
                                        mMap.clear();
                                        // Log.d("mensaje","eliminar año");
                                    }

                                    if((ano == ano_bd) && (mes != mes_bd)){
                                        reten.removeValue();  // elimina
                                        marcadores relleno_marca =
                                                new marcadores(crelleno, drelleno, frelleno);
                                        reten.child("rt1").setValue(relleno_marca);
                                        mMap.clear();
                                        // Log.d("elimina","mes");
                                    }

                                    if((ano == ano_bd) && (mes == mes_bd)&&(dia != dia_bd)){
                                        reten.removeValue();  // elimina
                                        marcadores relleno_marca =
                                                new marcadores(crelleno, drelleno, frelleno);
                                        reten.child("rt1").setValue(relleno_marca);
                                        mMap.clear();
                                        // Log.d("elimina","dia");
                                    }
                                }
                                i++;  // aumenta contador del while
                            }
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Error al actualizar Retenes", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Firebase", "Error en Actualizar la BD", databaseError.toException());
                    }

                });
                // metodo para borrar retenes de la BD con el primer reporte de cada dia
                acci.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        try{

                            long childrenCount = dataSnapshot.getChildrenCount();
                            // funciona: retorna cantidad de ramas en reten
                            long i = 1;

                            while (i <= childrenCount){

                                marcadores marca = dataSnapshot.child("ac" + String.valueOf(i))
                                        .getValue(marcadores.class);

                                if(marca != null){

                                    String [] resulfecha;
                                    resulfecha = GetFecha(marca.getFecha());
                                    // devuelve vector string con dia, mes, año

                                    int dia_bd = Integer.parseInt(resulfecha[0]);
                                    int mes_bd = Integer.parseInt(resulfecha[1]);
                                    int ano_bd = Integer.parseInt(resulfecha[2]);

                                    // sacar fecha actual del movil:

                                    Calendar cal = Calendar.getInstance();

                                    int dia = cal.get(Calendar.DATE);
                                    int mes = (cal.get(Calendar.MONTH)+1);
                                    int ano = cal.get(Calendar.YEAR);

                                    String time = "" + cal.get(Calendar.HOUR_OF_DAY)
                                            + ":"
                                            + cal.get(Calendar.MINUTE);

                                    String date = "" + cal.get(Calendar.DATE)
                                            + "/" + (cal.get(Calendar.MONTH)+1)
                                            + "/" + cal.get(Calendar.YEAR);

                                    // ubicacion de relleno

                                    String crelleno = "37.804121,-122.429958";
                                    String drelleno = "Fort Mason" + " San Francisco, California";
                                    String frelleno = time + " " + date;

                                    // comparar fecha datos BD y fecha actual dispositivo:

                                    if(ano > ano_bd){
                                        acci.removeValue();  // elimina
                                        marcadores relleno_marca =
                                                new marcadores(crelleno, drelleno, frelleno);
                                        acci.child("ac1").setValue(relleno_marca);
                                        mMap.clear();
                                        // Log.d("mensaje","eliminar año");
                                    }

                                    if((ano == ano_bd) && (mes != mes_bd)){
                                        acci.removeValue();  // elimina
                                        marcadores relleno_marca =
                                                new marcadores(crelleno, drelleno, frelleno);
                                        acci.child("ac1").setValue(relleno_marca);
                                        mMap.clear();
                                        // Log.d("elimina","mes");
                                    }

                                    if((ano == ano_bd) && (mes == mes_bd)&&(dia != dia_bd)){
                                        acci.removeValue();  // elimina
                                        marcadores relleno_marca =
                                                new marcadores(crelleno, drelleno, frelleno);
                                        acci.child("ac1").setValue(relleno_marca);
                                        mMap.clear();
                                        // Log.d("elimina","dia");
                                    }
                                }
                                i++;  // aumenta contador del while
                            }
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Error escritura en BD", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("Firebase", "Error en Actualizar la BD", databaseError.toException());
                    }

                });
                // metodo para borrar accidentes de la BD con el primer reporte de cada dia
                Log.d("TIMER: BD Actualizada","Retenes y Accidentes actualizados");
                handler.postDelayed(this, 300000);
                // se ejecuta siempre cada 5 min = 5 * (60000 ms = 1 minuto) = 300000 ms
            }
        };
        handler.postDelayed(runnable, 10000);  // se ejecuta por primera vez a los 10 segundos despues de iniciar la app


        /**
         * FIN DEL ON_CREATE
         */
    }




    /**
     * METODOS:
     */

    /**
     * Inicializa componentes interfaz:
     */
    public void Ini_Comp(){

        centro = (TextView) findViewById(R.id.iccentro);
        // se crea objeto centro que hace refencia al texview con el icono de ubicacion
        agregar = (ImageButton) findViewById(R.id.b_nuevamarca);
        // se crea objeto agregar que hace referencia al boton con el cual se
        // agregaran nuevos marcadores a ala base de datos
        reporte = (RadioGroup) findViewById(R.id.elegirmarca);
        // se crea objeto reporte para elegir el tipo de informacion a añadir en la BD

        vel = (TextView) findViewById(R.id.velocidad);  // ver velocidad actual
        presi = (TextView) findViewById(R.id.presicion);  // ver precision segun gps del dispositivo
        tempo = (TextView) findViewById(R.id.tiempor);  // ver tiempo de ruta
        dist = (TextView) findViewById(R.id.distancia);  // ver distancia de ruta
        bearin_local = (TextView) findViewById(R.id.sentido);
        bearin_prox = (TextView) findViewById(R.id.sentido_a);

        btnFindPath = (Button) findViewById(R.id.btn_ruta);  // boton para iniciar proceso de ruta
        btn_ubiact = (Button) findViewById(R.id.btn_mi_ubi);
        // boton para colocar posicion actual como inicio de la ruta
        etOrigin = (EditText) findViewById(R.id.txt_origen);  // insertar origen
        etDestination = (EditText) findViewById(R.id.txt_destino);  // insertar destino

        // soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        // constructor para los sonidos de las alertas

        speedkm = 0.0f;
        play_sound = 0;
        aux_play_sound = 0;
        mediaPlayer = new MediaPlayer();

        /**
         * despues de inicializar elementos de la interfaz carga los otros elementos
         */

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**
         * establece accion de abrir y cerrar menu desplegable
         */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle( this, drawer, toolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        /**
         * establece menu en  menu desplegable
         */

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);
        // referencia al header o cabecero del navigationView

        usuario_r = (TextView) header.findViewById(R.id.usuario);
        // inicializa textview en el que se publicara el nombre del usuario
        correo_r = (TextView) header.findViewById(R.id.correo);
        //  inicializa textview en el que se publicara el email del usuario
        foto_perfil = (ImageView) header.findViewById(R.id.foto_p);
        // inicializa imageview en el que se publicara foto de perfil

        navigationView.setNavigationItemSelectedListener(this);
        // declara escucha del navigationView

    }


    /**
     * Carga objeto usuario Firebase:
     */
    public void User_Firebase (){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) { goLoginScreen(); }  // Regresa al LoginActivity
        else { // Obtenemos: Nombre, correo, id usuario, u url de la foto de perfil
            String name = mFirebaseUser.getDisplayName();
            usuario_r.setText(name);
            String email = mFirebaseUser.getEmail();
            correo_r.setText(email);
            if (mFirebaseUser.getPhotoUrl() != null) {
                photoUrl = mFirebaseUser.getPhotoUrl();
                Log.d("url foto",photoUrl.toString());
                new carga_imagen().execute();
            }
        }
    }


    /**
     * Carga objeto mapa en el fragment:
     */
    public void Carga_Mapa (){
        try{
            if (servicesOK()) {
                buildGoogleApiClient();
                // construye cliente google api
                if (mGoogleApiClient != null) { mGoogleApiClient.connect(); }
                // conecta cliente google api
                else{ Toast.makeText(this, "Error GoogleApiClient", Toast.LENGTH_SHORT).show(); }
                SupportMapFragment sMapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                // se declara cual es objeto mapa "sMapFragment"
                sMapFragment.getMapAsync(this);
                // se carga objeo mapa en donde quiero que se vea, en este caso en un fragment
                Toast.makeText(this, "Mapa_Ok", Toast.LENGTH_SHORT).show();
                // se muestra mensaje de que el mapa se cargo correctamente

            } else { Toast.makeText(this, "Mapa_Error", Toast.LENGTH_SHORT).show(); }
        }catch (Exception e){ Toast.makeText(this, "Error conexiÓn a Play Services", Toast.LENGTH_SHORT).show(); }
    }


    /**
     * Revisa si el GPS esta activado:
     */
    public void Pregunta_Gps (){

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // pregunta quien provee los datos de geolocalizacion

        if (!locationManager.isProviderEnabled(GPS_PROVIDER)){
            // si el gps no esta activado activa alerta
            AlertNoGps();  // mensaje solicitando activacion del GPD
        }

    }


    /**
     * Alerta de que no hay GPS activado :
     */
    public void AlertNoGps() {

        try{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                    .setCancelable(false)
                    .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            startActivity(new Intent(android.provider.Settings
                                    .ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                            FirebaseAuth.getInstance().signOut();  // cierro firebase
                            Auth.GoogleSignInApi.signOut(mGoogleApiClient);  // cierra google
                            LoginManager.getInstance().logOut();  // cierra facebook
                            goLoginScreen();  // se direge al loginActivity
                        }
                    });
            alert = builder.create();
            alert.show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error con el GPS del dispositivo", Toast
                    .LENGTH_SHORT).show();
        }
    }


    /**
     * Pregunta por ermisos para getLastKnownLocation:
     */
    public void Permisos_A (){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) { goLoginScreen(); }
            // verifica permisos y si no los tiene regresa a menu login
            else { location = locationManager.getLastKnownLocation(GPS_PROVIDER); }
            // guarda ultima lat, lon conocidas
        } else { location = locationManager.getLastKnownLocation(GPS_PROVIDER); }
        // guarda ultima lat, lon conocidas

    }


    /**
     * Lo que hace cada opcion del Radio grupo:
     */
    public void Radio_grupo (){

        reporte.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // se activa cuando detecta un cambio de estado en alguno de los RadioButtons
                try{
                    rb = (RadioButton) group.findViewById(checkedId);  // identifica radio button

                    switch (rb.getId()){  // pregunta caso por caso

                        case R.id.sel_fotorad:

                            etDestination.setVisibility(View.INVISIBLE);
                            // ocultar elementos de crear rutas
                            etOrigin.setVisibility(View.INVISIBLE);
                            btnFindPath.setVisibility(View.INVISIBLE);
                            btn_ubiact.setVisibility(View.INVISIBLE);

                            centro.setVisibility(View.VISIBLE);
                            // para ocultar elementos de agregar marcador
                            agregar.setVisibility(View.VISIBLE);

                            item_selc = 1;  // reportar fotomulta
                            reporte.setVisibility(View.GONE);  // para ocultar radio group

                            break;

                        case R.id.sel_rete:

                            etDestination.setVisibility(View.INVISIBLE);
                            // ocultar elementos de crear rutas
                            etOrigin.setVisibility(View.INVISIBLE);
                            btnFindPath.setVisibility(View.INVISIBLE);
                            btn_ubiact.setVisibility(View.INVISIBLE);

                            centro.setVisibility(View.VISIBLE);
                            // para ocultar elementos de agregar marcador
                            agregar.setVisibility(View.VISIBLE);

                            item_selc = 2;  // reportar reten
                            reporte.setVisibility(View.GONE);  // para ocultar radio group
                            break;

                        case R.id.sel_accidt:

                            etDestination.setVisibility(View.INVISIBLE);
                            // ocultar elementos de crear rutas
                            etOrigin.setVisibility(View.INVISIBLE);
                            btnFindPath.setVisibility(View.INVISIBLE);
                            btn_ubiact.setVisibility(View.INVISIBLE);

                            centro.setVisibility(View.VISIBLE);
                            // para ocultar elementos de agregar marcador
                            agregar.setVisibility(View.VISIBLE);

                            item_selc = 3;  // reportar accidente
                            reporte.setVisibility(View.GONE);  // para ocultar radio group
                            break;
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error en Menu Seleccion de Reporte", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    /**
     * Elementos comunes:
     */

    /**
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
    /*Save your data to be restored here
    Example : outState.putLong("time_state", time); , time is a long variable*/
        super.onSaveInstanceState(outState);
        outState.putInt("timer_cero_kph", play_sound);
        outState.putInt("marcadores_foto", r_foto);
    }


    /** Cuando se cierra la aplicacion **/
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(alert != null) { alert.dismiss (); }

        //alertasss.cancel(true);
        //alertasss.isCancelled();
        //handlerkm.dump(new LogPrinter(Log.DEBUG, "TAG"), "PREFIX");
        // android.os.Process.killProcess(android.os.Process.myPid());
        // Kill the app on click of back button.
        Log.d("Despedida","Chao");
    }


    /** Cuando se pausa la aplicacion **/
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Pausa","La garrotera");
    }


    /** Cuando se re_activa la aplicacion **/
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Despierta","Agua Fria");
    }


    /** Boton back/regresar o salir de la aplicacion:
     * 1. metodo abrir o cerrar menu desplegable
     * 2. ocultar elelmtos ruta
     * 3. ocultar radio group
     */
    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if((centro.getVisibility() == View.VISIBLE)||(agregar.getVisibility() == View.VISIBLE)
                ||(reporte.getVisibility() == View.VISIBLE)){
            centro.setVisibility(View.INVISIBLE);
            agregar.setVisibility(View.INVISIBLE);
            reporte.setVisibility(View.INVISIBLE);
            //rb.setChecked(false);
        }
        else if((btnFindPath.getVisibility() == View.VISIBLE)
                ||(btn_ubiact.getVisibility() == View.VISIBLE)
                ||(etOrigin.getVisibility() == View.VISIBLE)
                ||(etDestination.getVisibility() == View.VISIBLE)){
            btnFindPath.setVisibility(View.INVISIBLE);
            btn_ubiact.setVisibility(View.INVISIBLE);
            etOrigin.setVisibility(View.INVISIBLE);
            etDestination.setVisibility(View.INVISIBLE);
        }
        else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
            android.os.Process.killProcess(android.os.Process.myPid());
            // Kill the app on click of back button.
            //alertasss.cancel(true);
            //alertasss.isCancelled();
            //handlerkm.dump(new LogPrinter(Log.DEBUG, "TAG"), "PREFIX");
        }
    }


    /** Metodo crear menus
     * 1. menu superior **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    /**  Opciones o itemMenu de los Menus creados
     * 1. Items de menu superior
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // cierra sesion
            play_sound = 0;
            FirebaseAuth.getInstance().signOut();  // cierro firebase
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);  // cierra google
            LoginManager.getInstance().logOut();  // cierra facebook
            goLoginScreen();  // se direge al loginActivity
            return true;
            // menu configuracion superior derecha
        }
        if(id == R.id.acerca){

            //Bundle outState = this.getIntent().getExtras();
            //outState.putInt("timer_cero_kph", play_sound);
            //outState.putInt("marcadores_foto", r_foto);
            //super.onSaveInstanceState(outState);
            goAcerca();
        }
        // pueden existir mas opciones como items
        return super.onOptionsItemSelected(item);
    }


    /** Acciones de los items del menu desplegable
     *  eventos de la varra de navegacion
     *  1. Mostrar zonas de fotomulta
     *  2. Mostrar Retenes en la via
     *  3. Mostrar Accidentes en la via
     *  4. Opcion Rutas
     *  5. Opcion Reportar
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        // se da cuenta de que id menu se selecciono y se decide que hacer
        if (id == R.id.nav_foto) {
            mMap.clear();  // borrar marcadores antiguos
            sendRequest();
            cmdm_foto = 0;  // cuenta de marcadores pintados
            marca_fotoradar();  // proceso mara pintar marcadores registraos en la BD

            // para ver en tiempo real unicmente el tipo de dato que se desea ya
            // sea fotoradar, reten, acci o ttodos
            r_foto = 1;
            r_ret = 0;
            r_acci = 0;

            hab_report = 1;  // para habilitar la opcion de reportar

            if (rb != null){ rb.setChecked(false); }
            // rb = objeto radio button = libera radio button

            // para ocultar:
            centro.setVisibility(View.INVISIBLE);
            agregar.setVisibility(View.INVISIBLE);
            reporte.setVisibility(View.GONE);

            // para iniciar alertas en cero kph:
            if (play_sound == 0){
                play_sound = 1;
                //aux_play_sound = 0;
                goalert();
            }


        } else if (id == R.id.nav_retenes) {

            mMap.clear();  // borrar marcadores antiguos

            sendRequest();

            cmdm_reten = 0;  // cuenta de marcadores pintados

            marca_reten();  // proceso mara pintar marcadores registraos en la BD

            // para ver en tiempo real unicmente el tipo de dato que se
            // desea ya sea fotoradar, reten, acci o ttodos
            r_foto = 0;
            r_ret = 1;
            r_acci = 0;

            hab_report = 1;  // para habilitar la opcion de reportar

            if (rb != null){ rb.setChecked(false); }
            // rb = objeto radio button = libera radio button

            // para ocultar:
            centro.setVisibility(View.INVISIBLE);
            agregar.setVisibility(View.INVISIBLE);
            reporte.setVisibility(View.GONE);

        } else if (id == R.id.nav_accidentes) {

            mMap.clear();  // borrar marcadores antiguos

            sendRequest();

            cmdm_acci = 0;  // cuenta de marcadores pintados

            marca_accidente();  // proceso mara pintar marcadores registraos en la BD

            // para ver en tiempo real unicmente el tipo de dato que se
            // desea ya sea fotoradar, reten, acci o ttodos
            r_foto = 0;
            r_ret = 0;
            r_acci = 1;

            hab_report = 1;  // para habilitar la opcion de reportar

            if (rb != null){ rb.setChecked(false); }
            // rb = objeto radio button = libera radio button

            // para ocultar:
            centro.setVisibility(View.INVISIBLE);
            agregar.setVisibility(View.INVISIBLE);
            reporte.setVisibility(View.GONE);

        } else if (id == R.id.nav_total) {

            mMap.clear();  // borrar marcadores antiguos

            sendRequest();

            // cuenta de marcadores pintados:
            cmdm_foto = 0;
            cmdm_reten = 0;
            cmdm_acci = 0;

            // proceso mara pintar marcadores registraos en la BD
            marca_fotoradar();
            marca_reten();
            marca_accidente();

            // para ver en tiempo real unicmente el tipo de dato que se
            // desea ya sea fotoradar, reten, acci o ttodos
            r_foto = 1;
            r_ret = 1;
            r_acci = 1;

            hab_report = 1;  // para habilitar la opcion de reportar

            if (rb != null){ rb.setChecked(false); }
            // rb = objeto radio button = libera radio button

            // para ocultar:
            centro.setVisibility(View.INVISIBLE);
            agregar.setVisibility(View.INVISIBLE);
            reporte.setVisibility(View.GONE);

            // para iniciar alertas en cero kph:
            if (play_sound == 0){
                play_sound = 1;
                //aux_play_sound = 0;
                goalert();
            }


        } else if (id == R.id.nav_rutas) {

            mensajes_ruta = 1;

            if (rb != null){ rb.setChecked(false); }
            // rb = objeto radio button = libera radio button

            // para ocultar:
            centro.setVisibility(View.INVISIBLE);
            agregar.setVisibility(View.INVISIBLE);
            reporte.setVisibility(View.GONE);

            // para ver:

            etDestination.setVisibility(View.VISIBLE);
            etOrigin.setVisibility(View.VISIBLE);
            btnFindPath.setVisibility(View.VISIBLE);
            btn_ubiact.setVisibility(View.VISIBLE);

        } else if (id == R.id.nav_reporte) {

            if (hab_report == 1){

                Log.d("Opcion", "Reporte");

                centr_mapa = false;
                // para que no se anime/mueva/reubique al centro
                // del mapa mientras se hace el reporte

                if (rb != null){ rb.setChecked(false); }
                // rb = objeto radio button = libera radio button

                // para ocultar:
                centro.setVisibility(View.INVISIBLE);
                agregar.setVisibility(View.INVISIBLE);

                etDestination.setVisibility(View.INVISIBLE);
                etOrigin.setVisibility(View.INVISIBLE);
                btnFindPath.setVisibility(View.INVISIBLE);
                btn_ubiact.setVisibility(View.INVISIBLE);
                vel.setVisibility(View.INVISIBLE);
                tempo.setVisibility(View.INVISIBLE);
                dist.setVisibility(View.INVISIBLE);

                // deja ver radio grupo
                reporte.setVisibility(View.VISIBLE);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawer.closeDrawer(GravityCompat.START);
        // cierra menu desplegable cuando se elige un aopcion

        return true;
    }


    /**
     * Elementos y procesos:
     */

    /** conecta google api client **/
    protected synchronized void buildGoogleApiClient() {
        // nuevo googleapiclient
        try{
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error Google Play Services", Toast
                    .LENGTH_SHORT).show();
        }
    }


    /** Cuando conecta a GooglePlayServices
     *
     * Actualiza camara en las coordenadas actuales y aplica zoom **/
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1){
            try{
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    goToLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 15);
                    // actualiza camara en las coordenadas actuales y aplica zoom
                }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), "Error Play Services", Toast.LENGTH_SHORT)
                        .show();
            }


        }else {
            ///PERMISO CONCENDIDO
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try{
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (mLastLocation != null) {
                        goToLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 15);  // actualiza camara en las coordenadas actuales y aplica zoom
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error Play Services", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(this, "1 Permiso Concedido", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /** Cuando se suspende conexion a GooglePlayServices
     * si se detiene la coneccion: **/
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Conexion Google Play Services suspendida", Toast.LENGTH_SHORT)
                .show();
    }


    /** Cuando falla la conexion a GooglePlayServices
     * si falla la coneccion**/
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, "Error al conectar Google Play Services", Toast.LENGTH_SHORT).show();

    }


    /** verifica coneccion con google services **/
    private boolean servicesOK() {

        try{
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            // pregunta si esta disponible
            int result = googleAPI.isGooglePlayServicesAvailable(this);  // resultado
            if(result != ConnectionResult.SUCCESS) {
                if(googleAPI.isUserResolvableError(result)) {
                    googleAPI.getErrorDialog(this, result, REQUEST_GOOGLE_PLAY_SERVICES).show();
                }
                Toast.makeText(this, "No se pudo conectar a Google Play Services", Toast
                        .LENGTH_SHORT).show();
                return false;
            }
            return true;  // si ttodo esta bien
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),
                    "Error No se pudo conectar a Google Play Services",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }  // verifica disponibilidad a Google Play Services


    /** Cuando el mapa esta listo ok **/
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // aqui se configura el mapa y se pueden agregar punteros marcadores iniciales etc.
        try {

            mMap = googleMap;  // se define objeto del mapa
            configuracion();  // configuracion inicial del mapa
            //sendRequest();

            // Long click para ubicacion de destino en rutas
            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){  // click largo sostenido en mapa

                @Override
                public void onMapLongClick(LatLng latLng) {

                    String lati = String.valueOf(latLng.latitude);
                    String longi = String.valueOf(latLng.longitude);
                    etDestination.setText(lati + ", " + longi);
                }
            });

            // prueba redibujar al cambio de camara
            if(r_foto ==1){
                sendRequest();
                marca_fotoradar();
            }
            if(r_ret ==1){
                marca_reten();
                sendRequest();
            }
            if(r_acci ==1){
                marca_accidente();
                sendRequest();
            }


        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error  Mapa", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    /** configura mapa **/
    public void configuracion() {

        try {
            if (mMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Lo siento! No se puede cargar el mapa",
                        Toast.LENGTH_SHORT).show();
                // revisa si el mapa se ha creado o no
            }
            if (mMap != null) {  // si el mapa se cargo correctamente:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);  // Cambiar el tipo de mapa

                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    mMap.setMyLocationEnabled(true);  // Mostrar / ocultar tu ubicación

                }else{
                    ///PERMISO CONCENDIDO
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager
                            .PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        // Mostrar / ocultar tu ubicación
                        Toast.makeText(this, "1 Permiso Concedido", Toast.LENGTH_SHORT).show();
                    }
                }
                // mMap.getUiSettings().setZoomControlsEnabled(true);
                // Mostrar / ocultar los controles del zoom
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                // Mostrar / ocultar boton de localización
                mMap.getUiSettings().setCompassEnabled(true);
                // Mostrar / ocultar icon de compas
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                // Mostrar / ocultar evento de rotar
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                // Mostrar / ocultar funcionalidad del zoom
                mMap.getUiSettings().setScrollGesturesEnabled(true);
                // Mostrar / ocultar funcionalidad del zoom scroll
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                // poder inclinar el mapa
                mMap.getUiSettings().setMapToolbarEnabled(false);
                // para que no aparesca en pantalla la conexion a google map app
                mMap.getUiSettings().setTiltGesturesEnabled(true);
                // habilita inclinacion del mapa
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error al Configuar Mapa", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    /** Redirecciona camara y zoom  ANIMACION CAMARA **/
    public void goToLocation(double lat, double lng, float zoom) {
        // ir a determinada coordenada, recibe latitud, longitud, y zoom

        try{
            LatLng latLng = new LatLng(lat, lng);
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            // without zoom use newLatLng(latLng) method
            mMap.animateCamera(update);

            // prueba orientacion mapa

            if(speedkm > 30){
                CameraPosition oldPos = mMap.getCameraPosition();
                //float sentido2 = sentido;
                //float bearing2 = (float) Math.toDegrees(sentido2);

               CameraPosition pos = CameraPosition.builder(oldPos).target(latLng).
                       bearing(sentido).build();
               mMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));
            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error al Actualizar mapa", Toast.LENGTH_SHORT).show();
        }
    }


    /** Envia solicitud para encontrar ruta **/
    private void sendRequest() {

        String origin = etOrigin.getText().toString();
        // trae ubicacion de origen
        String destination = etDestination.getText().toString();
        // trae ubicacion de destino

        if (origin.isEmpty()) {

            if(mensajes_ruta == 1){
                Toast.makeText(this, "Ingresar direccion de origen!", Toast.LENGTH_SHORT).show();
                // si esta vacio el origen
            }

            return;
        }
        else if (destination.isEmpty()) {

            if(mensajes_ruta == 1){
                Toast.makeText(this, "Ingresar direccion de destino!", Toast.LENGTH_SHORT).show();
                // si esta vacio el destino

            }
            return;
        }
        else{
            try {
                new DirectionFinder(this, origin, destination).execute();
                etDestination.setVisibility(View.INVISIBLE);  // ocultar elementos de crear ruta
                etOrigin.setVisibility(View.INVISIBLE);
                btnFindPath.setVisibility(View.INVISIBLE);
                btn_ubiact.setVisibility(View.INVISIBLE);
                mensajes_ruta = 0;
            }  // ejecuta constructor de ruta
            catch (UnsupportedEncodingException e) { e.printStackTrace(); }  // si hay error critico
        }
    }


    /** Para eliminar los ruta al trazar nueva:**/
    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Espere por favor.",
                "Encontrando ruta!", true);
        // muestra cuadro de dialogo mientras realiza busqueda de ruta
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
                // remueve marcadores de ruta anterior o existente
            }
        }
        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
                // remueve marcadores de ruta anterior o existente
            }
        }
        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
                // remueve marcadores de ruta anterior o existente
            }
        }
    }


    /** Cuando se escucha/recibe la informacion de la ruta encontrada: **/
    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {

        // se ejecuta codigo cuando termina de consultar informacion
        // JSON y devuelve o entra lista de puntos
        progressDialog.dismiss();  // cierra cuadro de dialogo
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            // se ubica la camara en inicio de ruta
            tempo.setText("Tiempo R: " + route.duration.text);
            // muestra en textview duracion
            dist.setText("Distancia R: " + route.distance.text);
            // muestra en textview distancia


            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_geo11))
                    // se inserta marcador en punto de origen
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_geo11))
                    // se inserta marcador en punto de destino
                    .title(route.endAddress)
                    .position(route.endLocation)));
            PolylineOptions polylineOptions = new PolylineOptions().
                    // aracteristicas de la/las lineas que compondran la ruta
                    geodesic(true).
                    color(Color.BLUE).
                    width(20);
            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));
            // se grafican poli lineas generando la ruta en el mapa
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }

        tempo.setVisibility(View.VISIBLE);
        dist.setVisibility(View.VISIBLE);
    }


    /** Regresar al loginactivity: **/
    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("timer_cero_kph", play_sound);
        intent.putExtra("marcadores_foto", r_foto);
        startActivity(intent);
    }


    /** Ir al Acerca de: **/
    private void goAcerca() {
        Intent intent = new Intent(this, acerca.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("timer_cero_kph", play_sound);
        intent.putExtra("marcadores_foto", r_foto);
        startActivity(intent);
    }


    /************************************  FIRE_BASE **********************************************/
    /** Para insertar marcador nuevo en la BD **/
    public void insmarcador (){
        // unicamente inserta informacion nueva a la BD

        try {

            LatLng center2 = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
            //se halla centro del mapa visible en el fragment, o LatLng
            // center1 = mMap.getCameraPosition().target;
            // Log.d("metodo centro1: " + String.valueOf(center1.latitude) +  " , "  + String.valueOf(center1.longitude), " #1 ");


            // obtener coordenadas centro en formato string:

            String geo_ctrola = String.valueOf(center2.latitude);
            String geo_ctrolo = String.valueOf(center2.longitude);
            String geo_ctro = geo_ctrola + "," +  geo_ctrolo;
            // coordenadas listas para agregar a BD
            // Log.d(" metodo centro a" , geo_ctro);  // publico las coordenadas encontradas del centro del mapa juntas pero solo separadas por una coma
            // Log.d(" metodo centro2: " + String.valueOf(center2.latitude) +  " , "  + String.valueOf(center2.longitude), " #2 ");  // publico las coordenadas encontr/adas en el centro


            // obtener fecha y hora:

            Calendar cal = Calendar.getInstance();
            String date = "" + cal.get(Calendar.DATE) + "/"
                    + (cal.get(Calendar.MONTH)+1) + "/"
                    + cal.get(Calendar.YEAR);
            // fecha dia/mes/año

            String time = "" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
            // hora  formato 24h
            // Log.d("  Fecha:  ",date );  //se publica fecha dia/mes/año
            // Log.d("  Hora:  ", time);  // se publica hora  formato 24h
            String hora_fecha = time + " " + date;
            // hora y fecha final para agregar a l abase de datos
            // Log.d(" Hora final", hora_fecha);
            // se publica formato hora y fecha final para agregar a l abase de dato


            // obtener direccion para agregar a la BD:

            List<Address> addressList;  // se crea una lista nueva
            Address address;  // encontar direccion en determinadas coordenadas
            Geocoder geocoder = new Geocoder(this);  // objeto geocoder del tipo Geocoder

            if(Geocoder.isPresent()){
                //do your stuff
                addressList = geocoder.getFromLocation(center2.latitude, center2.longitude, 1);
                // para obtener direccion apartir de latitud y longitud tipo double
                // y restringir los resulados a solo uno

                address = addressList.get(0);
                // se obtiene toda la informacion de el primer valor guardado en la lista

                if(address == null){
                    center2 = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
                    addressList = geocoder.getFromLocation(center2.latitude, center2.longitude, 2);
                    // para obtener direccion apartir de latitud y longitud tipo double
                    // y restringir los resulados a solo uno
                    address = addressList.get(0);
                    // se obtiene toda la informacion de el primer valor guardado en la lista
                }
                else if (address != null){

                    // String directotal = address.toString();  // funciona, se convierte el valor addres a string
                    // Log.d("  Direccion total:  ", directotal);  // funciona, se publica direcion resultado apatir de las coordenadas que se le pasan
                    //String direc_carrera = address.getThoroughfare();  funciona
                    // Log.d("  Carrera:  ", direc_carrera);  // funciona, se publica direcion  carrera resultado apatir de las coordenadas que se le pasan
                    // String direc_calle = address.getFeatureName();  funciona
                    // Log.d("  Calle:  ", direc_calle);  // funciona, se publica direcion  carrera resultado apatir de las coordenadas que se le pasan
                    // String direc_geo_marca = "" + address.getThoroughfare() + " " + "#" + address.getFeatureName();  // funciona
                    // address es una lista de vectores de strings en donde cada vector es una posible direccion cercana
                    // con las cooredenadas introducidas a un objeto de tipo geocoder utilizando el metodo .getFromLocation
                    // y en cada vector de  strings cada index es un valor en particular: direccion, ciudad, pais
                    String direc_geo_marca = address.getAddressLine(0);
                    // valor final de direccion que sera añadido a la base de datos
                    final marcadores nueva_marca =
                            new marcadores(geo_ctro, direc_geo_marca, hora_fecha);
                    // se crea objeto marca que contiene la informacion a publicar en
                    // la base de datos
                    // ya teniendo la informacion necesaria en el formato mismo(string)
                    // de la base de datos hay que agregarla a la base de datos
                    // publicar en base de datos:
                    if (item_selc == 1) {        // agregar a fotoradar

                        fotoradar.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                try{
                                    // averiguar cuantas ramas hay en acci
                                    long childrenCount = dataSnapshot.getChildrenCount();
                                    // funciona: retorna cantidad de ramas en fotoradar
                                    add_rama_foto = childrenCount + 1;
                                    // cuenta las ramas en acci + 1 para la nueva informacion
                                    fotoradar.child("mf" + String.valueOf(add_rama_foto))
                                            .setValue(nueva_marca);
                                    item_selc = 0;
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(), "Error escritura en BD",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w("Firebase", "Error en agrgar a la BD", databaseError.toException());
                            }
                        });

                        // ocultar:
                        centro.setVisibility(View.INVISIBLE);
                        agregar.setVisibility(View.INVISIBLE);

                        vel.setVisibility(View.VISIBLE);

                        Toast.makeText(this,
                                "Ubicacion de alerta agregado correctamente a la base de datos",
                                Toast.LENGTH_LONG).show();
                    }
                    if (item_selc == 2) { // agregar a reten

                        reten.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                try{
                                    // averiguar cuantas ramas hay en acci
                                    long childrenCount = dataSnapshot.getChildrenCount();
                                    // funciona: retorna cantidad de ramas en reten
                                    add_rama_retn = childrenCount + 1;
                                    // cuenta las ramas en acci
                                    reten.child("rt" + String.valueOf(add_rama_retn)).
                                            setValue(nueva_marca);
                                    item_selc = 0;
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(),
                                            "Error escritura en BD", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w("Firebase", "Error en agrgar a la BD",
                                        databaseError.toException());
                            }
                        });

                        // ocultar:
                        centro.setVisibility(View.INVISIBLE);
                        agregar.setVisibility(View.INVISIBLE);

                        vel.setVisibility(View.VISIBLE);

                        Toast.makeText(this,
                                "Ubicacion de alerta agregado correctamente a la base de datos",
                                Toast.LENGTH_LONG).show();
                    }
                    if (item_selc == 3) { // agregar a acci

                        acci.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                try{
                                    // averiguar cuantas ramas hay en acci
                                    long childrenCount = dataSnapshot.getChildrenCount();
                                    // funciona: retorna cantidad de ramas en acci
                                    add_rama_accidt = childrenCount + 1;  // cuenta las ramas en acci
                                    acci.child("ac" + String.valueOf(add_rama_accidt))
                                            .setValue(nueva_marca);
                                    item_selc = 0;
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(),
                                            "Error escritura en BD",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w("Firebase", "Error en agrgar a la BD", databaseError
                                        .toException());
                            }
                        });

                        // ocultar:
                        centro.setVisibility(View.INVISIBLE);
                        agregar.setVisibility(View.INVISIBLE);

                        vel.setVisibility(View.VISIBLE);

                        Toast.makeText(this,
                                "Ubicacion de alerta agregado correctamente a la base de datos",
                                Toast.LENGTH_LONG).show();
                    }

                    centr_mapa = true;  // permitir animacion reubicacion mapa:
                    rb.setChecked(false);  // rb = objeto radio button = libera radio button

                    // ocultar:
                    centro.setVisibility(View.INVISIBLE);
                    agregar.setVisibility(View.INVISIBLE);

                    vel.setVisibility(View.VISIBLE);
                }
            }else{
                //cann't implement geocoder
                addressList = geocoder.getFromLocation(center2.latitude, center2.longitude, 1);
                // para obtener direccion apartir de latitud y longitud tipo double
                // y restringir los resulados a solo uno
                Toast.makeText(getApplicationContext(),
                        "Error Objeto Geocoder",
                        Toast.LENGTH_SHORT).show();
            }


        } catch (IOException e) {
            e.printStackTrace();
            centro.setVisibility(View.INVISIBLE);
            agregar.setVisibility(View.INVISIBLE);

            vel.setVisibility(View.VISIBLE);

            Toast.makeText(getApplicationContext(),
                    "Error escritura en BD",
                    Toast.LENGTH_SHORT).show();
        }
    }  // Boton que unicamente agrega informacion nueva a la base de datos


    /** Lee informacion desde la BD para crear los  marcadores de fotoradar **/
    public void marca_fotoradar(){

        fotoradar.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{
                    if (mMap != null){

                        // averiguar cuantas ramas hay en fotoradar y con un
                        // for obtener los datos para pintar las marcas en el mapa

                        long childrenCount = dataSnapshot.getChildrenCount();
                        // funciona: retorna cantidad de ramas en fotoradar
                        int i  = 1;  // cuenta las ramas en fotoradar
                        copi = new String[((int)childrenCount) + 1];
                        // copio informacion coordenadas de los marcadores,
                        // en el index cero no hay nada

                        while (i <= childrenCount){
                            // mientras i sea menor o igual al numero total de ramas den fotoradar:

                            marcadores marca = dataSnapshot.child("mf" + String.valueOf(i))
                                    .getValue(marcadores.class);
                            // funciona  marca es un objeto de la clase marcadores
                            // A el objeto marca lee y escribe los tres datos que se
                            // requieren en los marcadores: coordenada, direccion y fecha
                            // el objeto marca se puede utilizar sobre la rama fotoradar
                            // especificando el .chil("mfx")
                            // fotomulta.add(i, marca.getCoor());

                            copi[i]= marca.getCoor();

                            String [] resulcoor;  // vector para recibir string de las coordenadas
                            resulcoor = String_Coord_Marca(marca.getCoor());
                            // se reciben las coordenadas

                            double lati, longi;  // para convertir de string a double

                            lati = Double.parseDouble(resulcoor[0]);
                            // se convierte de string a double
                            longi = Double.parseDouble(resulcoor[1]);

                            mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(lati, longi))
                                    .title(marca.getDirec())
                                    .snippet(marca.getFecha())
                                    .icon(BitmapDescriptorFactory.
                                            fromResource(R.drawable.ic_ffoto1))
                                    // icono conservo transparencia png
                                    .anchor(0.5f, 1)
                                    .flat(false));  // aplana el marcador

                            // valores de prueba:
                            // Log.d("mf" + String.valueOf(i) + "   Geocoor: ", marca.getCoor());
                            // Log.d("mf" + String.valueOf(i) + "   Direccion: ", marca.getDirec());
                            // Log.d("mf" + String.valueOf(i) + "   Fecha: ", marca.getFecha());

                            i++;  // aumenta contador de ramas
                            cmdm_foto = i;
                        }

                    }

                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),
                            "Error al dibujar Marcadores",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Firebase", "Error al dibujar Marcadores", databaseError.toException());
            }
        });
    }  // dibuja marcadores en mapa


    /** Lee informacion desde la BD para crear los  marcadores de reten **/
    public void marca_reten(){

        reten.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{

                    // averiguar cuantas ramas hay en reten y con un for
                    // obtener los datos para pintar las marcas en el mapa

                    long childrenCount = dataSnapshot.getChildrenCount();
                    // funciona: retorna cantidad de ramas en reten
                    long i = 1;  // cuenta las ramas en reten

                    while (i <= childrenCount){
                        // mientras i sea menor o igual al numero total de ramas den reten:

                        marcadores marca = dataSnapshot.child("rt" + String.valueOf(i))
                                .getValue(marcadores.class);
                        // funciona  marca es un objeto de la clase marcadores

                        // A el objeto marca lee y escribe los tres datos que se requieren
                        // en los marcadores: coordenada, direccion y fecha

                        // el objeto marca se puede utilizar sobre la rama
                        // reten especificando el .chil("rtx")

                        String [] resulcoor;  // vector para recibir string de las coordenadas
                        resulcoor = String_Coord_Marca(marca.getCoor());
                        // se reciben las coordenadas

                        double lati, longi;  // para convertir de string a double

                        lati = Double.parseDouble(resulcoor[0]);  // se convierte de string a double
                        longi = Double.parseDouble(resulcoor[1]);

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lati, longi))
                                .title(marca.getDirec())
                                .snippet(marca.getFecha())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_ppoli2))
                                .anchor(0.5f, 1)
                                .flat(false));  // aplana el marcador
                        // valores de prueba:
                        // Log.d("rt" + String.valueOf(i) + "   Geocoor: ", marca.getCoor());
                        // Log.d("rt" + String.valueOf(i) + "   Direccion: ", marca.getDirec());
                        // Log.d("rt" + String.valueOf(i) + "   Fecha: ", marca.getFecha());
                        i++;  // aumenta contador de ramas
                        cmdm_reten = i;
                    }
                    // Log.d("Cuenta children: ", String.valueOf(childrenCount));
                    // funciona, muestra numero total de ramas en fotoradar
                    // Log.d("Alerta reten", "Alerta reten");

                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),
                            "Error escritura en BD",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("Firebase",
                        "Error al dibujar Marcadores en mapa",
                        databaseError.toException());
            }
        });
    }  // dibuja marcadores en mapa


    /** Lee informacion desde la BD para crear los  marcadores de accidente **/
    public void marca_accidente(){

        acci.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{

                    // averiguar cuantas ramas hay en acci y con un for obtener los
                    // datos para pintar las marcas en el mapa

                    long childrenCount = dataSnapshot.getChildrenCount();
                    // funciona: retorna cantidad de ramas en acci
                    long i = 1;  // cuenta las ramas en acci

                    while (i <= childrenCount){  // mientras i sea menor o
                        // igual al numero total de ramas den acci:

                        marcadores marca = dataSnapshot.child("ac" + String.valueOf(i))
                                .getValue(marcadores.class);
                        // funciona  marca es un objeto de la clase marcadores

                        // A el objeto marca lee y escribe los tres datos que se requieren
                        // en los marcadores: coordenada, direccion y fecha

                        // el objeto marca se puede utilizar sobre la rama acci
                        // especificando el .chil("acx")

                        String [] resulcoor;  // vector para recibir string de las coordenadas
                        resulcoor = String_Coord_Marca(marca.getCoor());
                        // se reciben las coordenadas

                        double lati, longi;  // para convertir de string a double

                        lati = Double.parseDouble(resulcoor[0]);  // se convierte de string a double
                        longi = Double.parseDouble(resulcoor[1]);

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lati, longi))
                                .title(marca.getDirec())
                                .snippet(marca.getFecha())
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_aacci4))
                                .anchor(0.5f, 1)
                                .flat(false));  // aplana el marcador
                        // valores de prueba:
                        // Log.d("ac" + String.valueOf(i) + "   Geocoor: ", marca.getCoor());
                        // Log.d("ac" + String.valueOf(i) + "   Direccion: ", marca.getDirec());
                        // Log.d("ac" + String.valueOf(i) + "   Fecha: ", marca.getFecha());
                        i++;  // aumenta contador de ramas
                        cmdm_acci = i;
                    }
                    // Log.d("Cuenta children: ", String.valueOf(childrenCount));  // funciona, muestra numero total de ramas en fotoradar
                    // Log.d("Alerta accidente", "Alerta accidente");

                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error escritura en BD", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("Firebase", "Error al dibujar Marcadores en mapa", databaseError.toException());
            }
        });
    }  // dibuja marcadores en mapa


   /** Genera alertas  a 0 kph**/
    public void goalert(){




        if(play_sound == 1){

            // Aqui se deben implementar las alertas en audio
            // segun se acerque el dispositivo a dichos puntos
            handlerkm = new Handler();
            //final String[] clone = copii.clone();
            Runnable runnablekm = new Runnable() {

                @Override
                public void run() {

                    try{


                        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1){
                            //try{
                            L_actual_timer = locationManager.getLastKnownLocation(GPS_PROVIDER);
                            // posicion actual

                            if(L_actual_timer == null){
                                L_actual_timer = locationManager
                                        .getLastKnownLocation(NETWORK_PROVIDER);
                            }

                        }else {
                            ///PERMISO CONCENDIDO
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {
                                //try{
                                    L_actual_timer = locationManager
                                            .getLastKnownLocation(GPS_PROVIDER);  // posicion actual
                                if(L_actual_timer == null){
                                    L_actual_timer = locationManager
                                            .getLastKnownLocation(NETWORK_PROVIDER);
                                }

                            }
                        }

                    // revision de distancias con respecto a los marcadores:
                    String [] resulcoor;  // vector para recibir string de las coordenadas
                    double lati, longi;  // para convertir de string a double
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    // play_sound = 1;

                        //if(speedkm < 25){

                            // precision != 0.0

                            Log.d("antes del while","ok go1");

                            int jj = 1;

                            while (jj <= copi.length - 1){

                                Log.d("despues del while","ok");

                                resulcoor = String_Coord_Marca(copi[jj]);
                                // se reciben las coordenadas
                                lati = Double.parseDouble(resulcoor[0]);
                                // se convierte de string a double
                                longi = Double.parseDouble(resulcoor[1]);

                                lmarcador.setLatitude(lati);
                                // set lat  y long de cada marcador
                                lmarcador.setLongitude(longi);

                                // max = sentido + 23;
                                // min = sentido - 23;

                                //sentidoA = lactual.bearingTo(lmarcador);
                                // direccion a marcador
                                distanciaA = L_actual_timer.distanceTo(lmarcador);

                                if (((speedkm >= 0.0f)&&(speedkm <= 25))&&(distanciaA < 35)){

                                    // aux_play_sound = 1;


                                    if(mediaPlayer != null){
                                        mediaPlayer.release();
                                        mediaPlayer = MediaPlayer
                                                .create(getApplicationContext(), R.raw.uno);
                                        mediaPlayer.start();

                                        // funciona pero cambios bruscos de aceleracion no
                                        // el siguente escucha es para habilitar el onbackgraund

                                        //Escucha cuando termina de reproducir /////////////////////
                                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                           public void onCompletion(MediaPlayer mp) {
                                                aux_play_sound = 0;
                                           }
                                        });
                                        ////////////////////////////////////////////////////////////

                                    }

                                    v.vibrate(500);

                                    cma++;
                                    if(cma >= 30){cma = 0;}



                                    Log.d("Alerta",
                                            "Precaucion! zona de foto multa a menos de 20 metros");

                                    Toast.makeText(getApplicationContext(),
                                            "Precaucion! zona de foto multa a menos de 20 metros",
                                            Toast.LENGTH_SHORT)
                                            .show();

                                    break;
                                }
                                jj++;
                            }
                        //}

                        Log.d("revision km","ok go1");
                        handler.postDelayed(this, 4000 + 1000*cma);
                        // se ejecuta siempre cada 4 segundos
                    }catch (Exception e){
                     Toast.makeText(getApplicationContext(),
                             "Error en Aletas Foto Multa",
                             Toast.LENGTH_SHORT).show();
                        play_sound = 0;
                   }
                }
            };
            handler.postDelayed(runnablekm, 5000);  // se ejecuta por primera vez a los 3 segundo

        }
    }


    /** Genera alertas  a mas de 30 kph**/
    private class goalert_dos extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            try{
                // calcular distancias
                // revision de distancias con respecto a los marcadores:
                String [] resulcoor;  // vector para recibir string de las coordenadas
                double lati, longi;  // para convertir de string a double
                float min;
                float max;
                float aux_sentidoA;
                //Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                //play_sound = 1;



                    // precision != 0.0

                    Log.d("antes del while","ok go 2");

                    int jj = 1;

                    while (jj <= copi.length - 1){

                        Log.d("despues del while","ok");

                        resulcoor = String_Coord_Marca(copi[jj]);  // se reciben las coordenadas
                        lati = Double.parseDouble(resulcoor[0]);  // se convierte de string a double
                        longi = Double.parseDouble(resulcoor[1]);

                        lmarcador.setLatitude(lati);  // set lat  y long de cada marcador
                        lmarcador.setLongitude(longi);

                        max = sentido + 30;  // cono anulo de 120 grados
                        min = sentido - 30;

                        aux_sentidoA = lactual.bearingTo(lmarcador);  // direccion a marcador



                        sentidoA = normalizeDegree(aux_sentidoA); // direccion a marcador
                        distanciaA = lactual.distanceTo(lmarcador);


                        if ((sentidoA <= max)&&(sentidoA >= min)){

                            if (((speedkm > 28)&&(speedkm <= 60))&&(distanciaA < 150)){

                                cma = 0;
                                // soundPool.play(treinta, 1, 1, 0, 0, 1);
                                // v.vibrate(500);
                                // Log.d("Alerta",
                                // "Precaucion! zona de foto multa a menos de 80 metros");
                                // break;
                                reproducir = 2;
                                aux_play_sound = 1;
                                break;
                            }
                            if (((speedkm > 60)&&(speedkm <= 80))&&(distanciaA < 300)){

                                cma = 0;
                                // soundPool.play(sesenta, 1, 1, 0, 0, 1);
                                // v.vibrate(500);
                                // Log.d("Alerta",
                                // "Alerta! zona de foto multa a menos de 200 metros");
                                // break;
                                reproducir = 3;
                                aux_play_sound = 1;
                                break;
                            }
                            if (((speedkm > 80)&&(speedkm <= 180))&&(distanciaA < 1000)){

                                cma = 0;
                                //soundPool.play(ochenta, 1, 1, 0, 0, 1);
                                // v.vibrate(500);
                                // Log.d("Alerta",
                                // "Alerta! zona de foto multa a menos de 500 metros");
                                // break;
                                reproducir = 4;
                                aux_play_sound = 1;
                                break;
                            }
                        }
                        jj++;
                    }

                Log.d("revision km","ok go 2");




            }catch (Exception e){e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // reproducir alerta y condiciones

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

          // if(reproducir == 1){  // de 0 a 30 kph
               //soundPool.play(cero, 1, 1, 0, 0, 1);
             //  if(mediaPlayer != null){
             //      mediaPlayer.release();
             //      mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.uno);
             //      mediaPlayer.start();

              //     //Escucha cuando termina de reproducir
              //     mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
              //         @Override
              //         public void onCompletion(MediaPlayer mp) { aux_play_sound = 1; }
              //     });
              // }

             //  v.vibrate(500);
             //  Log.d("Alerta","Precaucion! zona de foto multa a menos de 20 metros");
            //}
            if((reproducir == 2) && (aux_play_sound == 1)){  // de 30 a 60 kph
                // soundPool.play(treinta, 1, 1, 0, 0, 1);
                if(mediaPlayer != null){
                    mediaPlayer.release();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.dos);
                    mediaPlayer.start();

                    //Escucha cuando termina de reproducir
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) { aux_play_sound = 0; }
                    });
                }

                v.vibrate(500);
                bearin_local.setText("Bearing: " + sentido + "  Bearing_to:" + sentidoA);

                Log.d("Alerta","Precaucion! zona de foto multa a menos de 80 metros");

                Toast.makeText(getApplicationContext(),
                        "Precaucion! zona de foto multa a menos de 80 metros",
                        Toast.LENGTH_SHORT)
                        .show();

            }
            if((reproducir == 3) && (aux_play_sound == 1)){  // de 60 a 80 kph
                //soundPool.play(sesenta, 1, 1, 0, 0, 1);
                if(mediaPlayer != null){
                    mediaPlayer.release();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.tres);
                    mediaPlayer.start();

                    //Escucha cuando termina de reproducir
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) { aux_play_sound = 0; }
                    });
                }

                v.vibrate(500);
                bearin_local.setText("Bearing: " + sentido + "  Bearing_to:" + sentidoA);

                Log.d("Alerta","Alerta! zona de foto multa a menos de 200 metros");

                Toast.makeText(getApplicationContext(),
                        "Alerta! zona de foto multa a menos de 200 metros",
                        Toast.LENGTH_SHORT)
                        .show();

            }
            if((reproducir == 4) && (aux_play_sound == 1)){  // de 80 a 180 kph
                //soundPool.play(ochenta, 1, 1, 0, 0, 1);
                if(mediaPlayer != null){
                    mediaPlayer.release();
                    mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.cuatro);
                    mediaPlayer.start();

                    //Escucha cuando termina de reproducir
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) { aux_play_sound = 0; }
                    });
                }

                v.vibrate(500);
                bearin_local.setText("Bearing: " + sentido + "  Bearing_to:" + sentidoA);

                Log.d("Alerta","Alerta! zona de foto multa a menos de 500 metros");

                Toast.makeText(getApplicationContext(),
                        "Alerta! zona de foto multa a menos de 500 metros",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    /** Carga imagen de perfil: **/
    private class carga_imagen extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {

            try{
                fot_perf = BitmapFactory
                        .decodeStream((InputStream) new URL(photoUrl.toString()).getContent());
            }catch (Exception e){e.printStackTrace();}
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            foto_perfil.setImageBitmap(fot_perf);
        }
    }


    /**
     * SUB_Strings:
     */


    /** Adecuacion de los Srings de las coordenadas **/
    public String [] String_Coord_Marca(String coo) {

        int l = coo.length(), k ;
        char c ;
        String slat = "", slon = "" ;
        String [] result = {"", ""};

        for (int i = 0; i < l; i++){

            c = coo.charAt(i);

            if (c == '-'){

                k = i;
                slat = coo.substring(0, k-2);
                slon = coo.substring(k, l-1);
                //coo = "";
                break;
            }
        }

        result[0] = slat;
        result[1] = slon;
        return result;
    }


    /** Obtener fecha de los Retenes  y Accidentes en la BD  **/
    public String [] GetFecha(String fech) {

        int l = fech.length(), k, e = 0;
        char c ;
        String fecha;
        String ano = "", mes = "", dia = "";
        String [] result = {"", "", ""};
        int cuenta = 0;

        for (int i = 0; i < l; i++){

            c = fech.charAt(i);

            if (c == ' '){

                k = i;
                fecha = fech.substring(k+1, l);

                for (int j = 0; j < fecha.length(); j++){

                    char cc = fecha.charAt(j);

                    if(cc == '/'){

                        cuenta = cuenta +1;

                        if(cuenta == 1){
                            dia = fecha.substring(0, j);
                            e = j;
                        }
                        else if(cuenta == 2){
                            mes = fecha.substring(e+1, j);
                            ano = fecha.substring(j+1, fecha.length());
                        }
                    }
                }
                break;
            }
        }

        result[0] = dia;
        result[1] = mes;
        result[2] = ano;

        // Log.d("fecha:",dia + "," + mes + "," + ano);

        return result;
    }


    /**
     *  Normalizar bearingTo.
     */
    private float normalizeDegree(float value) {
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return 180 + (180 + value);
        }
    }




    /** fin del super metodo de la actividad principal**/

}





