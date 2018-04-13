package jogo.uema.com.br.sw.jogo;

import android.graphics.Bitmap;

/**
 * Avião inimigo médio, de tamanho médio, habilidade moderadamente anti-ataque
 */
public class AeronaveInimigaMedia extends AeronaveInimiga {

    public AeronaveInimigaMedia(Bitmap bitmap){
        super(bitmap);
        setPower(4);//A capacidade de resistência do inimigo é de 4, ou seja,
                    // quatro balas são necessárias para destruir este tipo de inimigo.
        setValue(6000);//Destrua um inimigo do medio e obtenha 6.000 pontos.
    }

}