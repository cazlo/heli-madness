package heliGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public class Ring extends Sprite{

	private static final String IMAGE_NAME = "images/ringSprite.png";
	static final int RING_WIDTH = 20;
	static final int RING_HEIGHT = 50;
	static final int RING_VALUE = 50;//the value in poitns for hitting a ring
	
	private int canvasX;

	Ring(int xPos,int yPos){
		super(xPos, yPos, IMAGE_NAME);
		//this.setWidth(RING_WIDTH);
		//this.setHeight(RING_HEIGHT);
		this.generateCollisionShape();
		this.canvasX = xPos;
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
		if(this.getSpriteImage() == null){//no image for sprite
			g.setColor(Color.YELLOW);
			g.fillRect(this.getCanvasX(), this.getY(), RING_WIDTH, RING_HEIGHT);//draw a rectangle the size of the sprite
		}
		else{
			//this.getSpriteImage().paintIcon(null, g, this.getCanvasX(), this.getY());
			g.drawImage(this.getSpriteImage(), this.getX(), this.getY(), null);
		}
	}
}
