package jogo.uema.com.br.sw.jogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Prêmios
 */
public class Premios extends AutoSprite {
    public static int STATUS_DOWN1 = 1;
    public static int STATUS_UP2 = 2;
    public static int STATUS_DOWN3 = 3;

    private int status = STATUS_DOWN1;

    public Premios(Bitmap bitmap){
        super(bitmap);
        setSpeed(7);
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        //Não chama o método super.afterDraw no AfterDraw
        if(!isDestroyed()){
            //Mude a direção ou a velocidade depois de desenhar um certo número de vezes
            int canvasHeight = canvas.getHeight();
            if(status != STATUS_DOWN3){
                float maxY = getY() + getHeight();
                if(status == STATUS_DOWN1){
                    //Primeiro para baixo
                    if(maxY >= canvasHeight * 0.25){
                        //Quando a primeira queda no valor crítico muda de direção, para cima
                        setSpeed(-5);
                        status = STATUS_UP2;
                    }
                }
                else if(status == STATUS_UP2){
                    //Segundo para cima
                    if(maxY+this.getSpeed() <= 0){
                        //Mude a direção quando o segundo sobe para o valor crítico, para baixo
                        setSpeed(13);
                        status = STATUS_DOWN3;
                    }
                }
            }
            if(status == STATUS_DOWN3){
                if(getY() >= canvasHeight){
                    destroy();
                }
            }
        }
    }
}