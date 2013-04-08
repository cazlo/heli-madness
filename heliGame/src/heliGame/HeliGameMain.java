package heliGame;

//import heliGame.Helicopter.CanvasStates;
import heliGame.Helicopter.MovementStates;
import heliGame.Helicopter.ThrottleStates;
import heliGame.Level.LevelNotLoadedException;
//import heliSim.Level.LevelStates;
//
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

public final class HeliGameMain extends JFrame {

	/**
	 * 
	 */
	static final boolean DEBUG = true;//TODO: REMOVE THIS BEFORE TURNING IT IN
	
	//define constants for the game
	static final int GAME_WIDTH = 1200; //width of game window
	static final int GAME_HEIGHT = 600; // height of game window
	static final int UPDATE_RATE = 60; //number of game updates per second
	static final long UPDATE_PERIOD = 1000000000L / UPDATE_RATE;  // time (nanoseconds) that loop thread is paused between game updates
	static final int NUM_LEVELS = 1;
	static final int MIDDLE_OF_FRAME = (GAME_WIDTH / 2) - (Helicopter.HELI_WIDTH/2);//the location at which the heli sprite stops moving, and the level starts moving
        
        //booleans for keypress handling
        volatile boolean bUpPressed = false;
        volatile boolean bDownPressed = false;
        volatile boolean bLeftPressed = false;
        volatile boolean bRightPressed = false;
        volatile boolean bHelpPressed = false;
        volatile boolean bPausePressed = false;
        
    	// enumerate states of game to make for more readable code
	static enum GameStates {
            INITIALIZED, PLAYING, PAUSED, GAMEOVER, NEXTLEVEL, EXPLODING, WIN
	}
        
        public static enum CanvasStates{// the state of the heli with respect to the middle canvas
            //STATIONARY means the heli is not moving(its in the middle of the canvas), and the level is moving
            //MOVING_IN_LEFT means it is moving in any direction on the left side of the middle point of the canvas
            //MOVING_IN_RIGHT means it is moving in any direction on the right side of the middle point of the canvas
            STATIONARY, MOVING_IN_LEFT, MOVING_IN_RIGHT
	}
	
        private CanvasStates canvasStatus;
	private GameStates gameState; //current state of the game
		
	//define instance variables of game objects
	private Helicopter heli;
	private Explosion heliExplosion, birdExplosion;
	private Level currentLevel;
        private GameOverMessage gameOverMessage;
        private HelpMenu helpMenu;
        
        //other stuff for game
	private int currentLevelNumber = 1;
	private int score;
        //private boolean win = false;
	//Ground groundSprite;
	
	//define handler for panels of game
	private GamePanel gamePanel;
	private OptionsPanel optionsPanel;
	private InstrumentPanel instrumentPanel;
	private JPanel bottomPanel;
		
	//constructor which initializes the game's objects and UI components
	public HeliGameMain(){
            init();

            //set up UI components
            //this.setContentPane(new JDesktopPane());
            Container contentPane = this.getContentPane();
            contentPane.setLayout(new BorderLayout() );
            gamePanel = new GamePanel();
            gamePanel.setPreferredSize(new Dimension(GAME_WIDTH,GAME_HEIGHT));
            contentPane.add(gamePanel,BorderLayout.CENTER);

            bottomPanel = new JPanel(new FlowLayout());
            optionsPanel = new OptionsPanel();
            instrumentPanel = new InstrumentPanel();
            bottomPanel.add(instrumentPanel);
            bottomPanel.add(optionsPanel);		
            contentPane.add(bottomPanel, BorderLayout.SOUTH);

            this.setTitle("Helicopter Game");
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.setResizable(false);

            this.pack();
            this.setVisible(true);

            helpMenu = new HelpMenu();
            helpMenu.setLocationRelativeTo(this);

            gameState = GameStates.PAUSED;
            start();
	}
		
	public void init() {
            // initialize game objects
            //this.win = false;
            this.currentLevelNumber = 1;
            try{
                currentLevel = new Level(currentLevelNumber);
            } catch (LevelNotLoadedException e){
                System.out.println(e);
                shutdown(1);
            }

            this.score = 0;
            heli = new Helicopter(currentLevel.getStartX(),currentLevel.getStartY());
            
            if (heli.getX() < MIDDLE_OF_FRAME){
                canvasStatus = CanvasStates.MOVING_IN_LEFT;
                heli.setCanvasX(heli.getX());
            }
            else if (heli.getX() == MIDDLE_OF_FRAME){
                canvasStatus = CanvasStates.STATIONARY;
                heli.setCanvasX(heli.getX());
            }
            else{
                canvasStatus = CanvasStates.STATIONARY;
                heli.setCanvasX(MIDDLE_OF_FRAME);
            }
            birdExplosion = null;//(re)-set birdExplosion to null so it wont be drawn erraneously 
            heliExplosion = null;
            gameState = GameStates.INITIALIZED;
	}
	
	public void shutdown(int status){
            System.exit(status);
	}
	
	public void start(){
            Thread gameThread = new Thread() {
                @Override
                public void run(){
                    loop();
                }
            };

            gameThread.start();
	}
	
