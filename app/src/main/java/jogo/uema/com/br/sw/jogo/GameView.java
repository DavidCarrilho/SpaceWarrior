package jogo.uema.com.br.sw.jogo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jogo.uema.com.br.sw.R;


public class GameView extends View {

    //0:aviaoDeCombate
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

    private Paint paint;
    private Paint textPaint;
    private AeronaveDeCombate aviaoDeCombate = null;
    private List<Sprite> sprites = new ArrayList<Sprite>();
    private List<Sprite> spritesNeedAdded = new ArrayList<Sprite>();
    private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private float density = getResources().getDisplayMetrics().density;//Densidade da tela
    public static final int STATUS_GAME_STARTED = 1;//O jogo começa
    public static final int STATUS_GAME_PAUSED = 2;//Pausar jogo
    public static final int STATUS_GAME_OVER = 3;//Fim do jogo
    public static final int STATUS_GAME_DESTROYED = 4;//Destruição do jogo
    private int status = STATUS_GAME_DESTROYED;//Inicialmente destruído
    private long frame = 0;//Número inicial total de frames desenhados
    private long score = 0;//Pontuação inicial total
    private float fontSize = 12;//O tamanho de fonte padrão para desenho de texto no canto superior esquerdo
    private float fontSize2 = 20;//Tamanho da fonte usado para desenhar texto no Dialog at Game Over
    private float borderSize = 2;//Borda do Jogo Sobre o Diálogo
    private Rect continueRect = new Rect();//"Continuar", botão "Reiniciar" do Rect

