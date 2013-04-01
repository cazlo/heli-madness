package heliGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Random;

//import javax.swing.ImageIcon;

public class Bird extends Sprite {
	//---------constants-----------
	private static final String IMAGE_NAME = "birdSprite.gif";
	
	static final int MIN_ALTITUDE = 450;//the lowest they will go
	static final int BIRD_WIDTH = 20;
	static final int BIRD_HEIGHT = 20;
	static final int MAX_SPEED = 3;
        static final int MAX_BIRDS_NORMAL = 35;
        static final int MAX_BIRDS_DEBUG = 15;
	
	static int MAX_BIRDS = MAX_BIRDS_NORMAL;
	//---------variables------------
	private int canvasX;
	private int initialY;
	
	//----------------------------stuff for animation-------------------------------
	private static final int [] birdAnimImageNumbers = {1,2,3};
	private static final String birdAnimLocation = "images/bird/birdFrame";
	private static int birdAnimDelay = MAX_BIRDS * 10;
	private static AnimationUtils birdAnim;
	
	//--------------constructor--------------
	Bird(int xIn,int yIn, boolean initAnimation){
                super(xIn,yIn);
                if(HeliGameMain.DEBUG){
                        Bird.MAX_BIRDS = MAX_BIRDS_DEBUG;
                        birdAnimDelay = MAX_BIRDS * 10;
                }
                else{
                        Bird.MAX_BIRDS = MAX_BIRDS_NORMAL;
                        birdAnimDelay = MAX_BIRDS * 10;
                }
                
		if (initAnimation){
			birdAnim = new AnimationUtils(birdAnimImageNumbers.length,
                                                  birdAnimLocation,
                                                  birdAnimImageNumbers,
                                                  ".png",
                                                  birdAnimDelay,
                                                  AnimationUtils.LoopTypes.LOOP_FOREVER );
		}
		//this.setXSpeed(-1);
		this.setXSpeed(0-(new Random().nextInt(MAX_SPEED) + 1));//random speed birds in 1 direction
		this.setInitialY(yIn);
		this.generateCollisionShape();
	}
	
	//-----------------getters and setters--------------
	public int getCanvasX(){
		return canvasX;
	}
	public void setCanvasX(int canvasXIn){
		canvasX = canvasXIn;
	}
	
	public int getInitialY() {
		return initialY;
	}

	public void setInitialY(int initialYIn) {
		this.initialY = initialYIn;
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
		this.setCollisionShape(new Rectangle(this.getX(),this.getY(),BIRD_WIDTH,BIRD_HEIGHT));
	}

	
	public void updateBird(){
		//this.setX(this.getX() + this.getXSpeed());
		updateSprite();
		birdAnim.updateImageAnimation();
		//generateCollisionShape();//re-generate a new shape at its new location
		this.getCollisionRectangle2D().setRect(this.getX(),this.getY(),BIRD_WIDTH,BIRD_HEIGHT);
	}

	//---------------------drawing stuff----------------------
	@Override
	public void drawSprite(Graphics g) {
		if(birdAnim.getCurrentFrame() == null){//no image for sprite
			g.setColor(Color.RED);
			g.fillRect(this.canvasX, this.getY(), BIRD_WIDTH, BIRD_HEIGHT);//draw a rectangle the size of the sprite
		}
		else{
			//this.getSpriteImage().paintIcon(null, g, this.canvasX, this.getY());
			g.drawImage(birdAnim.getCurrentFrame(), canvasX, this.getY(), null);
		}
	}

}
