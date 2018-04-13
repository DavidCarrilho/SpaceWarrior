package jogo.uema.com.br.sw.jogo;

import android.graphics.Bitmap;

/**
 * A classe Balas move-se de baixo para cima em linha reta
 */
public class Balas extends AutoSprite {

    public Balas(Bitmap bitmap){
        super(bitmap);
        setSpeed(-10);//Números negativos indicam balas voam para cima
    }

}