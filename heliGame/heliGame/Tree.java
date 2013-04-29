package heliGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

public class Tree extends Sprite{
	private static final String IMAGE_LOCATION = "images/treeSprite.png";
	private static final int TRUNK_WIDTH = 30;
	private static final int TRUNK_HEIGHT = 90;
	public static final int CANOPY_RADIUS = 40;;
	private Rectangle2D trunkCollisionRectangle;
	
	public Tree(int xPos, int yPos) {
		super(xPos, yPos, IMAGE_LOCATION);
		//this.setWidth(TRUNK_WIDTH);
		//this.setHeight(TRUNK_HEIGHT);
		generateCollisionShape();
	}
	
	
	//----------------stuff for collision detection------------

	@Override
	public boolean intersects(Rectangle2D rectangleIn) {
		// TODO Auto-generated method stub
		if (rectangleIn.intersects(trunkCollisionRectangle)){
			return true;
		}
		else if (this.getCollisionShape().intersects(rectangleIn)){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public void generateCollisionShape() {
		// TODO Auto-generated method stub
		this.setCollisionShape(new Ellipse2D.Double(this.getX(),this.getY(), 2*CANOPY_RADIUS, 2*CANOPY_RADIUS));
		trunkCollisionRectangle = new Rectangle2D.Double(
				this.getX() + CANOPY_RADIUS - (TRUNK_WIDTH/2), this.getY() + 2*CANOPY_RADIUS, TRUNK_WIDTH, TRUNK_HEIGHT);
	}


	@Override
	public void drawSprite(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		// TODO Auto-generated method stub
		if(this.getSpriteImage() == null){//no image for sprite
			g2.setColor(new Color(156, 93, 82));
			g2.fill(trunkCollisionRectangle);//draw a rectangle the size of the sprite
			g2.setColor(Color.GREEN);
			g2.fill(this.getCollisionShape());
		}
		else{
			//this.getSpriteImage().paintIcon(null, g, this.getX(), this.getY());
			g.drawImage(this.getSpriteImage(), this.getX(), this.getY(), null);
		}
	}

	
	
}
