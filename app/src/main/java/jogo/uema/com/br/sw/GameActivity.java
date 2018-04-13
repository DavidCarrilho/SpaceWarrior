package jogo.uema.com.br.sw;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import jogo.uema.com.br.sw.jogo.GameView;


public class GameActivity extends Activity {

    private GameView gameView;
    MediaPlayer mp = new MediaPlayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //mp.stop();
        mp = MediaPlayer.create(this, R.raw.mp4);
        mp.start();
        mp.setLooping(true);

        gameView = (GameView)findViewById(R.id.gameView);
        //0:combatAircraft
        //1:sprite_explosao
        //2:balaAmarela
        //3:balaAzul
        //4:smallEnemyPlane
        //5:middleEnemyPlane
        //6:bigEnemyPlane
        //7:bombAward
        //8:bulletAward
        //9:pause1
        //10:pause2
        //11:bomba
        int[] bitmapIds = {
                R.drawable.aeronave,
                R.drawable.sprite_explosao,
                R.drawable.bala_amarela,
                R.drawable.bala_azul,
                R.drawable.aeronave_pequena,
                R.drawable.aeronave_media,
                R.drawable.aeronave_grande,
                R.drawable.premio_bomba,
                R.drawable.premio_bala,
                R.drawable.pause1,
                R.drawable.pause2,
                R.drawable.bomba
        };
        gameView.start(bitmapIds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(gameView != null){
            mp.pause();
            gameView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(gameView != null){
            mp.stop();
            gameView.destroy();

            mp = MediaPlayer.create(this, R.raw.mp1);
            mp.start();
            mp.setLooping(true);

            //TextView gameOver
        }
        gameView = null;
    }
}