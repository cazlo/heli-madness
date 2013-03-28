package heliGame;

import heliGame.Helicopter.CanvasStates;
import heliGame.Helicopter.MovementStates;
import heliGame.Helicopter.RotorStates;
import heliGame.Level.LevelNotLoadedException;
//import heliSim.Level.LevelStates;
//
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

public class HeliGameMain extends JFrame {

	/**
	 * 
	 */
	static final boolean DEBUG = true;
	
	//define constants for the game
	static final int GAME_WIDTH = 800; //width of game window
	static final int GAME_HEIGHT = 600; // height of game window
	static final int UPDATE_RATE = 80; //number of game updates per second
	static final long UPDATE_PERIOD = 1000000000L / UPDATE_RATE;  // time (nanoseconds) that loop thread is paused between game updates
	static final int NUM_LEVELS = 1;
	static final int MIDDLE_OF_FRAME = (GAME_WIDTH / 2) - (Helicopter.HELI_WIDTH/2);//the location at which the heli sprite stops moving, and the level starts moving
		
	// enumerate states of game to make for more readable code
	static enum gameStates {
		INITIALIZED, PLAYING, PAUSED, GAMEOVER, NEXTLEVEL, EXPLODING
	}
		
	static gameStates gameState; //current state of the game
		
	//define instance variables of game objects
	private Helicopter heli;
	private Explosion heliExplosion;
	private Level currentLevel;
	private int currentLevelNumber = 1;
	private int score;
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

		this.pack();
		this.setVisible(true);
		
