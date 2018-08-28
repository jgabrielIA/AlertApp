package jgabolord.prueba1;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;



public class LoginActivity extends AppCompatActivity {

    // informacion extra para la actividad principal

    int play_sound;
    int r_foto;

    SignInButton lgn_btn;  // Boton de icicio sesion de google
    private static final int RC_SIGN_IN = 0;  //Indicador d inicio
    private GoogleApiClient mGoogleApiClient;  // Cliente api google
    private static final String TAG = "LOGIN_ACTIVITY";
    private FirebaseAuth mAuth;  // Objeto Autenticacion Firebase (OAF)
    private FirebaseAuth.AuthStateListener mAuthListener;  // Escucha de OAF

    // Para proceso con facebook:
    LoginButton loginButton;  // Boton inicio con facebook
    CallbackManager callbackManager;

   // private ProgressBar progressBar;  // mientras carga la autenticacion

    // identificadores de permisos
    private static final int PERMISO_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISO_ACCESS_COARSE_LOCATION = 2;


    // cuadros de dialogo interaccion permisos usuario

    android.support.v7.app.AlertDialog alert = null;  // cuando no esta activado el GPS y pregunta

    android.support.v7.app.AlertDialog explica = null;  // expicar permisos

    // android.support.v7.app.AlertDialog confirma = null;  // confirma desicion de denegar permiso


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(this.getApplicationContext());

        setContentView(R.layout.activity_login);

        try {
        Bundle datos = this.getIntent().getExtras();
        play_sound = datos.getInt("timer_cero_kph");
        r_foto = datos.getInt("marcadores_foto");

        } catch (Exception e) {
        e.printStackTrace();
        }

        mAuth = FirebaseAuth.getInstance();  // Instancia firebase