	private void loop(){
            long startTime, timeTaken, timeLeft;


            if (gameState == GameStates.INITIALIZED) {
                gameState = GameStates.PLAYING;
            }
            else if (gameState == GameStates.GAMEOVER || 
                     gameState == GameStates.WIN){
                init();//regenerate game objects for new game
                gameState = GameStates.PLAYING;
            }

            //loop for game
            while (true){
                startTime = System.nanoTime();
                    if (gameState != GameStates.PAUSED){
                        if (gameState == GameStates.GAMEOVER ||
                            gameState == GameStates.WIN){
                            break; //get out of main game loop if game is over
                        }
                        /*if (gameState == GameStates.EXPLODING){
                            if (heliExplosion.doneExploding()){//check to see if the explosion animation is done yet
                                gameState = GameStates.GAMEOVER;
                            }
                            else{
                                heliExplosion.updateExplosion();
                                if (birdExplosion != null){
                                    birdExplosion.updateExplosion();
                                }
                                currentLevel.updateLevel();
                            }
                        }*/
                        if (gameState == GameStates.PLAYING ||
                            gameState == GameStates.EXPLODING){
                            //update state of all game objects
                            updateGame();
                        }
                        if (gameState == GameStates.NEXTLEVEL){
                            if (currentLevelNumber == NUM_LEVELS){
                                //win = true;
                                //gameState = GameStates.GAMEOVER;
                                gameOverMessage = new GameOverMessage("All Levels Completed", score, currentLevel.getMaxScore());
                                gameState = GameStates.WIN;
                            }
                            else{
                                currentLevelNumber ++;
                                try {
                                    currentLevel = new Level(currentLevelNumber);
                                } catch (LevelNotLoadedException e) {
                                    e.printStackTrace();
                                    shutdown(1);//exit out of the program 
                                }

                                heli = new Helicopter(currentLevel.getStartX(),currentLevel.getStartY());
                                gameState = GameStates.PLAYING;
                            }
                        }
                        repaint();//refresh the frame; re-calls the paintComponent methods of each panel in the main frame
                }
                //find out how long to sleep to keep the game refreshing at REFRESH_RATE
                timeTaken = System.nanoTime() - startTime;
                timeLeft = (UPDATE_PERIOD - timeTaken) / 1000000L;  // in milliseconds
                if (timeLeft < 10){                    
                    timeLeft = 10;  //set a minimum amount of time to sleep
                }
                try{
                    Thread.sleep(timeLeft);//let other threads do thier stuff (like input thread)
                } 
                catch(InterruptedException ex){
                }
            }//end main loop

	}
	
	public void updateGame() {
            switch (gameState){
                case PLAYING:
                    heli.updateHeli();//update y and x of heli in total game plane
                    currentLevel.updateLevel();
                    updateCanvas();
                    instrumentPanel.updateInstrumentPanel();
                    collisionDetection();
                    break;
                case EXPLODING:
                    currentLevel.updateLevel();
                    if (heliExplosion.doneExploding()){//check to see if the explosion animation is done yet
                        gameState = GameStates.GAMEOVER;
                    }
                    else{
                        heliExplosion.updateExplosion();
                        if (birdExplosion != null){
                            birdExplosion.updateExplosion();
                        }
                       // currentLevel.updateLevel();
                    }
                    break;
            }
            
		
	}
	
	private void updateCanvas(){
            //----------check to see if heli is right at beginning or right at end of level--------------
            //at these points, the heli and background will swap movement states; the background stops moving
            //and the heli starts moving, or vice versa

            if (heli.willCrossBeginningChange()){
                if (heli.getXSpeed() > 0){//it is moving to the right
                    heli.setCanvasX(MIDDLE_OF_FRAME);
                    currentLevel.setXSpeed(heli.getXSpeed());
                    canvasStatus = CanvasStates.STATIONARY;
                }
                else if (heli.getXSpeed() < 0){//it is moving to the left
                    heli.setCanvasX(heli.getX());
                    currentLevel.setXSpeed(0);
                    canvasStatus = CanvasStates.MOVING_IN_LEFT;
                }
            }
            else if (heli.willCrossEndChange(currentLevel.getLevelLength())){
                if (heli.getXSpeed() > 0){
                    heli.setCanvasX(GAME_WIDTH - (currentLevel.getLevelLength() - heli.getX()));
                    currentLevel.setXSpeed(0);
                    canvasStatus = CanvasStates.MOVING_IN_RIGHT;
                }
                else if (heli.getXSpeed() < 0){
                    heli.setCanvasX(MIDDLE_OF_FRAME);
                    currentLevel.setXSpeed(heli.getXSpeed());
                    canvasStatus = CanvasStates.STATIONARY;
                }
            }
            else{//wont cross anywhere
                switch(canvasStatus){
                    case STATIONARY:
                        //don't need to update the heli's canvas x,
                        //because the background is moving, not the heli
                        break;
                    case MOVING_IN_RIGHT:
                        heli.setCanvasX(GAME_WIDTH - (currentLevel.getLevelLength() - heli.getX()));
                        break;
                    case MOVING_IN_LEFT:
                        heli.setCanvasX(heli.getX());
                        break;
            }
        }

            //------------------------------accelerate heli if falling-------------
            if (heli.willCrossAccelerationPoint()){
                    heli.setYSpeed(heli.getYSpeed() - 1);
                    heli.setFallYTop(heli.getY() );
            }

            //------------------------------keep heli confined in canvas-------------------
            if (heli.willCrossSideOfCanvas(currentLevel.getLevelLength()) ){
                if (heli.getXSpeed() > 0){//its going to cross at the end
                    heli.setX(currentLevel.getLevelLength() - Helicopter.HELI_WIDTH);
                    heli.setCanvasX(GAME_WIDTH - Helicopter.HELI_WIDTH);
                    heli.setXSpeed(0);
                    heli.setTheta(0);
                }
                else{//its going to cross at the beginning
                    heli.setX(0);
                    heli.setCanvasX(0);
                    heli.setXSpeed(0);		
                    heli.setTheta(0);
                }
            }

            if (heli.willCrossTopOfCanvas()){
                heli.setYSpeed(0);
                heli.setY(0);
            }
            else if (heli.willCrossBottomOfCanvas()){
                heli.setYSpeed(0);
                heli.setY(GAME_HEIGHT - Helicopter.HELI_HEIGHT);
            }

            //---------------sync the level drawing and the heli if they become unsynced------------------
            //TODO: fix this hack
            //fix level not drawing correctly at speed 3
            //caused by rounding error (integer division)?
            switch (canvasStatus){
                case STATIONARY:
                    break;
                case MOVING_IN_RIGHT:
                    if(currentLevel.getCurrentX() != 
                       (currentLevel.getLevelLength() - GAME_WIDTH)){
                            currentLevel.setCurrentX(currentLevel.getLevelLength() - GAME_WIDTH);
                    }
                    break;
                case MOVING_IN_LEFT:
                    if(currentLevel.getCurrentX() != 0){
                            currentLevel.setCurrentX(0);
                    }
                    break; 
            }
		
	}
	public void collisionDetection() {
		
            checkTreeCollision();
            checkBirdCollision();
            checkRingCollision();
            checkGroundCollision();

            //---------------------------check to see if within finish line------------------
            if (heli.isLanded() && currentLevel.getFinishLine().contains((Rectangle2D)heli.getCollisionShape())){
                gameState = GameStates.NEXTLEVEL;
            }
	}

