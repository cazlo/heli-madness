package heliGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

//import javax.swing.ImageIcon;

public class Bird extends Sprite {
	//---------constants-----------
    	static final int MIN_ALTITUDE = 450;//the lowest they will go
	static final int BIRD_WIDTH = 20;
	static final int BIRD_HEIGHT = 20;
	static final int MAX_SPEED = 4;
        //static final int MAX_BIRDS_NORMAL = 35;
        //static final int MAX_BIRDS_DEBUG = 15
	static final int MAX_BIRDS = 15;//maximum number of birds in the game world
        
        //---------variables------------
	//private int canvasX;
	private int initialY, initialX;
	private static int numBirds = 0;//current number of birds in play
        
	//----------------------------stuff for animation-------------------------------
	private static final int [] birdAnimImageNumbers = {1,2,3};
	private static final String birdAnimLocation = "images/bird/birdFrame";
	private static int birdAnimDelay = 5;//MAX_BIRDS * 10;
        private AnimationUtils birdAnim;
	static BufferedImage[] animFrames = new BufferedImage[birdAnimImageNumbers.length];
        
	//--------------constructor--------------
	Bird(int xIn,int yIn){//, boolean initAnimation){
            super(xIn,yIn);
            if (numBirds == 0){
                for (int i = 0; i <birdAnimImageNumbers.length; i++){
                    animFrames[i] = AnimationUtils.staticLoadImage(birdAnimLocation + birdAnimImageNumbers[i] + ".png");
                }
            }
            else if (numBirds >= MAX_BIRDS){
                numBirds = 0;
            }
            Random rng = new Random();//for random speed and frame of animation
           /*
            if(HeliGameMain.DEBUG){
                    Bird.MAX_BIRDS = MAX_BIRDS_DEBUG;
                    //birdAnimDelay = MAX_BIRDS * 10;
            }
            else{
                    Bird.MAX_BIRDS = MAX_BIRDS_NORMAL;
                    //birdAnimDelay = MAX_BIRDS * 10;
            }*/

            //if (initAnimation){
            birdAnim = new AnimationUtils(birdAnimImageNumbers.length,
                                          birdAnimDelay,
                                          AnimationUtils.LoopTypes.LOOP_FOREVER);
                                          //animFrames);
            //}
            birdAnim.setCurrentFrame(birdAnimImageNumbers[rng.nextInt(birdAnimImageNumbers.length - 1)]);
            //this.setXSpeed(-1);
            this.setXSpeed(0-(rng.nextInt(MAX_SPEED) + 1));//random speed birds in 1 direction
            this.setInitialY(yIn);
            this.setInitialX(xIn);
            this.generateCollisionShape();
            numBirds++;
	}
	/*
	//-----------------getters and setters--------------
	public int getCanvasX(){
		return canvasX;
	}
	public void setCanvasX(int canvasXIn){
		canvasX = canvasXIn;
	}*/
	
	public int getInitialY() {
		return initialY;
	}

	public void setInitialY(int initialYIn) {
		this.initialY = initialYIn;
	}
        public int getInitialX() {
		return initialX;
	}

	public void setInitialX(int initialXIn) {
		this.initialX = initialXIn;
	}

	//-------------------------collision detection stuff----------------
	@Override
	public boolean intersects(Rectangle2D rectangleIn) {
		if (rectangleIn.intersects((Rectangle2D)this.getCollisionShape())){
			return true;
		}
		else{
			return false;
		}
		
	}

	@Override
	public void generateCollisionShape() {
		this.setCollisionShape(new Rectangle(this.getX(),this.getY(),BIRD_WIDTH-3,BIRD_HEIGHT-3));
	}

	
	public void updateBird(int levelSpeed){
            this.setX(this.getX() + this.getXSpeed() - levelSpeed);
            
            //-------------check to see if it drifted off the screen--------
            if (this.getX() < (0 - Bird.BIRD_WIDTH)){//it drifted off the screen

                this.setX(this.getInitialX());
                this.setY(this.getInitialY());
            }
            else if (this.getX() > (HeliGameMain.GAME_WIDTH * 2)){//if the heli is outrunning them
                this.setX(0 - Bird.BIRD_WIDTH + 1);
            }
            if(this.getX() <= HeliGameMain.GAME_WIDTH){//update its animation if it is visible
                updateBirdAnimation();
            }
            //generateCollisionShape();//re-generate a new shape at its new location
            this.getCollisionRectangle2D().setRect(this.getX(),this.getY(),BIRD_WIDTH,BIRD_HEIGHT);
	}
        
        private void updateBirdAnimation(){
            this.birdAnim.updateImageAnimation();
        }

	//---------------------drawing stuff----------------------
	@Override
	public void drawSprite(Graphics g) {
            if(this.getX() <= HeliGameMain.GAME_WIDTH){//only draw it if it is visible
		if(animFrames[birdAnim.getCurrentFrameIndex()] == null){//no image for sprite
                    g.setColor(Color.RED);
                    //g.fillRect(this.canvasX, this.getY(), BIRD_WIDTH, BIRD_HEIGHT);//draw a rectangle the size of the sprite
                    g.fillRect(this.getX(), this.getY(), BIRD_WIDTH, BIRD_HEIGHT);//draw a rectangle the size of the sprite
		}
		else{
                    //this.getSpriteImage().paintIcon(null, g, this.canvasX, this.getY());
                    g.drawImage(animFrames[birdAnim.getCurrentFrameIndex()], this.getX(), this.getY(), null);
		}
            }
	}

}
