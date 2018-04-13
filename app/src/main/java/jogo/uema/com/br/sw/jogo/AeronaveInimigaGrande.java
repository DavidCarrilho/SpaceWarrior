package jogo.uema.com.br.sw.jogo;

import android.graphics.Bitmap;

/**
 * Grande aeronave inimiga, tamanho grande, forte habilidade anti-ataque
 */
public class AeronaveInimigaGrande extends AeronaveInimiga {

    public AeronaveInimigaGrande(Bitmap bitmap){
        super(bitmap);
        setPower(10);//A resistência do inimigo é 10, o que significa que 10 balas
                    // são necessárias para destruir este tipo de inimigo.
        setValue(30000);//Destruir um grande inimigo pode obter 30.000 pontos
    }

}