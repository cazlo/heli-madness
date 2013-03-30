package heliGame;

import heliGame.AnimationUtils.LoopTypes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

public class Explosion extends Sprite {
	private int width, height;
	
	
	//----------------------------stuff for animation-------------------------------
	private static final int [] explosionAnimImageNumbers = {1,2,3};
	private static final String explosionAnimLocation = "images/explosion/explosionFrame";
	private static final int explosionAnimDelay = 20;
	private AnimationUtils explosionAnim;
	
	
	//---------------------constructor------------------
	Explosion(int xIn, int yIn, int widthIn, int heightIn){
		super(xIn, yIn);
		this.width = widthIn;
		this.height = heightIn;
		explosionAnim = new AnimationUtils(explosionAnimImageNumbers.length,
                                               explosionAnimLocation,
                                               explosionAnimImageNumbers,
                                               ".png",
                                               explosionAnimDelay,
                                               LoopTypes.PLAY_ONCE );
		
	}
	//-----------------update the explosion---------------
	public void updateExplosion(){
		explosionAnim.updateImageAnimation();
	}
	
	//----------------------inherited abstract stuff-----------------
	@Override
	public boolean intersects(Rectangle2D rectangleIn) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void generateCollisionShape() {
		// TODO Auto-generated method stub

	}

	//--------------------------method to draw the sprite------------------
	@Override
	public void drawSprite(Graphics g) {
		if(explosionAnim.getCurrentFrame() == null){//no image for sprite
			g.setColor(Color.RED);
			g.fillRect(this.getX(), this.getY(), width, height);//draw a rectangle the size of the sprite
		}
		else{
			//this.getSpriteImage().paintIcon(null, g, this.canvasX, this.getY());
			g.drawImage(explosionAnim.getCurrentFrame(), this.getX(), this.getY(), null);
		}
	}
	public boolean doneExploding() {
		if (this.explosionAnim.animationIsDone()){
			return true;
		}
		else{
			return false;
		}
	}

}