        private void checkTreeCollision() {
            //------------------------------tree collision--------------------------
            //check to see if it hits a tree
            for (int treeNum = 0; treeNum < currentLevel.getTreeList().size(); treeNum++){
                if (currentLevel.getTreeList().get(treeNum).getX() >= (currentLevel.getCurrentX() - Tree.CANOPY_RADIUS * 2) &&
                    currentLevel.getTreeList().get(treeNum).getX() <= (currentLevel.getCurrentX() + GAME_WIDTH)){//only need to check visible trees
                    if (currentLevel.getTreeList().get(treeNum).intersects((Rectangle2D)heli.getCollisionShape())){
                        gameState = GameStates.EXPLODING;
                        gameOverMessage = new GameOverMessage("Collision With Tree", score, currentLevel.getMaxScore());
                        heliExplosion = new Explosion(heli.getCanvasX(),
                                                      heli.getY(),
                                                      Helicopter.HELI_WIDTH + 20,
                                                      Helicopter.HELI_HEIGHT + 20);
                        currentLevel.setXSpeed(0);
                        if (DEBUG){
                                System.out.println("Collision with tree");
                        }
                    }
                }
            }
        }
        
        private void checkBirdCollision(){
            //---------------------------bird Collision------------------
            //check to see if it hits a bird
            for (int birdNum = 0; birdNum < Bird.MAX_BIRDS; birdNum++){
                if (currentLevel.getBirds()[birdNum].getX() >= (currentLevel.getCurrentX() - Bird.BIRD_WIDTH) &&
                    currentLevel.getBirds()[birdNum].getX() <= (currentLevel.getCurrentX() + GAME_WIDTH)){//only need to check visible birds
                    if (currentLevel.getBirds()[birdNum].intersects((Rectangle2D)heli.getCollisionShape())){
                        switch(heli.getThrottleStatus()){
                            case IDLE:
                                //make bird go over the heli if it is idle and landed
                                if (heli.isLanded()){
                                    currentLevel.getBirds()[birdNum].setY(currentLevel.getBirds()[birdNum].getY() - 1);
                                    if (DEBUG){
                                            System.out.println("collision with bird");	
                                    }
                                }
                                else{
                                    gameState = GameStates.EXPLODING;
                                    gameOverMessage = new GameOverMessage("Fatal Collision With Bird", score, currentLevel.getMaxScore());
                                    currentLevel.getBirds()[birdNum].setXSpeed(0);//slow the bird so it will remain within the explosion
                                    birdExplosion = new Explosion(currentLevel.getBirds()[birdNum].getCanvasX()
                                                                 ,currentLevel.getBirds()[birdNum].getY()
                                                                 ,Bird.BIRD_WIDTH
                                                                 ,Bird.BIRD_HEIGHT);
                                                                 currentLevel.getBirds()[birdNum].setY(0-Bird.BIRD_HEIGHT);//move the bird off screen

                                    heliExplosion = new Explosion(heli.getCanvasX(),
                                    heli.getY(),
                                    Helicopter.HELI_WIDTH + 20,
                                    Helicopter.HELI_HEIGHT + 20);
                                    currentLevel.setXSpeed(0);
                                    if (DEBUG){
                                    System.out.println("fatal collision with bird");	
                                    }
                                    
                                }
                                break;
                            case NO_LIFT://if the rotor is spinning and
                            case HOVER:  //a bird collides with it, 
                            case LIFT:   //you're gonna have a bad time
                                gameState = GameStates.EXPLODING;
                                gameOverMessage = new GameOverMessage("Fatal Collision With Bird", score, currentLevel.getMaxScore());
                                currentLevel.getBirds()[birdNum].setXSpeed(0);//slow the bird so it will remain within the explosion
                                birdExplosion = new Explosion(currentLevel.getBirds()[birdNum].getCanvasX()
                                                      ,currentLevel.getBirds()[birdNum].getY()
                                                      ,Bird.BIRD_WIDTH
                                                      ,Bird.BIRD_HEIGHT);
                                currentLevel.getBirds()[birdNum].setY(0-Bird.BIRD_HEIGHT);//move the bird off screen

                                heliExplosion = new Explosion(heli.getCanvasX(),
                                                              heli.getY(),
                                                              Helicopter.HELI_WIDTH + 20,
                                                              Helicopter.HELI_HEIGHT + 20);
                                currentLevel.setXSpeed(0);
                                if (DEBUG){
                                    System.out.println("fatal collision with bird");	
                                }
                                break;
                        }

                    }
                }
            }
        }
        
