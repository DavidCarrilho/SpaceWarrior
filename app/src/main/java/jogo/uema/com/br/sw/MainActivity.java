package jogo.uema.com.br.sw;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity implements Button.OnClickListener {


    MediaPlayer mp = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mp = MediaPlayer.create(this, R.raw.mp1);
        mp.start();
        mp.setLooping(true);
    }

    //Startando o Jogo
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.btnJogar){
            mp.stop();
            StartarJogo();
        }
    }

    public void StartarJogo(){
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);

    }

    public void jogoMudo(View view){
        mp.pause();
    }

    public void sair(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Você quer realmente sair?");
        builder.setCancelable(true);

        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }
}