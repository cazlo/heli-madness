package heliGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public class Ring extends Sprite{

	//private static final String IMAGE_NAME = "images/ringSprite.png";
	static final int RING_WIDTH = 20;
	static final int RING_HEIGHT = 50;
	static final int RING_VALUE = 50;//the value in poitns for hitting a ring
	
        //----------------------------stuff for animation-------------------------------
	private static final int [] ringAnimImageNumbers = {1,2,3,2};
	private static final String ringAnimLocation = "images/ring/ringFrame";
	private static int ringAnimDelay = 5;
        private AnimationUtils ringAnim;
        
	private int canvasX;

	Ring(int xPos,int yPos){
            //super(xPos, yPos, IMAGE_NAME);
            super(xPos, yPos);
            this.ringAnim = new AnimationUtils(ringAnimImageNumbers.length,
                                          ringAnimLocation,
                                          ringAnimImageNumbers,
                                          ".png",
                                          ringAnimDelay,
                                          AnimationUtils.LoopTypes.LOOP_FOREVER );
            Random rng = new Random();//set the animation to a random frame, so theyre not all moving in unison
            this.ringAnim.setCurrentFrame(ringAnimImageNumbers[rng.nextInt(ringAnimImageNumbers.length)]);
            this.generateCollisionShape();
            this.canvasX = xPos;
	}
        
        public void updateAnimation(){
            this.ringAnim.updateImageAnimation();
        }

	public int getCanvasX() {
            return canvasX;
	}

	public void setCanvasX(int canvasXIn) {
            this.canvasX = canvasXIn;
	}

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
		this.setCollisionShape(new Rectangle(this.getX(),this.getY(),RING_WIDTH,RING_HEIGHT));
	}

	@Override
	public void drawSprite(Graphics g) {
            //if(this.getSpriteImage() == null){//no image for sprite
            if(ringAnim.getCurrentFrame() == null){//no image for sprite
                g.setColor(Color.YELLOW);
                g.fillRect(this.getCanvasX(), this.getY(), RING_WIDTH, RING_HEIGHT);//draw a rectangle the size of the sprite
            }
            else{
                //this.getSpriteImage().paintIcon(null, g, this.getCanvasX(), this.getY());
                //g.drawImage(this.getSpriteImage(), this.getCanvasX(), this.getY(), null);
                g.drawImage(ringAnim.getCurrentFrame(), this.getCanvasX(), this.getY(), null);
            }
	}
}
