package heliGame;

//import heliGame.HeliGameMain.gameStates;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
//import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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
				if(birdIndex == 0){//initialize the animation if it is the first bird only
					birds [birdIndex] = new Bird(numGenerator.nextInt(this.levelLength) ,numGenerator.nextInt(Bird.MIN_ALTITUDE), true);
				}
				else{
					birds [birdIndex] = new Bird(numGenerator.nextInt(this.levelLength) ,numGenerator.nextInt(Bird.MIN_ALTITUDE), false);
				}
				
				//check to see if the bird is within a 200 pixels horizontally or vertically if the heli
				//if it is, re-generate it
				while (birds[birdIndex].intersects(
						new Rectangle(this.startX - 200, this.startY - 200, 400 + Helicopter.HELI_WIDTH, 400 + Helicopter.HELI_HEIGHT))){
					if(birdIndex == 0){//initialize the animation if it is the first bird only
						birds [birdIndex] = new Bird(numGenerator.nextInt(this.levelLength) ,numGenerator.nextInt(Bird.MIN_ALTITUDE), true);
					}
					else{
						birds [birdIndex] = new Bird(numGenerator.nextInt(this.levelLength) ,numGenerator.nextInt(Bird.MIN_ALTITUDE), false);
					}
				}
			}
			drawWholeLevel();
			
		}
		else{
			throw new LevelNotLoadedException(
					"ERROR: Level "+levelNumber+" not succesfully loaded. Exiting.");
		}
	}
	
	//----------------------Getters and Setters-------------------------------
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
			FileInputStream inStream = new FileInputStream(LEVEL_DIR+"level"+levelNumber+".level");
			parseLevelFile(inStream);//only gets executed if file opens okay 
			inStream.close();
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
	
	private void parseLevelFile(FileInputStream inStream){
		String lineIn = "";
		
		BufferedReader fileIn = new BufferedReader(new InputStreamReader(inStream));
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
		
		//put ground onto this image
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
		//drawImage(Image, x1 destination, y1destination, x2 destination, y2 destination,
		//         x1 source, y1 source, x2 source, y2 source, image observer)
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
		g.drawImage(offScreenImage, 0, 0, null);
		
		//----------------------draw visible birds--------------
		for (int birdIndex = 0; birdIndex < Bird.MAX_BIRDS; birdIndex++){
			if ((birds[birdIndex].getX()>= (this.currentX - Bird.BIRD_WIDTH)) &&
			    (birds[birdIndex].getX()<= (this.currentX + HeliGameMain.GAME_WIDTH))){
				birds[birdIndex].setCanvasX( birds[birdIndex].getX() - this.currentX );//set the x to draw the bird on the canvas
				birds[birdIndex].drawSprite(g);
			}
			else{
				//dont draw the bird
			}
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
		//TODO: update position of level's objects
		//-----------process each bird 1 at a time--------------------
		for (int birdIndex = 0; birdIndex < Bird.MAX_BIRDS; birdIndex++){
			//-----------------check to see if bird is going into ground-----------------
			while(ground.containsPoint(birds[birdIndex].getX(), birds[birdIndex].getY() + Bird.BIRD_HEIGHT - 1)){
				birds[birdIndex].setY(birds[birdIndex].getY() - 4);
			}
			
			//-------------check to see if it drifted off the screen--------
			if (birds[birdIndex].getX() < (0 - Bird.BIRD_WIDTH)){//it drifted off the screen
				//put it back at the end of the level
				birds[birdIndex].setX(this.levelLength);
				birds[birdIndex].setY(birds[birdIndex].getInitialY());
			}
			else{//update it as normal
				birds[birdIndex].updateBird();
			}
		}
	}

	/*
	//------------------Give ability for program to access object to test for collision------
	public boolean intersectsTree(){
		return false;
		
	}
	
	public boolean intersectsGround(){
		return false;
		
	}*/
	
	//-------------Class for custom exception which a level can throw when improperly loaded-----------
	public class LevelNotLoadedException extends Exception{
		public LevelNotLoadedException(String message){
			super(message);
		}
	}
}