        private void checkRingCollision(){
            //--------------------------ring Collision----------------------
            //check to see if it hits a ring
            for (int ringNum = 0; ringNum < currentLevel.getRingList().size(); ringNum++){
                if (currentLevel.getRingList().get(ringNum).getX() >= (currentLevel.getCurrentX() - Ring.RING_WIDTH) &&
                    currentLevel.getRingList().get(ringNum).getX() <= (currentLevel.getCurrentX() + GAME_WIDTH)){//only need to check visible rings
                    if (currentLevel.getRingList().get(ringNum).intersects((Rectangle2D)heli.getCollisionShape())){
                        score += Ring.RING_VALUE;
                        currentLevel.getRingList().get(ringNum).setX(0-Ring.RING_WIDTH - 20);//move the ring outside of the play area
                        currentLevel.getRingList().get(ringNum).generateCollisionShape();//make sure its collision shape moves with it
                        if (DEBUG){
                            System.out.println("collision with ring");	
                        }
                    }
                }
            }
        }
        
        private void checkGroundCollision(){
            //------------------------ground collision-----------------------------
            //check to see if it hits the ground too fast
            if (currentLevel.getGround().intersects((Rectangle2D) heli.getCollisionShape())
                    && heli.getYSpeed() != 0){//don't need to do stuff for collision if y speed is 0,
                                              //so no need to go into this stuff if the heli isnt moving

                if (heli.getXSpeed() > Helicopter.MAX_X_LANDING_SPEED || //going to fast horizontally to safely land traveling to the right
                    heli.getXSpeed() < (0-Helicopter.MAX_X_LANDING_SPEED)){//going to fast to safely land in - x direction (to the left)
                        
                    //gameState = gameStates.GAMEOVER;
                    gameOverMessage = new GameOverMessage("Hit Ground Too Fast", score, currentLevel.getMaxScore());
                    gameState = GameStates.EXPLODING;
                    heliExplosion = new Explosion(heli.getCanvasX(),
                                                  heli.getY(),
                                                  Helicopter.HELI_WIDTH + 20,
                                                  Helicopter.HELI_HEIGHT + 20);
                    currentLevel.setXSpeed(0);
                    if (DEBUG){
                            System.out.println("intersected ground too fast");
                    }
                }
                else{
                    if (heli.getYSpeed() < (Helicopter.MAX_Y_LANDING_SPEED)){// it is < because Yspeed will be negative
                        //gameState = gameStates.GAMEOVER;
                        gameOverMessage = new GameOverMessage("Hit Ground Too Fast", score, currentLevel.getMaxScore());
                        gameState = GameStates.EXPLODING;
                        heliExplosion = new Explosion(heli.getCanvasX(),
                                                      heli.getY(),
                                                      Helicopter.HELI_WIDTH + 20,
                                                      Helicopter.HELI_HEIGHT + 20);
                        currentLevel.setXSpeed(0);
                        if (DEBUG){
                                System.out.println("intersected ground too fast");
                        }
                    }
                    else{
                        heli.setYSpeed(0);
                        heli.setXSpeed(0);
                        heli.setTheta(0);
                        heli.setY(heli.getY() - 1);
                        currentLevel.setXSpeed(0);
                        heli.setLanded(true);
                        if (DEBUG){
                                System.out.println("normal landing");
                        }
                    }
                }
            }

            //check to see if the heli is starting to move into the ground and correct that
            if (currentLevel.getGround().containsPoint(heli.getX(),
                                                       heli.getY() + (Helicopter.HELI_HEIGHT - 1))){//heli is moving into ground in -x direction
                if (heli.getXSpeed() > Helicopter.MAX_X_LANDING_SPEED ||
                    heli.getYSpeed() < Helicopter.MAX_Y_LANDING_SPEED){
                    //if it is going too fast, so explode it
                    gameState = GameStates.EXPLODING;
                    gameOverMessage = new GameOverMessage("Hit Ground Too Fast", score, currentLevel.getMaxScore());
                    heliExplosion = new Explosion(heli.getCanvasX(),
                                                  heli.getY(),
                                                  Helicopter.HELI_WIDTH + 20,
                                                  Helicopter.HELI_HEIGHT + 20);
                    currentLevel.setXSpeed(0);
                }
                else{
                    //stop it moving
                    heli.setYSpeed(0);
                    heli.setXSpeed(0);
                    heli.setTheta(0);//reset the angle it is at
                    heli.setLanded(true);
                    heli.setY(heli.getY() - 1);

                    currentLevel.setXSpeed(0);
                    if (DEBUG){
                        System.out.println("Correction applied");
                    }

                    //heli.setX(heli.getX() + 3);
                    //switch (heli.getCanvasStatus()){
                    //	case STATIONARY:
                    //		//move currectX in level to sync the part of the level which is drawm
                    //		currentLevel.setCurrentX(currentLevel.getCurrentX() + 3);
                    //		break;
                    //	case MOVING_IN_LEFT:
                    //	case MOVING_IN_RIGHT:
                    //		//dont need to apply this correction if the heli's x is actually changing
                    //		break;
                    //}
                }
            }
            else if (currentLevel.getGround().containsPoint(heli.getX() + Helicopter.HELI_WIDTH,
                                                            heli.getY() + (Helicopter.HELI_HEIGHT - 1))){//heli is moving into ground in +x direction
                if (heli.getXSpeed() > Helicopter.MAX_X_LANDING_SPEED ||
                     heli.getYSpeed() < Helicopter.MAX_Y_LANDING_SPEED){
                    gameState = GameStates.EXPLODING;
                    gameOverMessage = new GameOverMessage("Hit Ground Too Fast", score, currentLevel.getMaxScore());
                    heliExplosion = new Explosion(heli.getCanvasX(),
                                                  heli.getY(),
                                                  Helicopter.HELI_WIDTH,
                                                  Helicopter.HELI_HEIGHT);
                    currentLevel.setXSpeed(0);
                }
                else{
                    heli.setYSpeed(0);
                    heli.setXSpeed(0);
                    heli.setY(heli.getY() - 1);
                    heli.setLanded(true);
                    heli.setTheta(0);

                    currentLevel.setXSpeed(0);

                    if (DEBUG){
                        System.out.println("Correction applied");
                    }

                    //heli.setX(heli.getX() - 3);
                    //switch (heli.getCanvasStatus()){
                    //case STATIONARY:
                    //	currentLevel.setCurrentX(currentLevel.getCurrentX() - 3);
                    //	break;
                    //case MOVING_IN_LEFT:
                    //case MOVING_IN_RIGHT:
                    //	break;
                    //}
                }
            }
        }
        