    //Variáveis ​​relacionadas ao evento de toque
    private static final int TOUCH_MOVE = 1;//Movel
    private static final int TOUCH_SINGLE_CLICK = 2;//Clique em
    private static final int TOUCH_DOUBLE_CLICK = 3;//Clique duas vezes
    //Um evento de clique é composto de dois eventos: DOWN e UP. Supondo que o intervalo de baixo para cima seja inferior a 200 milissegundos,
    // consideramos que ocorreu um evento de clique.
    private static final int singleClickDurationTime = 200;
    //Um evento de duplo clique é sintetizado por dois eventos de clique. Entre dois eventos de clique é inferior a 300 milissegundos,
    // nós pensamos que ocorreu um evento de duplo clique.
    private static final int doubleClickDurationTime = 300;
    private long lastSingleClickTime = -1;//Quando o último clique aconteceu
    private long touchDownTime = -1;//Quando o contato é pressionado
    private long touchUpTime = -1;//Quando o contato salta
    private float touchX = -1;//A coordenada x do contato
    private float touchY = -1;//A coordenada y do contato

    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GameView, defStyle, 0);
        a.recycle();
        //Inicializar Paint
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        //Defina textPaint, definido como anti-alias e negrito
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG);
        textPaint.setColor(0xff000000);
        fontSize = textPaint.getTextSize();
        fontSize *= density;
        fontSize2 *= density;
        textPaint.setTextSize(fontSize);
        borderSize *= density;
    }

    public void start(int[] bitmapIds){
        destroy();
        for(int bitmapId : bitmapIds){
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), bitmapId);
            bitmaps.add(bitmap);
        }
        comecarQuandoBitmapsPronto();
    }

    private void comecarQuandoBitmapsPronto(){
        aviaoDeCombate = new AeronaveDeCombate(bitmaps.get(0));
        //Defina o jogo para começar
        status = STATUS_GAME_STARTED;
        postInvalidate();
    }

    private void restart(){
        destruirNaoRefazerBitmaps();
        comecarQuandoBitmapsPronto();
    }

    public void pause(){
        //Defina o jogo para pausar
        status = STATUS_GAME_PAUSED;
    }

    private void resume(){
        //Defina o jogo para Retornar
        status = STATUS_GAME_STARTED;
        postInvalidate();
    }

    private long getScore(){
        //Obter o resultado do jogo
        return score;
    }

    /*-------------------------------desenhar-------------------------------------*/

    @Override
    protected void onDraw(Canvas canvas) {
        //Verificamos em cada quadro se as condições para atrasar o evento de clique são atendidas
        if(isSingleClick()){
            onSingleClick(touchX, touchY);
        }

        super.onDraw(canvas);

        if(status == STATUS_GAME_STARTED){
            desenharJogoIniciado(canvas);
        }else if(status == STATUS_GAME_PAUSED){
            desenarJogoPausado(canvas);
        }else if(status == STATUS_GAME_OVER){
            desenharGameOver(canvas);
        }
    }

    //Desenho de um jogo correndo
    private void desenharJogoIniciado(Canvas canvas){

        desenharPontuacaoBombas(canvas);

        //Na primeira vez que você desenha, mova o lutador para o fundo da tela, no centro da direção horizontal
        if(frame == 0){
            float centerX = canvas.getWidth() / 2;
            float centerY = canvas.getHeight() - aviaoDeCombate.getHeight() / 2;
            aviaoDeCombate.centerTo(centerX, centerY);
        }

        //Adicionar spritesNeedAdded a sprites
        if(spritesNeedAdded.size() > 0){
            sprites.addAll(spritesNeedAdded);
            spritesNeedAdded.clear();
        }

        //Examine a situação em que o lutador correu para a frente da bala
        destruirBalasFrenteAeronavesCombate();

        //Remova o já destruído antes do desenho Sprite
        removeDestroyedSprites();

        //Agregado aleatoriamente a cada 30 quadrosSprite
        if(frame % 30 == 0){
            createRandomSprites(canvas.getWidth());
        }
        frame++;

        //Traverses sprites para desenhar aviões inimigos, balas, recompensas e efeitos de explosão
        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()){
            Sprite s = iterator.next();

            if(!s.isDestroyed()){
                //O método de destruição pode ser chamado dentro do método de desenho do sprite
                s.desenhar(canvas, paint, this);
            }

            //Precisamos determinar se o Sprite foi destruído após a execução do método de desenho.
            if(s.isDestroyed()){
                //Se Sprite for destruído, remova-o de Sprites
                iterator.remove();
            }
        }

        if(aviaoDeCombate != null){
            //Último apontador Last desenhar combor
            aviaoDeCombate.desenhar(canvas, paint, this);
            if(aviaoDeCombate.isDestroyed()){
                //Se o lutador for atingido e destruído, o jogo acabou
                status = STATUS_GAME_OVER;
            }
            //Invoca o método postInvalidate () para tornar o View continuado a renderizar, obtendo efeitos dinâmicos
            postInvalidate();
        }
    }

    //Desenhe um jogo pausado
    private void desenarJogoPausado(Canvas canvas){
        desenharPontuacaoBombas(canvas);

        //Chame o método OnDraw do Sprite em vez do método de desenho para que o Sprite estático possa ser renderizado sem alterar a posição do sprite
        for(Sprite s : sprites){
            s.onDraw(canvas, paint, this);
        }
        if(aviaoDeCombate != null){
            aviaoDeCombate.onDraw(canvas, paint, this);
        }

        //Diálogo de desenho, pontuação de exibição
        desenharQuadroPontuacao(canvas, "Continuar");

        if(lastSingleClickTime > 0){
            postInvalidate();
        }
    }

    //Desenhe o jogo no estado final
    private void desenharGameOver(Canvas canvas){
        //Game Over Depois de desenhar uma janela pop-up para mostrar o resultado final
       //ImageView gameOver = new (ImageView)findViewById()
       // TextView gameOver = new TextView("Game Over");
        //ImageView gameOver;
        //gameOver = (ImageView)findViewById(R.id.imageView);

        desenharQuadroPontuacao(canvas, "Reiniciar");

        if(lastSingleClickTime > 0){
            postInvalidate();
        }
    }

    private void desenharQuadroPontuacao(Canvas canvas, String operation){
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        //Armazenar valores brutos
        float originalFontSize = textPaint.getTextSize();
        Paint.Align originalFontAlign = textPaint.getTextAlign();
        int originalColor = paint.getColor();
        Paint.Style originalStyle = paint.getStyle();
        /*
        W = 360
        w1 = 20
        w2 = 320
        buttonWidth = 140
        buttonHeight = 42
        H = 558
        h1 = 150
        h2 = 60
        h3 = 124
        h4 = 76
        */
        int w1 = (int)(20.0 / 360.0 * canvasWidth);
        int w2 = canvasWidth - 2 * w1;
        int buttonWidth = (int)(140.0 / 360.0 * canvasWidth);

        int h1 = (int)(150.0 / 558.0 * canvasHeight);
        int h2 = (int)(60.0 / 558.0 * canvasHeight);
        int h3 = (int)(124.0 / 558.0 * canvasHeight);
        int h4 = (int)(76.0 / 558.0 * canvasHeight);
        int buttonHeight = (int)(42.0 / 558.0 * canvasHeight);

        canvas.translate(w1, h1);
        //Desenhe a cor do fundo
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFD7DDDE);
        Rect rect1 = new Rect(0, 0, w2, canvasHeight - 2 * h1);
        canvas.drawRect(rect1, paint);
        //Desenhe uma borda
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xFF515151);
        paint.setStrokeWidth(borderSize);
        //paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawRect(rect1, paint);
        //Texto de desenho "Marcas de aeronavess"
        textPaint.setTextSize(fontSize2);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Pontos", w2 / 2, (h2 - fontSize2) / 2 + fontSize2, textPaint);
        //Desenhe as linhas horizontais em "Aircraft Wars Score"
        canvas.translate(0, h2);
        canvas.drawLine(0, 0, w2, 0, paint);
        //Desenhe pontuações reais
        String allScore = String.valueOf(getScore());
        canvas.drawText(allScore, w2 / 2, (h3 - fontSize2) / 2 + fontSize2, textPaint);
        //Desenhe as linhas horizontais abaixo da pontuação
        canvas.translate(0, h3);
        canvas.drawLine(0, 0, w2, 0, paint);
        //Borda do botão de desenho
        Rect rect2 = new Rect();
        rect2.left = (w2 - buttonWidth) / 2;
        rect2.right = w2 - rect2.left;
        rect2.top = (h4 - buttonHeight) / 2;
        rect2.bottom = h4 - rect2.top;
        canvas.drawRect(rect2, paint);
        //Desenhe o texto "continuar" ou "reiniciar"
        canvas.translate(0, rect2.top);
        canvas.drawText(operation, w2 / 2, (buttonHeight - fontSize2) / 2 + fontSize2, textPaint);
        continueRect = new Rect(rect2);
        continueRect.left = w1 + rect2.left;
        continueRect.right = continueRect.left + buttonWidth;
        continueRect.top = h1 + h2 + h3 + rect2.top;
        continueRect.bottom = continueRect.top + buttonHeight;

        //Redefinir
        textPaint.setTextSize(originalFontSize);
        textPaint.setTextAlign(originalFontAlign);
        paint.setColor(originalColor);
        paint.setStyle(originalStyle);
    }

    //Desenhe a pontuação no canto superior esquerdo
    //e o número de bombas no canto inferior esquerdo
    private void desenharPontuacaoBombas(Canvas canvas){
        //Desenhe o botão de pausa no canto superior esquerdo
        Bitmap pauseBitmap = status == STATUS_GAME_STARTED ? bitmaps.get(9) : bitmaps.get(10);
        RectF pauseBitmapDstRecF = getPauseBitmapDstRecF();
        float pauseLeft = pauseBitmapDstRecF.left;
        float pauseTop = pauseBitmapDstRecF.top;
        canvas.drawBitmap(pauseBitmap, pauseLeft, pauseTop, paint);
        //Desenhe o número total de pontos no canto superior esquerdo
        float scoreLeft = pauseLeft + pauseBitmap.getWidth() + 20 * density;
        float scoreTop = fontSize + pauseTop + pauseBitmap.getHeight() / 2 - fontSize / 2;
        canvas.drawText(score + "", scoreLeft, scoreTop, textPaint);

        //Desenhe o canto inferior esquerdo
        if(aviaoDeCombate != null && !aviaoDeCombate.isDestroyed()){
            int bombCount = aviaoDeCombate.getBombCount();
            if(bombCount > 0){
                //Desenhe a bomba no canto inferior esquerdo
                Bitmap bombBitmap = bitmaps.get(11);
                float bombTop = canvas.getHeight() - bombBitmap.getHeight();
                canvas.drawBitmap(bombBitmap, 0, bombTop, paint);
                //Desenhe o número de bombas no canto inferior esquerdo
                float bombCountLeft = bombBitmap.getWidth() + 10 * density;
                float bombCountTop = fontSize + bombTop + bombBitmap.getHeight() / 2 - fontSize / 2;
                canvas.drawText("X " + bombCount, bombCountLeft, bombCountTop, textPaint);
            }
        }
    }

    //Examine a situação em que o Jogador correu para a frente da bala
    private void destruirBalasFrenteAeronavesCombate(){
        if(aviaoDeCombate != null){
            float aircraftY = aviaoDeCombate.getY();
            List<Balas> aliveBullets = getAliveBullets();
            for(Balas bullet : aliveBullets){
                //Se o Jogador correu na frente da bala, então destrua a bala
                if(aircraftY <= bullet.getY()){
                    bullet.destroy();
                }
            }
        }
    }

    //Remova os sprites que foram destruídos
    private void removeDestroyedSprites(){
        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()){
            Sprite s = iterator.next();
            if(s.isDestroyed()){
                iterator.remove();
            }
        }
    }

    //Gere um Sprite aleatório
    private void createRandomSprites(int canvasWidth){
        Sprite sprite = null;
        int speed = 2;
        //callTime indica o número de vezes que o método createRandomSprites foi chamado
        int callTime = Math.round(frame / 30);
        if((callTime + 1) % 25 == 0){
            //Enviar prêmios de itens
            if((callTime + 1) % 50 == 0){
                //Enviar uma bomba
                sprite = new PremioDeBomba(bitmaps.get(7));
            }
            else{
                //Enviar balas duplas
                sprite = new PremioDeBalas(bitmaps.get(8));
            }
        }
        else{
            //Enviar aeronave inimiga
            int[] nums = {0,0,0,0,0,1,0,0,1,0,0,0,0,1,1,1,1,1,1,2};
            int index = (int) Math.floor(nums.length* Math.random());
            int type = nums[index];
            if(type == 0){
                //Pequeno inimigo
                sprite = new AeronaveInimigaPequena(bitmaps.get(4));
            }
            else if(type == 1){
                //Avião inimigo
                sprite = new AeronaveInimigaMedia(bitmaps.get(5));
            }
            else if(type == 2){
                //Avião inimigo
                sprite = new AeronaveInimigaGrande(bitmaps.get(6));
            }
            if(type != 2){
                if(Math.random() < 0.33){
                    speed = 4;
                }
            }
        }

        if(sprite != null){
            float spriteWidth = sprite.getWidth();
            float spriteHeight = sprite.getHeight();
            float x = (float)((canvasWidth - spriteWidth)* Math.random());
            float y = -spriteHeight;
            sprite.setX(x);
            sprite.setY(y);
            if(sprite instanceof AutoSprite){
                AutoSprite autoSprite = (AutoSprite)sprite;
                autoSprite.setSpeed(speed);
            }
            addSprite(sprite);
        }
    }

    /*-------------------------------touch do Jogo------------------------------------*/

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //Ao chamar o método resolveTouchType, obtemos o tipo de evento que queremos
        //Observe que o método resolveTouchType não retorna o tipo TOUCH_SINGLE_CLICK
        //Chamaremos o método isSingleClick para detectar se um evento de clique é
        // acionado sempre que o método onDraw é executado.
        int touchType = resolveTouchType(event);
        if(status == STATUS_GAME_STARTED){
            if(touchType == TOUCH_MOVE){
                if(aviaoDeCombate != null){
                    aviaoDeCombate.centerTo(touchX, touchY);
                }
            }else if(touchType == TOUCH_DOUBLE_CLICK){
                if(status == STATUS_GAME_STARTED){
                    if(aviaoDeCombate != null){
                        //Um clique duplo fará com que o combate use bomba
                        aviaoDeCombate.bomb(this);
                    }
                }
            }
        }else if(status == STATUS_GAME_PAUSED){
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }else if(status == STATUS_GAME_OVER){
            if(lastSingleClickTime > 0){
                postInvalidate();
            }
        }
        return true;
    }

    //Síntese dos tipos de eventos que queremos
    private int resolveTouchType(MotionEvent event){
        int touchType = -1;
        int action = event.getAction();
        touchX = event.getX();
        touchY = event.getY();
        if(action == MotionEvent.ACTION_MOVE){
            long deltaTime = System.currentTimeMillis() - touchDownTime;
            if(deltaTime > singleClickDurationTime){
                //Movimento de contato
                touchType = TOUCH_MOVE;
            }
        }else if(action == MotionEvent.ACTION_DOWN){
            //Entre em contato
            touchDownTime = System.currentTimeMillis();
        }else if(action == MotionEvent.ACTION_UP){
            //Salto de contato
            touchUpTime = System.currentTimeMillis();
            //Calcule a diferença de tempo entre o momento em que o contato é
            // pressionado até o salto de contato
            long downUpDurationTime = touchUpTime - touchDownTime;
            //Se a diferença de tempo entre este contato pressionar e levantar é
            // menor que a diferença de tempo especificada por um evento de um único clique,
            //Então, pensamos que houve um único clique
            if(downUpDurationTime <= singleClickDurationTime){
                //Calcule a diferença horária entre o clique e o último clique
                long twoClickDurationTime = touchUpTime - lastSingleClickTime;

                if(twoClickDurationTime <=  doubleClickDurationTime){
                    //Se a diferença horária entre dois cliques for
                    // menor que a diferença horária entre a execução de eventos de duplo clique,
                    //Então, pensamos que houve um evento de duplo clique
                    touchType = TOUCH_DOUBLE_CLICK;
                    //Repor variáveis
                    lastSingleClickTime = -1;
                    touchDownTime = -1;
                    touchUpTime = -1;
                }else{
                    //desta vez, mas nenhum evento de duplo clique foi formado,
                    // então não vamos ativar esse evento de clique.
                    //Devemos olhar nos milésimos de segundo do DoubleClickDurationTime
                    // para ver se formamos um segundo evento de clique novamente
                    //Se um segundo evento de clique for formado naquele momento,
                    // então nós sintetizamos um evento de duplo clique com este evento de clique.
                    //Caso contrário, esse evento de clique será ativado após o segundo milhegundo do segundoClickDurationTime
                    lastSingleClickTime = touchUpTime;
                }
            }
        }
        return touchType;
    }

    //Chame este método onDraw para verificar se um evento de clique ocorre em cada quadro
    private boolean isSingleClick(){
        boolean singleClick = false;
        //erificamos se o evento do último clique satisfaz a condição que desencadeou o evento de
        // clique depois de passar nos milissegundos do DoubleClickDurationTime.
        if(lastSingleClickTime > 0){
            //Calcule a diferença de tempo do último evento de clique no momento atual
            long deltaTime = System.currentTimeMillis() - lastSingleClickTime;

            if(deltaTime >= doubleClickDurationTime){
                //Se a diferença horária exceder o tempo necessário para um evento de duplo clique,
                //Então, o evento de clique que deveria ter ocorrido antes do atraso é acionado neste momento
                singleClick = true;
                //Repor variáveis
                lastSingleClickTime = -1;
                touchDownTime = -1;
                touchUpTime = -1;
            }
        }
        return singleClick;
    }

    private void onSingleClick(float x, float y){
        if(status == STATUS_GAME_STARTED){
            if(isClickPause(x, y)){
                //Clique no botão de pausa
                pause();
            }
        }else if(status == STATUS_GAME_PAUSED){
            if(isClickContinueButton(x, y)){
                //Clique no botão "Continuar"
                resume();
            }
        }else if(status == STATUS_GAME_OVER){
            if(isClickRestartButton(x, y)){
                //Clique no botão "Reiniciar"
                restart();
            }
        }
    }

    //Você clicou no botão de pausa no canto superior esquerdo?
    private boolean isClickPause(float x, float y){
        RectF pauseRecF = getPauseBitmapDstRecF();
        return pauseRecF.contains(x, y);
    }

    //Você clicou em "Continuar" no estado de pausa
    private boolean isClickContinueButton(float x, float y){
        return continueRect.contains((int)x, (int)y);
    }

    //Se deseja clicar no botão "reiniciar" no estado GAME OVER
    private boolean isClickRestartButton(float x, float y){
        return continueRect.contains((int)x, (int)y);
    }

    private RectF getPauseBitmapDstRecF(){
        Bitmap pauseBitmap = status == STATUS_GAME_STARTED ? bitmaps.get(9) : bitmaps.get(10);
        RectF recF = new RectF();
        recF.left = 15 * density;
        recF.top = 15 * density;
        recF.right = recF.left + pauseBitmap.getWidth();
        recF.bottom = recF.top + pauseBitmap.getHeight();
        return recF;
    }

    /*-------------------------------destruir------------------------------------*/

    private void destruirNaoRefazerBitmaps(){
        //Defina o jogo para destruir
        status = STATUS_GAME_DESTROYED;

        //Redefinir quadro
        frame = 0;

        //Pontuação de restituição
        score = 0;

        //Lutador de destruição
        if(aviaoDeCombate != null){
            aviaoDeCombate.destroy();
        }
        aviaoDeCombate = null;

        //Destruição de aeronaves inimigas, balas, recompensas, explosões
        for(Sprite s : sprites){
            s.destroy();
        }
        sprites.clear();
    }

    public void destroy(){
        destruirNaoRefazerBitmaps();

        //Libere recursos do Bitmap
        for(Bitmap bitmap : bitmaps){
            bitmap.recycle();
        }
        bitmaps.clear();
    }

    /*-------------------------------Métodos Públicos-----------------------------------*/

    //Adicione Sprite a Sprites
    public void addSprite(Sprite sprite){
        spritesNeedAdded.add(sprite);
    }

    //Adicionar uma pontuação
    public void addScore(int value){
        score += value;
    }

    public int getStatus(){
        return status;
    }

    public float getDensity(){
        return density;
    }

    public Bitmap getYellowBulletBitmap(){
        return bitmaps.get(2);
    }

    public Bitmap getBlueBulletBitmap(){
        return bitmaps.get(3);
    }

    public Bitmap getExplosionBitmap(){
        return bitmaps.get(1);
    }

    //Obter uma aeronave inimiga ativa
    public List<AeronaveInimiga> getAliveEnemyPlanes(){
        List<AeronaveInimiga> enemyPlanes = new ArrayList<AeronaveInimiga>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof AeronaveInimiga){
                AeronaveInimiga sprite = (AeronaveInimiga)s;
                enemyPlanes.add(sprite);
            }
        }
        return enemyPlanes;
    }

    //Obtenha recompensas de bomba ativas
    public List<PremioDeBomba> getAliveBombAwards(){
        List<PremioDeBomba> bombAwards = new ArrayList<PremioDeBomba>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof PremioDeBomba){
                PremioDeBomba bombAward = (PremioDeBomba)s;
                bombAwards.add(bombAward);
            }
        }
        return bombAwards;
    }

    //Obtenha recompensas de bala ativas
    public List<PremioDeBalas> getAliveBulletAwards(){
        List<PremioDeBalas> bulletAwards = new ArrayList<PremioDeBalas>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof PremioDeBalas){
                PremioDeBalas bulletAward = (PremioDeBalas)s;
                bulletAwards.add(bulletAward);
            }
        }
        return bulletAwards;
    }

    //Obter marcadores ativos
    public List<Balas> getAliveBullets(){
        List<Balas> bullets = new ArrayList<Balas>();
        for(Sprite s : sprites){
            if(!s.isDestroyed() && s instanceof Balas){
                Balas bullet = (Balas)s;
                bullets.add(bullet);
            }
        }
        return bullets;
    }
}