        callbackManager = CallbackManager.Factory.create();  // Objeto callback de facebook login
        loginButton = (LoginButton) findViewById(R.id.login_f_btn);  // Inicializar btn facebook
        loginButton.setReadPermissions(Arrays.asList("email"));
        // LoginManager.getInstance().registerCallback(callbackManager,
        // new FacebookCallback<LoginResult>() {

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // goMainScreen();


                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    if(ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED){

                        handleFacebookAccessToken(loginResult.getAccessToken());
                        Toast.makeText(LoginActivity.this,
                                "Autenticacion ok.",
                                Toast.LENGTH_SHORT).show();

                        // Toast.makeText(getApplicationContext(),
                        // "1 Permiso Concedido", Toast.LENGTH_SHORT).show();

                    } else{

                        explicarUsoPermiso();
                        solicitarPermiso_COARSE();
                    }



                } else {

                    explicarUsoPermiso();
                    solicitarPermiso_FINE();
                }






            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this,
                        "Autenticacion cancelada.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error en autenticacion.",
                        Toast.LENGTH_SHORT).show();
            }
        });


        lgn_btn = (SignInButton) findViewById(R.id.login_g_btn);
        //Inicializa el objeto boton login google

        // Configura Google Sign In
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        // Configura el Google Api Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        Toast.makeText(getApplicationContext(),
                                connectionResult.getErrorMessage(), Toast.LENGTH_SHORT)
                                .show();  // Muestra si hay error al conectar con api google

                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        // Evento al precionar el boton de inicio de sesion
        lgn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        // Escucha del estado de la autenticacion con firebase:
        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // FirebaseUser user = firebaseAuth.getCurrentUser();  // Verifica si existe usuario
                if (firebaseAuth.getCurrentUser()!=null){


                    //pueba permisos

                    /**
                     * ¿Tengo el permiso para hacer la accion?
                     */
                     ///PERMISO CONCENDIDO
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //goMainScreen();  // tanto para google y facebook

                        //Toast.makeText(getApplicationContext(), "1 Permiso Concedido", Toast.LENGTH_SHORT).show();

                        if(ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED){

                            goMainScreen();  // tanto para google y facebook

                            Toast.makeText(getApplicationContext(),
                                    "1 Permiso Concedido",
                                    Toast.LENGTH_SHORT).show();

                        } else{

                            explicarUsoPermiso();
                            solicitarPermiso_COARSE();
                        }

                    } else {

                        explicarUsoPermiso();
                        solicitarPermiso_FINE();
                    }
                }
            }
        };

    }


    /********** Metodos:  *********/


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }  // para inicio con google


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);  // Para google
        callbackManager.onActivityResult(requestCode, resultCode, data);  // Para facebook
        //super.onActivityResult(requestCode, resultCode, data);
        //callbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                // Lleva token de google a metodo/escucha de firebase
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(LoginActivity.this,
                        "Fallo en inicio de sesion.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    /** Acceso a firebase con token de google:  **/

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this,
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    /** Acceso a firebase con token de facebook:  **/

    private void handleFacebookAccessToken(AccessToken accessToken) {

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithCredential", task.getException());
                    Toast.makeText(getApplicationContext(),
                            "Authentication failed.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    private void goMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("timer_cero_kph", play_sound);
        intent.putExtra("marcadores_foto", r_foto);
        startActivity(intent);
    }

    private void explicarUsoPermiso() {


        //Este IF es necesario para saber si el usuario ha marcado o no la casilla [] No volver a preguntar
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            Toast.makeText(this, "2.1 Explicamos razonadamente porque necesitamos el permiso A",
                    Toast.LENGTH_SHORT)
                    .show();

            //Explicarle al usuario porque necesitas el permiso (Opcional)
            alertDialogBasico();

        } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)){


            Toast.makeText(this, "2.1 Explicamos razonadamente porque necesitamos el permiso B",
                    Toast.LENGTH_SHORT)
                    .show();

            //Explicarle al usuario porque necesitas el permiso (Opcional)
            alertDialogBasico();

        }


    }


    public void alertDialogBasico() {

        try{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Para el correcto funcionamiento de AlertApp " +
                    "es necesario conocer tu ubicación.")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog,
                                            @SuppressWarnings("unused") final int id) {
                            //startActivity(new Intent(android.provider.
                            // Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            dialog.cancel();
                        }
                    });
            explica = builder.create();
            explica.show();

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error con explicar permiso",
                    Toast.LENGTH_SHORT).show();
        }



    }


    private void solicitarPermiso_COARSE() {


        //Pedimos el permiso o los permisos con un cuadro de dialogo del sistema
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISO_ACCESS_COARSE_LOCATION);

        Toast.makeText(this, "2.2 Pedimos el permiso con un cuadro de dialogo del sistema A",
                Toast.LENGTH_SHORT).show();


    }

    private void solicitarPermiso_FINE() {


        //Pedimos el permiso o los permisos con un cuadro de dialogo del sistema
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISO_ACCESS_FINE_LOCATION);

        Toast.makeText(this, "2.2 Pedimos el permiso con un cuadro de dialogo del sistema B",
                Toast.LENGTH_SHORT).show();


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /**
         * Si tubieramos diferentes permisos solicitando permisos de la aplicacion,
         * aqui habria varios IF
         */
        if (requestCode == PERMISO_ACCESS_FINE_LOCATION) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Realizamos la accion
                //startActivity(intentLLamada);
                Toast.makeText(this, "3.1 Permiso Concedido A", Toast.LENGTH_SHORT).show();
            } else {
                //1-Seguimos el proceso de ejecucion sin esta accion: Esto lo recomienda Google
                //2-Cancelamos el proceso actual
                //3-Salimos de la aplicacion
                Toast.makeText(this, "3.2 Permiso No Concedido B", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == PERMISO_ACCESS_COARSE_LOCATION) {

            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Realizamos la accion
                //startActivity(intentLLamada);
                Toast.makeText(this, "3.1 Permiso Concedido C", Toast.LENGTH_SHORT).show();
            } else {
                //1-Seguimos el proceso de ejecucion sin esta accion: Esto lo recomienda Google
                //2-Cancelamos el proceso actual
                //3-Salimos de la aplicacion
                Toast.makeText(this, "3.2 Permiso No Concedido D", Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