	//refresh the game. called in the repaint method of gamepanel
	public void drawGame(Graphics g){
            switch (gameState){
                case PLAYING:
                case PAUSED:
                    currentLevel.drawVisibleLevel(g);
                    heli.drawSprite(g);
                    break;
                case EXPLODING:
                    currentLevel.drawVisibleLevel(g);
                    heli.drawSprite(g);
                    if (birdExplosion != null){
                        birdExplosion.drawSprite(g);
                    }
                    else if (DEBUG){
                        System.out.println("birdExplosion = null");
                    }
                    if (heliExplosion != null){
                        heliExplosion.drawSprite(g);
                    }
                   else if (DEBUG){
                        System.out.println("heliExplosion = null");
                    }
                    break;
                case GAMEOVER:
                    currentLevel.drawVisibleLevel(g);
                    gameOverMessage.draw(g);
                    /*
                    if (win){
                        heli.drawSprite(g);
                        gameOverMessage.draw(g);
                    }
                    else{
                        gameOverMessage.draw(g);
                    }*/
                    break;
                case WIN:
                    currentLevel.drawVisibleLevel(g);
                    heli.drawSprite(g);
                    gameOverMessage.draw(g);
                    break;
            }
	}
	
	//update state of game objects on key press
	public void processKeyPress(){
            
            //Process if up was pressed.
            if(bUpPressed == true){
                processThrottleUp();
            }
            
            //Process if down was pressed.
            if(bDownPressed == true){
                processThrottleDown();
            }
            
            //Proccess if left was pressed.
            if(bLeftPressed == true && gameState ==GameStates.PLAYING){
                switch (heli.getThrottleStatus()){
                    case IDLE://do nothing
                        break;
                    case NO_LIFT:
                        if (!heli.isLanded()){
                            processLeftPressed();
                        }
                        break;
                    case HOVER:
                    case LIFT:
                        processLeftPressed();
                        break;
                }
            }
            
            //Process if right was pressed.
            if(bRightPressed == true && gameState == GameStates.PLAYING){
                switch (heli.getThrottleStatus()){
                    case IDLE://do nothing
                        break;
                    case NO_LIFT:
                        if (!heli.isLanded()){
                            processRightPressed();
                        }
                        break;
                    case HOVER:
                    case LIFT:
                        processRightPressed();
                        break;
                }
            }
            
            //Process if help was pressed.
            if(bHelpPressed == true){
                processHelpPressed();
            }
            
            //Process if pause was pressed.
            if(bPausePressed == true){
                processPausePressed();
            }
	}
	
        private void processThrottleUp(){
            if (gameState == GameStates.PLAYING){
                switch (heli.getThrottleStatus()){
                    case IDLE:
                        heli.setThrottleStatus(ThrottleStates.NO_LIFT);

                        if (heli.getMovementStatus() == MovementStates.STATIC_LEFT){
                            heli.setMovementStatus(MovementStates.MOVING_LEFT);
                        }
                        else if (heli.getMovementStatus() == MovementStates.STATIC_RIGHT){
                            heli.setMovementStatus(MovementStates.MOVING_RIGHT);
                        }

                        if (heli.getYSpeed() < 0){//heli is falling
                            heli.setYSpeed(Helicopter.MAX_Y_LANDING_SPEED);
                            heli.setFallYTop(20000);
                        }
                        break;
                    case NO_LIFT:
                        heli.setThrottleStatus(ThrottleStates.HOVER);
                        heli.setYSpeed(0);
                        break;
                    case HOVER:
                        heli.setThrottleStatus(ThrottleStates.LIFT);
                        heli.setYSpeed(1);
                        heli.setLanded(false);
                        break;
                    case LIFT:
                        if (heli.getYSpeed() < Helicopter.MAX_Y_SPEED){
                                heli.setYSpeed(heli.getYSpeed() + 1);
                        }
                        break;
                }
            }    
        }
        
