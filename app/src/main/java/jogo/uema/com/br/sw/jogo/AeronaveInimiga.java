package jogo.uema.com.br.sw.jogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.List;

/**
 * O avião inimigo, movendo-se de cima para baixo ao longo de uma linha reta
 */
public class AeronaveInimiga extends AutoSprite {

    private int power = 1;//Capacidade anti-ataque da aeronave inimiga
    private int value = 0;//Bata uma pontuação hostil

    public AeronaveInimiga(Bitmap bitmap){
        super(bitmap);
    }

    public void setPower(int power){
        this.power = power;
    }

    public int getPower(){
        return power;
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    @Override
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView) {
        super.afterDraw(canvas, paint, gameView);

        //Depois que o desenho terminar, verifique se é atingido por uma bala.
        if(!isDestroyed()){
            //Depois de completar o desenho do inimigo,
            // deve ser julgado se é atingido por uma bala.

            List<Balas> bullets = gameView.getAliveBullets();
            for(Balas bullet : bullets){
                //Determine se o avião inimigo cruza a bala
                Point p = getCollidePointWithOther(bullet);
                if(p != null){
                    //Se houver uma interseção, significa que a bala atingiu o avião.
                    bullet.destroy();
                    power--;
                    if(power <= 0){
                        //A aeronave do inimigo não tem energia.
                        explode(gameView);
                        return;
                    }
                }
            }
        }
    }

    //Cria uma explosão e destrói o inimigo
    public void explode(GameView gameView){
        //Criar um efeito de explosão
        float centerX = getX() + getWidth() / 2;
        float centerY = getY() + getHeight() / 2;
        Bitmap bitmap = gameView.getExplosionBitmap();
        Explosao explosion = new Explosao(bitmap);
        explosion.centerTo(centerX, centerY);
        gameView.addSprite(explosion);

        //Depois de criar o efeito de explosão,
        // adicione pontuações ao GameView e destrua as aeronaves inimigas
        gameView.addScore(value);
        destroy();
    }
}