		start();
	}
		
	public void init() {
		// initialize game objects
		this.currentLevelNumber = 1;
		try{
			currentLevel = new Level(currentLevelNumber);
		} catch (LevelNotLoadedException e){
			System.out.println(e);
			shutdown(1);
		}
		
		this.score = 0;
		heli = new Helicopter(currentLevel.getStartX(),currentLevel.getStartY());
		
		gameState = gameStates.INITIALIZED;
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
		
		
		if (gameState == gameStates.INITIALIZED) {
			gameState = gameStates.PLAYING;
		}
		else if (gameState == gameStates.GAMEOVER){
			init();//regenerate game objects for new game
			gameState = gameStates.PLAYING;
		}
		
		//loop for game
		while (true){
			startTime = System.nanoTime();
			if (gameState == gameStates.GAMEOVER){
				//TODO: do stuff for gameover
				break; //get out of main game loop if game is over
			}
			if (gameState == gameStates.EXPLODING){
				if (heliExplosion.doneExploding()){
					gameState = gameStates.GAMEOVER;
				}
				else{
					heliExplosion.updateExplosion();
					currentLevel.updateLevel();
				}
			}
			if (gameState == gameStates.PLAYING){
				//update state of all game objects
				updateGame();
			}
			if (gameState == gameStates.NEXTLEVEL){
				if (currentLevelNumber > NUM_LEVELS){
					//TODO: completed all levels
					
				}
				else{
					currentLevelNumber ++;
					try {
						currentLevel = new Level(currentLevelNumber);
					} catch (LevelNotLoadedException e) {
						e.printStackTrace();
						shutdown(1);
					}

					heli = new Helicopter(currentLevel.getStartX(),currentLevel.getStartY());
					gameState = gameStates.PLAYING;
				}
			}
			repaint();//refresh the frame; re-calls the paintComponent methods of each panel in the main frame
			
			//find out how long to sleep to keep the game refreshing at REFRESH_RATE
			timeTaken = System.nanoTime() - startTime;
			timeLeft = (UPDATE_PERIOD - timeTaken) / 1000000L;  // in milliseconds
			if (timeLeft < 10){
				timeLeft = 10;  //set a minimum amount of time to sleep
			}
			try{
				Thread.sleep(timeLeft);//let other threads do thier stuff (like input thread)
			} catch(InterruptedException ex){}
		}
		
	}
	
	public void updateGame() {
		// TODO update state of all game objects
		
		
		
		heli.updateHeli();//update y and x of heli in total game plane
		currentLevel.updateLevel();
		updateCanvas();
		instrumentPanel.updateInstrumentPanel();

		collisionDetection();
		
	}
	
	private void updateCanvas(){
		//----------check to see if heli is right at beginning or right at end of level--------------
		//at these points, the heli and background will swap movement states; the background stops moving
		//and the heli starts moving, or vice versa
	
		if (heli.willCrossBeginningChange()){
			if (heli.getXSpeed() > 0){
				heli.setCanvasX(MIDDLE_OF_FRAME);
				currentLevel.setXSpeed(heli.getXSpeed());
				heli.setCanvasStatus(CanvasStates.STATIONARY);
			}
			else if (heli.getXSpeed() < 0){
				heli.setCanvasX(heli.getX());
				currentLevel.setXSpeed(0);
				heli.setCanvasStatus(CanvasStates.MOVING_IN_LEFT);
			}
		}
		else if (heli.willCrossEndChange(currentLevel.getLevelLength())){
			if (heli.getXSpeed() > 0){
				heli.setCanvasX(GAME_WIDTH - (currentLevel.getLevelLength() - heli.getX()));
				currentLevel.setXSpeed(0);
				heli.setCanvasStatus(CanvasStates.MOVING_IN_RIGHT);
			}
			else if (heli.getXSpeed() < 0){
				heli.setCanvasX(MIDDLE_OF_FRAME);
				currentLevel.setXSpeed(heli.getXSpeed());
				heli.setCanvasStatus(CanvasStates.STATIONARY);
			}
		}
		else{//wont cross anywhere
			switch(heli.getCanvasStatus()){
				case STATIONARY:
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
		switch (heli.getCanvasStatus()){
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
		//TODO check to see if it hits a ring
		
		//------------------------------tree collision--------------------------
		//check to see if it hits a tree
		for (int treeNum = 0; treeNum < currentLevel.getTreeList().size(); treeNum++){
			if(currentLevel.getTreeList().get(treeNum).intersects((Rectangle2D)heli.getCollisionShape())){
				//TODO:process heli hitting tree
				//gameState = gameStates.GAMEOVER;
				gameState = gameStates.EXPLODING;
				heliExplosion = new Explosion(heli.getCanvasX(), heli.getY(), Helicopter.HELI_WIDTH, Helicopter.HELI_HEIGHT);
				currentLevel.setXSpeed(0);
				if (DEBUG){
					System.out.println("Collision with tree");
				}
			}
		}
		
		//---------------------------bird Collision------------------
		//check to see if it hits a bird
		for (int birdNum = 0; birdNum < Bird.MAX_BIRDS; birdNum++){
			if (currentLevel.getBirds()[birdNum].intersects((Rectangle2D)heli.getCollisionShape())){
				//gameState = gameStates.GAMEOVER;
				gameState = gameStates.EXPLODING;
				heliExplosion = new Explosion(heli.getCanvasX(), heli.getY(), Helicopter.HELI_WIDTH, Helicopter.HELI_HEIGHT);
				currentLevel.setXSpeed(0);
				if (DEBUG){
					System.out.println("collision with bird");	
				}
			}
		}
		
		//--------------------------ring Collision----------------------
		//check to see if it hits a ring
		for (int ringNum = 0; ringNum < currentLevel.getRingList().size(); ringNum++){
			if (currentLevel.getRingList().get(ringNum).intersects((Rectangle2D)heli.getCollisionShape())){
				score += Ring.RING_VALUE;
				currentLevel.getRingList().get(ringNum).setX(0-Ring.RING_WIDTH - 20);//move the ring outside of the play area
				currentLevel.getRingList().get(ringNum).generateCollisionShape();//make sure its collision shape moves with it
				if (DEBUG){
					System.out.println("collision with ring");	
				}
			}
		}
		
		//------------------------ground collision-----------------------------
		//check to see if it hits the ground too fast
		if (currentLevel.getGround().intersects((Rectangle2D) heli.getCollisionShape())
			&& heli.getYSpeed() != 0){//don't need to do stuff for collision if y speed is 0,
									  //so no need to go into this loop if the heli isnt moving
			
			if (heli.getXSpeed() > Helicopter.MAX_X_LANDING_SPEED || //going to fast horizontally to safely land traveling to the right
				heli.getXSpeed() < (0-Helicopter.MAX_X_LANDING_SPEED)){//going to fast to safely land in - x direction (to the left)
				//TODO: process heli blowing up
				//gameState = gameStates.GAMEOVER;
				gameState = gameStates.EXPLODING;
				heliExplosion = new Explosion(heli.getCanvasX(), heli.getY(), Helicopter.HELI_WIDTH, Helicopter.HELI_HEIGHT);
				currentLevel.setXSpeed(0);
				if (DEBUG){
					System.out.println("intersected ground too fast");
				}
				
			}
			else{
				if (heli.getYSpeed() < (Helicopter.MAX_Y_LANDING_SPEED)){
					//TODO: process heli blowing up
					//gameState = gameStates.GAMEOVER;
					gameState = gameStates.EXPLODING;
					heliExplosion = new Explosion(heli.getCanvasX(), heli.getY(), Helicopter.HELI_WIDTH, Helicopter.HELI_HEIGHT);
					currentLevel.setXSpeed(0);
					if (DEBUG){
						System.out.println("intersected ground too fast");
					}
				}
				else{
					//TODO: process normal landing
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
		if (currentLevel.getGround().containsPoint(heli.getX(), heli.getY() + (Helicopter.HELI_HEIGHT - 1))){//heli is moving into ground in -x direction
			if (heli.getXSpeed() > Helicopter.MAX_X_LANDING_SPEED ||
				heli.getYSpeed() > Helicopter.MAX_Y_LANDING_SPEED){
				gameState = gameStates.EXPLODING;
				heliExplosion = new Explosion(heli.getCanvasX(), heli.getY(), Helicopter.HELI_WIDTH, Helicopter.HELI_HEIGHT);
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
		else if (currentLevel.getGround().containsPoint(heli.getX() + Helicopter.HELI_WIDTH, heli.getY() + (Helicopter.HELI_HEIGHT - 1))){//heli is moving into ground in +x direction
			if (heli.getXSpeed() > Helicopter.MAX_X_LANDING_SPEED ||
					heli.getYSpeed() > Helicopter.MAX_Y_LANDING_SPEED){
					gameState = gameStates.EXPLODING;
					heliExplosion = new Explosion(heli.getCanvasX(), heli.getY(), Helicopter.HELI_WIDTH, Helicopter.HELI_HEIGHT);
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
				heli.drawSprite(g);
				currentLevel.drawVisibleLevel(g);
				break;
			case EXPLODING:
				heli.drawSprite(g);
				currentLevel.drawVisibleLevel(g);
				heliExplosion.drawSprite(g);
				break;
			case GAMEOVER:
				//TODO: Draw stuff for gameover
				//heli.drawSprite(g);
				currentLevel.drawVisibleLevel(g);
				break;
		}
	}
	
	//update state of game objects on key press
	public void processKeyPress(int keyCode){
		switch(keyCode){
			case KeyEvent.VK_UP: 
			case KeyEvent.VK_W:	// process w pressed; throttle up
				//w is equivalent to throttle up
				if (gameState == gameStates.PLAYING){
					switch (heli.getRotorStatus()){
						case IDLE:
							heli.setRotorStatus(RotorStates.NO_LIFT);
							
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
							heli.setRotorStatus(RotorStates.HOVER);
							heli.setYSpeed(0);
							break;
						case HOVER:
							heli.setRotorStatus(RotorStates.LIFT);
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
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_S://process s pressed; throttle down
				if (gameState == gameStates.PLAYING){
					switch (heli.getRotorStatus()){
						case IDLE:
							break;
						case NO_LIFT:
							heli.setRotorStatus(RotorStates.IDLE);
							
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
							heli.setRotorStatus(RotorStates.NO_LIFT);
							
							if (heli.willCrossBottomOfCanvas()){
								heli.setYSpeed(0);
							}
							else{
								heli.setYSpeed(-1);
							}
							break;
						case LIFT:
							heli.setRotorStatus(RotorStates.HOVER);
							heli.setYSpeed(0);
							break;
					}
				}
				break;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_A://process 'a' pressed
				if (gameState == gameStates.PLAYING){
					switch (heli.getRotorStatus()){
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
				break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_D://process 'd' pressed
				if (gameState == gameStates.PLAYING){
					switch (heli.getRotorStatus()){
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
				break;
			case KeyEvent.VK_H:
				//TODO process H pressed
				break;
			case KeyEvent.VK_P:
				//TODO process p pressed
				switch (gameState){
					case PLAYING:
						gameState = gameStates.PAUSED;
						break;
					case PAUSED:
						gameState = gameStates.PLAYING;
						break;
					case GAMEOVER:
						start();
						break;
				}
				break;
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
			switch (heli.getCanvasStatus()){
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
			//dont do anything if it is turning
		}
		else{
			switch (heli.getCanvasStatus()){
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
	/*
	private static boolean heliWillMoveCanvasX(){
		if (heli.getX() < MIDDLE_OF_FRAME ){
			return true;
		}
		else if (heli.getX() > (currentLevel.getLevelLength() - MIDDLE_OF_FRAME)){
			//if (heli.getXSpeed() > 0){
				return true;
			//}
			//else{
				//return false;
			//}
		}/*
		else if (heli.getX() == ((GAME_WIDTH / 2) - (heli.HELI_WIDTH/2)) ||
				heli.getX() == (currentLevel.getLevelLength() - (GAME_WIDTH / 2)) ){
			return false;
		}*
		else{
			return false;
		}
	}*/
	
	
	
	
	//--------------------------Panels which make up the game's UI---------------------------
	//-----setup as inner classes to make accessing instances of game objects easier----------
	
	//the panel for which the game itself will be drawn on 
	public class GamePanel extends JPanel implements KeyListener {

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
			processKeyPress(e.getKeyCode());

		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

	}
	
	//the panel with instruments indicating the heli's stats
	public class InstrumentPanel extends JPanel {
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
			this.add(heliPosDebug);                     //TODO: remove (debug only)
			this.add(levelDebug);                       //TODO: remove (debug only)
			this.add(scoreLbl);
			this.add(new JLabel("Vertical Speed"));
			this.add(new JLabel("Horizontal Speed"));
			this.add(new JLabel("Throttle"));
			this.add(new JLabel("Heli(x,y)"));          //TODO: remove (debug only)
			this.add(new JLabel("(currentX,Xspeed)"));  //TODO: remove (debug only)
			this.add(new JLabel("Score"));
			
		}
		public void updateInstrumentPanel(){
			vertSpeedLbl.setText("" + heli.getYSpeed());
			horzSpeedLbl.setText("" + heli.getXSpeed());
			switch (heli.getRotorStatus()){
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
			heliPosDebug.setText("("+heli.getX()+","+heli.getY()+")");                    //TODO: remove (debug only)
			levelDebug.setText("("+currentLevel.getCurrentX()+","+currentLevel.getXSpeed()+")");                      //TODO: remove (debug only)
		}
		
	}
	
	//panel which gives the options to pause, play game, startover, bring up help menu
	public class OptionsPanel extends JPanel{

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