        private void processThrottleDown(){
            if (gameState == GameStates.PLAYING){
                switch (heli.getThrottleStatus()){
                    case IDLE:
                        //do nothing if its already idle
                        break;
                    case NO_LIFT:
                        heli.setThrottleStatus(ThrottleStates.IDLE);

                        if (heli.getMovementStatus() == MovementStates.MOVING_LEFT){
                                heli.setMovementStatus(MovementStates.STATIC_LEFT);
                        }
                        else if (heli.getMovementStatus() == MovementStates.MOVING_RIGHT){
                                heli.setMovementStatus(MovementStates.STATIC_RIGHT);
                        }

                        if (heli.isLanded()){
                                //dont do anything speed releated if it is landed
                        }
                        else{//not on ground
                                if (heli.willCrossBottomOfCanvas()){
                                        heli.setYSpeed(0);
                                }
                                else{
                                        heli.setYSpeed(-2);
                                        heli.setFallYTop(heli.getY());//get the height for which it starts to autogyro
                                }
                        }
                        break;
                    case HOVER:
                        heli.setThrottleStatus(ThrottleStates.NO_LIFT);

                        if (heli.willCrossBottomOfCanvas()){
                            heli.setYSpeed(0);
                        }
                        else{
                            heli.setYSpeed(-1);
                        }
                        break;
                    case LIFT:
                        heli.setThrottleStatus(ThrottleStates.HOVER);
                        heli.setYSpeed(0);
                        break;
                }
            }
        }
        
	private void processLeftPressed(){
            if(heli.isLanded()){
                    heli.setLanded(false);
            }

            if (heli.getXSpeed() == 0 &&
                            heli.getMovementStatus() == MovementStates.MOVING_RIGHT){//need to turn it from right to left
                    heli.setMovementStatus(MovementStates.TURNING_R2L);
                    if (DEBUG){
                            System.out.println("switching directions right to left");
                    }
            }
            else if (heli.getMovementStatus() == MovementStates.TURNING_L2R ||
                     heli.getMovementStatus() == MovementStates.TURNING_R2L){
                    //dont do anything if it is turning
            }
            else{
                switch (canvasStatus){
                    case STATIONARY:
                        if (heli.getXSpeed() > 0){//it is moving to the right
                            heli.setXSpeed(heli.getXSpeed() - 1);//reduce its speed by 1
                            currentLevel.setXSpeed(heli.getXSpeed());//make level have same speed
                            heli.setTheta(heli.getTheta() - Helicopter.THETA_STEP_SIZE);
                        }
                        else{//the speed is negative or 0					
                            if (0-heli.getXSpeed() < Helicopter.MAX_X_SPEED){//if it is going less than max speed in negative direction
                                heli.setXSpeed(heli.getXSpeed() - 1);
                                currentLevel.setXSpeed(heli.getXSpeed());
                                if (heli.getXSpeed() == 0){
                                        heli.setTheta(0);
                                }
                                else {
                                        heli.setTheta(heli.getTheta() - Helicopter.THETA_STEP_SIZE);
                                }
                            }
                            else{
                                //do nothing if its already at max speed
                            }
                        }
                        break;
                    case MOVING_IN_LEFT://it is moving in the left or right part of the screen
                    case MOVING_IN_RIGHT:
                        if (heli.getXSpeed() > 0){//it is moving to the left
                            heli.setXSpeed(heli.getXSpeed() - 1);//reduce its speed by 1
                            heli.setTheta(heli.getTheta() - Helicopter.THETA_STEP_SIZE);
                        }
                        else{//the speed is negative or 0
                            if (0-heli.getXSpeed() < Helicopter.MAX_X_SPEED){//if it is going less than max speed in negative direction
                                heli.setXSpeed(heli.getXSpeed() - 1);
                                if (heli.getXSpeed() == 0){
                                    heli.setTheta(0);
                                }
                                else{
                                    heli.setTheta(heli.getTheta() - Helicopter.THETA_STEP_SIZE);
                                }
                            }
                            else{
                                //do nothing if its already at max speed
                            }
                        }
                    break;
                }
            }
	}
	
