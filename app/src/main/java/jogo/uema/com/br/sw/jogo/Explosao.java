package jogo.uema.com.br.sw.jogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Classe de efeito explosivo, a localização não é variável,
 * mas pode mostrar efeito de explosão dinâmico
 */
public class Explosao extends Sprite {

    private int segment = 14;//O efeito de explosão é composto por 14 segmentos
    private int level = 0;//O 0º fragmento que inicialmente explodiu
    private int explodeFrequency = 2;//2 frames por fragmento de explosão

    public Explosao(Bitmap bitmap){
        super(bitmap);
    }

    @Override
    public float getWidth() {
        Bitmap bitmap = getBitmap();
        if(bitmap != null){
            return bitmap.getWidth() / segment;
        }
        return 0;
    }

    @Override
    public Rect getBitmapSrcRec() {
        Rect rect = super.getBitmapSrcRec();
        int left = (int)(level * getWidth());
        rect.offsetTo(left, 0);
        return rect;
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            if(getFrame() % explodeFrequency == 0){
                //o level (nível) é incrementado por um e é usado para desenhar a próxima explosão
                level++;
                if(level >= segment){
                    //Quando todos os fragmentos de explosão são desenhados,
                    // destrua o efeito de explosão
                    destroy();
                }
            }
        }
    }

    //Obter o número de frames necessários para desencadear uma explosão completa,
    // ou seja, 28 frames
    public int getExplodeDurationFrame(){
        return segment * explodeFrequency;
    }
}