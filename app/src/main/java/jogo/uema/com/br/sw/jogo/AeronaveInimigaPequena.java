package jogo.uema.com.br.sw.jogo;

import android.graphics.Bitmap;

/**
 * Pequena aeronave inimiga, tamanho pequeno, baixa habilidade anti-ataque
 */
public class AeronaveInimigaPequena extends AeronaveInimiga {

    public AeronaveInimigaPequena(Bitmap bitmap){
        super(bitmap);
        setPower(1);//A resistência do inimigo é 1, o que significa que
                    // uma bala pode destruir o este tipo de inimigo.
        setValue(1000);//Destruir uma pequena aeronave inimiga pode obter 1000 pontos
    }

}