	private void processRightPressed(){
            if(heli.isLanded()){
                    heli.setLanded(false);
            }
            if (heli.getXSpeed() == 0 &&
                heli.getMovementStatus() == MovementStates.MOVING_LEFT){//need to turn it from left to right
                
                heli.setMovementStatus(MovementStates.TURNING_L2R);
                if (DEBUG){
                    System.out.println("switching directions left to right");
                }	
            }
            else if (heli.getMovementStatus() == MovementStates.TURNING_L2R ||
                     heli.getMovementStatus() == MovementStates.TURNING_R2L){
                //dont do anything if it is currently turning
            }
            else{
                switch (canvasStatus){
                    case STATIONARY:
                        if (heli.getXSpeed() < 0){//it is moving to the left
                            heli.setXSpeed(heli.getXSpeed() + 1);//reduce its speed by 1
                            currentLevel.setXSpeed(heli.getXSpeed());//make level have same speed
                            heli.setTheta(heli.getTheta() + Helicopter.THETA_STEP_SIZE);
                        }
                        else{//the speed is positive or 0
                            if (heli.getXSpeed() < Helicopter.MAX_X_SPEED){//if it is going less than max speed in negative direction
                                heli.setXSpeed(heli.getXSpeed() + 1);
                                currentLevel.setXSpeed(heli.getXSpeed());
                                if (heli.getXSpeed() == 0){
                                    heli.setTheta(0);
                                }
                                else{
                                    heli.setTheta(heli.getTheta() + Helicopter.THETA_STEP_SIZE);
                                }
                            }
                            else{
                                //do nothing if its already at max speed
                            }
                        }
                        break;
                    case MOVING_IN_LEFT://it is moving in the left or right part of the screen
                    case MOVING_IN_RIGHT:
                        if (heli.getXSpeed() < 0){//it is moving to the left
                            heli.setXSpeed(heli.getXSpeed() + 1);//reduce its speed by 1
                            heli.setTheta(heli.getTheta() + Helicopter.THETA_STEP_SIZE);
                        }
                        else{//the speed is positive  or 0
                            if (heli.getXSpeed() < Helicopter.MAX_X_SPEED){//if it is going less than max speed in negative direction
                                heli.setXSpeed(heli.getXSpeed() + 1);
                                if (heli.getXSpeed() == 0){
                                    heli.setTheta(0);
                                }
                                else{
                                    heli.setTheta(heli.getTheta() + Helicopter.THETA_STEP_SIZE);
                                }
                            }
                            else{
                                //do nothing if its already at max speed
                            }
                        }
                        break;
                }
            }
	}
        
        private void processHelpPressed(){
            switch (gameState){
                case PLAYING:
                    gameState = GameStates.PAUSED;
                    if (helpMenu.isVisible()){
                        helpMenu.setVisible(false);
                    }
                    else{
                        helpMenu.setVisible(true);
                    }
                    break;
                case PAUSED:
                case GAMEOVER:
                case WIN:
                    if (helpMenu.isVisible()){
                        helpMenu.setVisible(false);
                    }
                    else{
                        helpMenu.setVisible(true);
                    }
                    break;
            }
        }
        
        private void processPausePressed(){
            switch (gameState){
                case PLAYING:
                    gameState = GameStates.PAUSED;
                    break;
                case PAUSED:
                    gameState = GameStates.PLAYING;
                    break;
                case GAMEOVER:
                case WIN:    
                    start();
                    break;
            }
        }
        
	
	//--------------------------Panels which make up the game's UI---------------------------
	//-----setup as inner classes to make accessing instances of game objects easier----------
	
	//the panel for which the game itself will be drawn on 
	private class GamePanel extends JPanel implements KeyListener {

		//constructor
		public GamePanel(){
                    setFocusable(true); //add ability to get key events
                    requestFocus();
                    addKeyListener(this);
		}
		@Override
		public void paintComponent(Graphics g){//called when frame refreshes with repaint();
                    super.paintComponent(g); //put component onto panel
                    setBackground(Color.BLUE); 
                    drawGame(g);
		}
		
		@Override
		public void keyPressed(KeyEvent e) {//process key press
                    
                    switch(e.getKeyCode()){//set keypress booleans
                            case KeyEvent.VK_UP:
                            case KeyEvent.VK_W:
                                bUpPressed = true;
                                break;

                            case KeyEvent.VK_DOWN:
                            case KeyEvent.VK_S:
                                bDownPressed = true;
                                break;

                            case KeyEvent.VK_LEFT:
                            case KeyEvent.VK_A:
                                bLeftPressed = true;
                                break;

                            case KeyEvent.VK_RIGHT:
                            case KeyEvent.VK_D:
                                bRightPressed = true;
                                break;

                            case KeyEvent.VK_H:
                                bHelpPressed = true;
                                break;

                            case KeyEvent.VK_P:
                                bPausePressed = true;
                                break;
                        }
			processKeyPress();//process the keypress booleans

		}

