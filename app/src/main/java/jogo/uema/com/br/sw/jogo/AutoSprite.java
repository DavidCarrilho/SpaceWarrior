package jogo.uema.com.br.sw.jogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Straight line Sprite, sua posição só pode ser direta para cima e para baixo
 */
public class AutoSprite extends Sprite {
    //O número de pixels movidos por quadro, positivo negativo
    private float speed = 2;

    public AutoSprite(Bitmap bitmap){
        super(bitmap);
    }

    public void setSpeed(float speed){
        this.speed = speed;
    }

    public float getSpeed(){
        return speed;
    }

    @Override
    protected void antesDeDesenhar(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            //Mova os pixels de velocidade na direção do eixo y
            move(0, speed * gameView.getDensity());
        }
    }

    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView){
        if(!isDestroyed()){
            //Verifique se Sprite está fora do escopo da tela, se for excedido, destrua Sprite
            RectF canvasRecF = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
            RectF spriteRecF = getRectF();
            if(!RectF.intersects(canvasRecF, spriteRecF)){
                destroy();
            }
        }
    }
}