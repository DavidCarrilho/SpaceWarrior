package jogo.uema.com.br.sw.jogo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

import java.util.List;

/**
 * Classe que o jogaodor, pode mudar de posição por interação
 */
public class AeronaveDeCombate extends Sprite {
    private boolean atingido = false;//Identifica se o jogador foi atingido
    private int bombaPremioCount = 0;//O número de bombas que podem ser usadas

    //Relação de balas
    private boolean single = true;//Seja ou não o logotipo, é uma única bala
    private int doubleTime = 0;//O número de vezes que foi desenhado com balas duplas
    private int maxDoubleTime = 140;//Número máximo de sorteios usando balas duplas

    //Piscando depois de ser atingido
    private long beginFlushFrame = 0;//Para começar a piscar jogador no primeiro quadro beginFlushFrame
    private int flushTime = 0;//O número de vezes que foram piscados
    private int flushFrequencia = 16;//Transforma a visibilidade do jogador a cada 16 quadros enquanto cintilam
    private int maxFlushTime = 10;//O número máximo de flashes

    public AeronaveDeCombate(Bitmap bitmap){
        super(bitmap);
    }

    @Override
    protected void antesDeDesenhar(Canvas canvas, Paint paint, GameView gameView) {
        if(!isDestroyed()){
            //Certifique-se de que o jogador esteja completamente dentro da tela
            validatePosition(canvas);

            //Disparando balas a cada 7 frames
            if(getFrame() % 7 == 0){
                fight(gameView);
            }
        }
    }

    //Certifique-se de que o jogador esteja completamente localizado Dentro da tela
    private void validatePosition(Canvas canvas){
        if(getX() < 0){
            setX(0);
        }
        if(getY() < 0){
            setY(0);
        }
        RectF rectF = getRectF();
        int canvasWidth = canvas.getWidth();
        if(rectF.right > canvasWidth){
            setX(canvasWidth - getWidth());
        }
        int canvasHeight = canvas.getHeight();
        if(rectF.bottom > canvasHeight){
            setY(canvasHeight - getHeight());
        }
    }

    //Lançar balas
    public void fight(GameView gameView){
        //Se o jogador for atingido ou destruído, as balas não serão disparadas
        if(atingido || isDestroyed()){
            return;
        }

        float x = getX() + getWidth() / 2;
        float y = getY() - 5;
        if(single){
            //Single yellow ballet disparando em modo único
            Bitmap yellowBulletBitmap = gameView.getYellowBulletBitmap();
            Balas yellowBullet = new Balas(yellowBulletBitmap);
            yellowBullet.moveTo(x, y);
            gameView.addSprite(yellowBullet);
        }
        else{
            //Lançar duas balas azuis no modo duplo
            float offset = getWidth() / 4;
            float leftX = x - offset;
            float rightX = x + offset;
            Bitmap blueBulletBitmap = gameView.getBlueBulletBitmap();

            Balas leftBlueBullet = new Balas(blueBulletBitmap);
            leftBlueBullet.moveTo(leftX, y);
            gameView.addSprite(leftBlueBullet);

            Balas rightBlueBullet = new Balas(blueBulletBitmap);
            rightBlueBullet.moveTo(rightX, y);
            gameView.addSprite(rightBlueBullet);

            doubleTime++;
            if(doubleTime >= maxDoubleTime){
                single = true;
                doubleTime = 0;
            }
        }
    }

    //Se o jogador for atingido, realize uma explosão
    //Especificamente, primeiro esconda o jogador e, em seguida,
    // cria um efeito de explosão. A explosão é completada com 28 quadros de renderização.
    //Depois que o efeito de explosão é totalmente renderizado, o efeito de explosão desaparece
    //Então os jogadores entram no modo de flash e o jogador destrói após um certo número de flashes.
    protected void afterDraw(Canvas canvas, Paint paint, GameView gameView){
        if(isDestroyed()){
            return;
        }

        //Quando o jogador atualmente não é atingido,
        // é necessário determinar se ele será atingido pelos inimiga.
        if(!atingido){
            List<AeronaveInimiga> enemies = gameView.getAliveEnemyPlanes();
            for(AeronaveInimiga enemyPlane : enemies){
                Point p = getCollidePointWithOther(enemyPlane);
                if(p != null){
                    //P é o ponto de colisão entre o jogador e o inimigo.
                    // Se p não é nulo, isso indica que o jogador foi atingido pelo inimigo.
                    explode(gameView);
                    break;
                }
            }
        }

        //beginFlushFrame tem um valor inicial de 0, indicando nenhum modo de flash
        //Se beginFlushFrame for maior que 0, significa que
        // se o frame beginFlushFrame entrar no modo intermitente
        if(beginFlushFrame > 0){
            long frame = getFrame();
            //Se o número de frame atual for maior ou igual a beginFlushFrame,
            // ele indica que o jogador entrou no estado de piscamento antes de ser destruído.
            if(frame >= beginFlushFrame){
                if((frame - beginFlushFrame) % flushFrequencia == 0){
                    boolean visible = getVisibility();
                    setVisibility(!visible);
                    flushTime++;
                    if(flushTime >= maxFlushTime){
                        //Se o jogador piscar mais do que o número máximo de flashes, jogador o lutador
                        destroy();
                        //Game.gameOver();
                    }
                }
            }
        }

        //Verifique se você tem adereços sem ser atingido
        if(!atingido){
            //Verifique se você obtém um suporte de bomba
            List<PremioDeBomba> bombAwards = gameView.getAliveBombAwards();
            for(PremioDeBomba bombAward : bombAwards){
                Point p = getCollidePointWithOther(bombAward);
                if(p != null){
                    bombaPremioCount++;
                    bombAward.destroy();
                    //Game.receiveBombAward();
                }
            }

            //Verifique se você tem itens de bala
            List<PremioDeBalas> premioBalas = gameView.getAliveBulletAwards();
            for(PremioDeBalas premioBala : premioBalas){
                Point p = getCollidePointWithOther(premioBala);
                if(p != null){
                    premioBala.destroy();
                    single = false;
                    doubleTime = 0;
                }
            }
        }
    }

    //Explosão de jogador
    private void explode(GameView gameView){
        if(!atingido){
            atingido = true;
            setVisibility(false);
            float centerX = getX() + getWidth() / 2;
            float centerY = getY() + getHeight() / 2;
            Explosao explosion = new Explosao(gameView.getExplosionBitmap());
            explosion.centerTo(centerX, centerY);
            gameView.addSprite(explosion);
            beginFlushFrame = getFrame() + explosion.getExplodeDurationFrame();
        }
    }

    //Obter o número de bombas disponíveis
    public int getBombCount(){
        return bombaPremioCount;
    }

    //Bomba de uso de jogador
    public void bomb(GameView gameView){
        if(atingido || isDestroyed()){
            return;
        }

        if(bombaPremioCount > 0){
            List<AeronaveInimiga> enemyPlanes = gameView.getAliveEnemyPlanes();
            for(AeronaveInimiga enemyPlane : enemyPlanes){
                enemyPlane.explode(gameView);
            }
            bombaPremioCount--;
        }
    }

    public boolean isAtingido(){
        return atingido;
    }

    public void setNotCollide(){
        atingido = false;
    }
}