		@Override
		public void keyReleased(KeyEvent e) {//set released keypress booleans
                        switch(e.getKeyCode()){
                            case KeyEvent.VK_UP:
                            case KeyEvent.VK_W:
                                bUpPressed = false;
                                break;

                            case KeyEvent.VK_DOWN:
                            case KeyEvent.VK_S:
                                bDownPressed = false;
                                break;

                            case KeyEvent.VK_LEFT:
                            case KeyEvent.VK_A:
                                bLeftPressed = false;
                                break;

                            case KeyEvent.VK_RIGHT:
                            case KeyEvent.VK_D:
                                bRightPressed = false;
                                break;

                            case KeyEvent.VK_H:
                                bHelpPressed = false;
                                break;

                            case KeyEvent.VK_P:
                                bPausePressed = false;
                                break;
                        }
                        //processKeyRelease();
                    

		}
                
                

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub
                
		}

	}
	
	//the panel with instruments indicating the heli's stats
	private class InstrumentPanel extends JPanel {
		private JLabel vertSpeedLbl;
		private JLabel horzSpeedLbl;
		private JLabel throttleLbl;
		private JLabel heliPosDebug;
		private JLabel levelDebug;
		private JLabel scoreLbl;
		
		InstrumentPanel(){
			vertSpeedLbl = new JLabel("");
			horzSpeedLbl = new JLabel("");
			throttleLbl = new JLabel("");
			heliPosDebug = new JLabel("");
			levelDebug = new JLabel("");
			scoreLbl = new JLabel("");
			
			this.setLayout(new GridLayout(2,6));
			this.add(vertSpeedLbl);//placeholder for vert. speed graphic
			this.add(horzSpeedLbl);//placeholder for horiz. speed graphic
			this.add(throttleLbl);//placeholder for throttle graphic
                        if (DEBUG){
                            this.add(heliPosDebug);                    
                            this.add(levelDebug);                       
                        }
			this.add(scoreLbl);
                        
			this.add(new JLabel("Vertical Speed"));
			this.add(new JLabel("Horizontal Speed"));
			this.add(new JLabel("Throttle"));
                        if (DEBUG){
                            this.add(new JLabel("Heli(x,y)"));          
                            this.add(new JLabel("(currentX,Xspeed)"));  
                        }
			this.add(new JLabel("Score"));
			
		}
		public void updateInstrumentPanel(){
			vertSpeedLbl.setText("" + heli.getYSpeed());
			horzSpeedLbl.setText("" + heli.getXSpeed());
			switch (heli.getThrottleStatus()){
				case IDLE:
					throttleLbl.setText("IDLE");
					break;
				case NO_LIFT:
					throttleLbl.setText("NO_LIFT");
					break;
				case HOVER:
					throttleLbl.setText("HOVER");
					break;
				case LIFT:
					throttleLbl.setText("LIFT");
					break;
			}
			
			scoreLbl.setText(""+score);
			
			//-------for debug------------
                        if (DEBUG){
                            heliPosDebug.setText("("+heli.getX()+","+heli.getY()+")");                    
                            levelDebug.setText("("+currentLevel.getCurrentX()+","+currentLevel.getXSpeed()+")");                      
                        }
		}
		
	}
	
	//panel which gives the options to pause, play game, startover, bring up help menu
	private class OptionsPanel extends JPanel{

	}
        
        //--------------------------------------help menu------------------------------------
        private class HelpMenu extends JFrame{
            HelpMenu(){
                JLabel titleLabel, objectiveLabel, controlsLabel, upLabel, downLabel, 
                        rightLabel,leftLabel, pauseLabel, helpLabel; 
                JButton playButton;
                        
                Container contentPane = this.getContentPane();
                //set up UI components
                contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
                titleLabel = new JLabel("Help Menu");
                titleLabel.setFont(new Font("Serif", Font.BOLD, 42));
                titleLabel.setAlignmentX(this.CENTER_ALIGNMENT);
                
                objectiveLabel = new JLabel("Fly through as many rings as possible while avoiding birds");
                objectiveLabel.setFont(new Font("Serif", Font.PLAIN, 26));
                objectiveLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                controlsLabel = new JLabel("Controls:");
                controlsLabel.setFont(new Font("Serif", Font.BOLD, 36));
                controlsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                upLabel = new JLabel("Press the up arrow or 'w' to increase throttle");
                upLabel.setFont(new Font("Serif", Font.PLAIN, 26));
                upLabel.setAlignmentX(this.CENTER_ALIGNMENT);
                
                downLabel = new JLabel("Press the down arrow or 's' to decrease throttle");
                downLabel.setFont(new Font("Serif", Font.PLAIN, 26));
                downLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                
                leftLabel = new JLabel("Press the left arrow or 'a' to move to the left");
                leftLabel.setFont(new Font("Serif", Font.PLAIN, 26));
                leftLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                rightLabel = new JLabel("Press the right arrow or 'd' to move to the right");
                rightLabel.setFont(new Font("Serif", Font.PLAIN, 26));
                rightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                pauseLabel = new JLabel("Press 'p' to pause and resume the game");
                pauseLabel.setFont(new Font("Serif", Font.PLAIN, 26));
                pauseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                helpLabel = new JLabel("Press 'h' to bring up this screen");
                helpLabel.setFont(new Font("Serif", Font.PLAIN, 26));
                helpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                playButton = new JButton("PLAY");
                playButton.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        HeliGameMain.HelpMenu.this.setVisible(false);
                        bHelpPressed = false;//Sets the help boolean to false to stop interference with the rest of the keypresses.
                        if (gameState == GameStates.GAMEOVER || 
                            gameState == GameStates.WIN){
                            start();
                        }
                        else{
                            gameState = GameStates.PLAYING;
                        }
                    }
                });
                playButton.setAlignmentX(Component.CENTER_ALIGNMENT);               
                
                
                contentPane.add(titleLabel);
                contentPane.add(objectiveLabel);
                contentPane.add(controlsLabel);
                contentPane.add(upLabel);
                contentPane.add(downLabel);
                contentPane.add(leftLabel);
                contentPane.add(rightLabel);
                contentPane.add(pauseLabel);
                contentPane.add(helpLabel);
                contentPane.add(playButton);
                
		//this.setLocationRelativeTo(null);//center it on the screen
		this.setTitle("Helicopter Game Help Menu");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
                this.setResizable(false);
                
		this.pack();
		this.setVisible(true);
		
            }
        }

	//-----------------------------finally main insertion point for program--------------------
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				// build the UI in its own thread for thread safety
				new HeliGameMain();
			}
		});
	}

}
