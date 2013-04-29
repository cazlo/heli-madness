package heliGame;

//import heliGame.HeliGameMain.gameStates;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
//import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
//import java.awt.image.RasterFormatException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class Level {
	static final String LEVEL_DIR = "levels/";
	/*
	public static enum LevelStates{
		STATIONARY, MOVING_LEFT, MOVING_RIGHT
	}
	private LevelStates levelStatus;*/
	
	private BufferedImage wholeLevelImage;
	private BufferedImage offScreenImage;//draw the level off screen before drawing to panel(double buffering)
	private int levelNumber;
	//private String fileName;
	private int startX, startY;//location heli starts at
	private int xSpeed;//in pixels
	private int currentX;//the current location of the top corner of image in relation to its total size
	private int levelLength;//the length of the level in pixels
        private int maxScore;
	
	//-----------------game objects for the current level--------------
	private ArrayList<Tree> treeList; 
	
	private Ground ground;
	private ArrayList<Integer> groundXPoints;
	private ArrayList<Integer> groundYPoints;
	
	private Bird [] birds;
	
	private ArrayList<Ring> ringList;
        
        private FinishLine finishLine;
	
	//-------------------------constructor--------------------------
	Level(int levelNumberIn) throws LevelNotLoadedException{
		groundXPoints = new ArrayList<Integer>();
		groundYPoints = new ArrayList<Integer>();
		treeList = new ArrayList<Tree>();
		birds = new Bird[Bird.MAX_BIRDS];
		ringList = new ArrayList<Ring>();
		
		this.wholeLevelImage = null;
		this.offScreenImage = null;
		
		this.levelNumber = levelNumberIn;
		this.xSpeed = 0;//not moving initially
		//this.levelStatus = LevelStates.STATIONARY;
		if (loadedLevel()){
                    this.levelLength = groundXPoints.get(groundXPoints.size()-1);
                    ground = new Ground(groundXPoints,groundYPoints);
                    finishLine = new FinishLine(groundXPoints.get(groundXPoints.size()-1) - (HeliGameMain.GAME_WIDTH/3),
                                                groundYPoints.get(groundYPoints.size()-1) - FinishLine.HEIGHT, this.levelLength );

                    //randomly generate birds
                    Random numGenerator = new Random();
                    for (int birdIndex = 0; birdIndex < Bird.MAX_BIRDS; birdIndex++){
                        birds [birdIndex] = new Bird(numGenerator.nextInt(HeliGameMain.GAME_WIDTH) + HeliGameMain.GAME_WIDTH,//draw it off screen initially
                                                     numGenerator.nextInt(Bird.MIN_ALTITUDE));//, true);
                        
                    }
                    this.maxScore = this.ringList.size() * Ring.RING_VALUE;
                    
                    drawWholeLevel();
		}
		else{
                    throw new LevelNotLoadedException("ERROR: Level "+levelNumber+" not succesfully loaded. Exiting.");
		}
	}
	
	//----------------------Getters and Setters-------------------------------
        public int getMaxScore(){
            return maxScore;
        }
        
	public int getStartX(){
		return startX;
	}
	
	public int getStartY(){
		return startY;
	}
	
	public int getCurrentX(){
		return currentX;
	}
	
	public int getXSpeed() {
		return xSpeed;
	}
	
	public int getLevelLength(){
		return levelLength;
	}
	
	public void setXSpeed(int xSpeed) {
		this.xSpeed = xSpeed;
	}
		
	public void setCurrentX(int currentXIn) {
		this.currentX = currentXIn;
	}
	
	public ArrayList<Tree> getTreeList(){
		return this.treeList;
	}
	
	public ArrayList<Ring> getRingList(){
		return this.ringList;
	}
	
	public Ground getGround(){
		return this.ground;
	}
	
	public Bird [] getBirds(){
		return this.birds;
	}
        
        public FinishLine getFinishLine(){
            return this.finishLine;
        }
	//-------------------------------tools to parse the level files-----------------------
	
	public boolean loadedLevel(){
            try{
                URL levelURL = HeliGameMain.class.getResource(LEVEL_DIR+"level"+levelNumber+".level");
                //FileInputStream inStream = new FileInputStream(LEVEL_DIR+"level"+levelNumber+".level");
                if (levelURL == null){
                    System.out.println("ERROR: Level File \'"+LEVEL_DIR+"level"+levelNumber+".level\' not found");
                    throw new IOException();
                }
                BufferedReader fileIn = new BufferedReader(new InputStreamReader(levelURL.openStream()));
                parseLevelFile(fileIn);//only gets executed if file opens okay 
                fileIn.close();
            }
            catch(FileNotFoundException e){
                System.out.println("ERROR: Level File \'"+LEVEL_DIR+"level"+levelNumber+".level\' not found");
                return false;
            } 
            catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
	}
	
	private void parseLevelFile(BufferedReader fileIn){
		String lineIn = "";
		
		//BufferedReader fileIn = new BufferedReader(new InputStreamReader(inStream));
		try{
			do{
				lineIn = fileIn.readLine();
				if (lineIn != null){
					parseLine(lineIn);
				}
			}while (lineIn != null);
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			try{
				fileIn.close();
			}
			catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	
	private void parseLine(String lineIn){
		String subString = "";
		
		if (lineIn.startsWith("//")){//check for comment
			//it is a comment, ignore it
		}
		else if (lineIn.startsWith("heliInitial(")){//check for initial heli spawn point
			try{
				subString = lineIn.substring(lineIn.indexOf('(') + 1, lineIn.indexOf(',') );
				this.startX = Integer.parseInt(subString);
				subString = lineIn.substring(lineIn.indexOf(',') + 1, lineIn.indexOf(')') );
				this.startY = Integer.parseInt(subString);
			}
			catch(NumberFormatException e){
				System.out.println("ERROR: error processing the line: "+lineIn);
			}
		}
		else if (lineIn.startsWith("tree(")){//check for tree
			try{
				int tempX, tempY;
				subString = lineIn.substring(lineIn.indexOf('(') + 1, lineIn.indexOf(',') );
				tempX = Integer.parseInt(subString);
				subString = lineIn.substring(lineIn.indexOf(',') + 1, lineIn.indexOf(')'));
				tempY = Integer.parseInt(subString);
				treeList.add(new Tree(tempX,tempY));//only add it if the file is parsed correctly
			}
			catch(NumberFormatException e){
				System.out.println("ERROR: error processing the line: "+lineIn);
			}
		}
		else if (lineIn.startsWith("groundX(")){// check for ground X points
			try{				
				subString = lineIn.substring(lineIn.indexOf('(') + 1,lineIn.indexOf(')'));
				//substring now only contains the points seperated by commas
				while(subString != ""){
					if (subString.indexOf(',') >= 0){//there is a comma in the substring still
						//subString = subString.substring(0, subString.indexOf(',') );
						groundXPoints.add(Integer.parseInt(subString.substring(0, subString.indexOf(',') )));//process the number before a comma
						subString = subString.substring(subString.indexOf(',') + 1);//get rid of the number just processed
						}
					else{//no comma left
						groundXPoints.add(Integer.parseInt(subString));
						subString = "";
					}
				}
                                //this.levelLength = groundXPoints.get(groundXPoints.size()-1);
				
			}
			catch(NumberFormatException e){
				System.out.println("ERROR: error processing the line: "+lineIn);
			}
		}
		else if (lineIn.startsWith("groundY(")){//check for ground y points
			try{
				subString = lineIn.substring(lineIn.indexOf('(') + 1,lineIn.indexOf(')'));
				//substring now only contains the points seperaated by commas
				while(subString != ""){
					if (subString.indexOf(',') >= 0){//there is a comma in the substring still
						//subString =subString.substring(0, subString.indexOf(',') );
						groundYPoints.add(Integer.parseInt(subString.substring(0, subString.indexOf(',') )));//process the number before a comma
						subString = subString.substring(subString.indexOf(',') + 1);//get rid of the number just processed
						}
					else{//no comma left
						groundYPoints.add(Integer.parseInt(subString));
						subString = "";
					}
				}
			}
			catch(NumberFormatException e){
				System.out.println("ERROR: error processing the line: "+lineIn);
			}
		}
		else if (lineIn.startsWith("Ring(")){//check for rings
			try{
				int tempX, tempY;
				subString = lineIn.substring(lineIn.indexOf('(') + 1, lineIn.indexOf(',') );
				tempX = Integer.parseInt(subString);
				subString = lineIn.substring(lineIn.indexOf(',') + 1, lineIn.indexOf(')'));
				tempY = Integer.parseInt(subString);
				ringList.add(new Ring(tempX,tempY));//only add it if the file is parsed correctly
			}
			catch(NumberFormatException e){
				System.out.println("ERROR: error processing the line: "+lineIn);
			}
		}                
		else{
			//it is something else, so just ignore it
		}
		
	}

	/*uneeded, because assume that the last x point loaded is the highest
	private int maxX(){
		int tempMax = this.groundXPoints.get(0);
		for (int i = 0; i < this.groundXPoints.size(); i++){
			if (this.groundXPoints.get(i) > tempMax){
				tempMax = this.groundXPoints.get(i);
			}
		}
		return tempMax;
	}*/
	
	//-------------------Drawing the level--------------------------
	
	//draws an image representing the whole level(minus the heli sprite)
	private void drawWholeLevel(){
		wholeLevelImage = new BufferedImage(this.levelLength, HeliGameMain.GAME_HEIGHT, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g2 = wholeLevelImage.createGraphics();
		Graphics g = (Graphics)g2;
		//TODO: put background onto this image
		
		//put trees onto this image
		for (int i = 0; i < treeList.size(); i++){
			treeList.get(i).drawSprite(g);
		}
		
                finishLine.drawSprite(g);
		ground.drawGround(g2);
		
		/*
		//put rings on image
		for (int i = 0; i < ringList.size(); i++){
			ringList.get(i).drawSprite(g);
		}*/
		//put other stuff onto this image
	}
	
	public void drawVisibleLevel(Graphics g){
		//---------------------draw a sub image of the level image-------------------
            /*//double buffer by drawing to an offscreen image first
		try{
			this.offScreenImage = this.wholeLevelImage.getSubimage(this.currentX, 0, HeliGameMain.GAME_WIDTH, HeliGameMain.GAME_HEIGHT);
		}
		catch (RasterFormatException ex){//somehow the image got un-synced and , re-sync it
                    if (this.currentX > this.levelLength - HeliGameMain.GAME_WIDTH){
                            this.currentX = (this.levelLength - HeliGameMain.GAME_WIDTH);
                    }
                    else if (this.currentX < 0){
                            this.currentX = 0;
                    }
                    if (HeliGameMain.DEBUG){
                            System.out.println("RasterFormatException handled");
                    }
                    this.offScreenImage = this.wholeLevelImage.getSubimage(this.currentX, 0, HeliGameMain.GAME_WIDTH, HeliGameMain.GAME_HEIGHT);
		}
		g.drawImage(offScreenImage, 0, 0, null);*/
                //instead f double buffering, just draw it this way to help improve performance
                g.drawImage(this.wholeLevelImage,
                            0,//destination x1
                            0,//destination y1
                            HeliGameMain.GAME_WIDTH,//destination x2
                            HeliGameMain.GAME_HEIGHT,//destination y2
                            this.currentX,//source x1,
                            0,//source y1
                            this.currentX + HeliGameMain.GAME_WIDTH,//source x2
                            HeliGameMain.GAME_HEIGHT,//source 
                            null//no observer
                            );
		
		//----------------------draw visible birds--------------
		for (int birdIndex = 0; birdIndex < Bird.MAX_BIRDS; birdIndex++){
                    //if ((birds[birdIndex].getX()>= (this.currentX - Bird.BIRD_WIDTH)) &&
                    //    (birds[birdIndex].getX()<= (this.currentX + HeliGameMain.GAME_WIDTH))){
                    //        birds[birdIndex].setCanvasX( birds[birdIndex].getX() - this.currentX );//set the x to draw the bird on the canvas
                            birds[birdIndex].drawSprite(g);
                   //}
                    //else{
                    //        //dont draw the bird
                    //}
		}
		
		//-----------------draw visible rings------------------
		for (int ringNum = 0; ringNum < ringList.size(); ringNum++){
                    if ((ringList.get(ringNum).getX()>= (this.currentX - Ring.RING_WIDTH)) &&
                        (ringList.get(ringNum).getX()<= (this.currentX + HeliGameMain.GAME_WIDTH))){

                        ringList.get(ringNum).setCanvasX( ringList.get(ringNum).getX() - this.currentX );//set the x to draw the bird on the canvas
                        ringList.get(ringNum).drawSprite(g);
                    }
                    else{
                            //dont draw the ring
                    }
		}
		
		//g.drawImage(wholeLevelImage,0,0,HeliSimMain.GAME_WIDTH, HeliSimMain.GAME_HEIGHT,
		//		this.currentX, 0, this.currentX + HeliSimMain.GAME_WIDTH, HeliSimMain.GAME_HEIGHT, null);
	}
	
	//--------------------------Update position of level and level's objects---------------
	public void updateLevel(){
            this.currentX += xSpeed;
            //update position of level's objects
            
            //-----update animation of visible rings---------------
            for (int ringNum = 0; ringNum < ringList.size(); ringNum++){
                if ((ringList.get(ringNum).getX()>= (this.currentX - Ring.RING_WIDTH)) &&
                    (ringList.get(ringNum).getX()<= (this.currentX + HeliGameMain.GAME_WIDTH))){
                    ringList.get(ringNum).updateAnimation();
                }
            }
            
            
            //-----------process each bird 1 at a time--------------------
            for (int birdIndex = 0; birdIndex < Bird.MAX_BIRDS; birdIndex++){
                //-----------------check to see if bird is going into ground-----------------
                while(ground.containsPoint(birds[birdIndex].getX() + this.currentX,
                                           birds[birdIndex].getY() + Bird.BIRD_HEIGHT - 1)){
                    birds[birdIndex].setY(birds[birdIndex].getY() - 4);
                    
                }
                
                birds[birdIndex].updateBird(this.xSpeed);
            }
	}

	//-------------Class for custom exception which a level can throw when improperly loaded-----------
	public class LevelNotLoadedException extends Exception{
		public LevelNotLoadedException(String message){
			super(message);
		}
	}
}
