package jgabolord.prueba1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class acerca extends AppCompatActivity {

    int play_sound;
    int r_foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acerca);

        // se reciben variables que se guardan de la actividad principal:

        Bundle datos = this.getIntent().getExtras();

        play_sound = datos.getInt("timer_cero_kph");
        r_foto = datos.getInt("marcadores_foto");


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goMainScreen();
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
}
