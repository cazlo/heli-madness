/*
 * The finish line.  If the heli crosses it totally and lands, the level is
 * completed
 */
package heliGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public class FinishLine extends Sprite{

    private static final String imageLocation = "images/finishLineSprite.png";
    public static final int HEIGHT = 100;
    public static final int WIDTH = 20;
    
    private int levelLength;
    
    FinishLine(int xIn, int yIn, int levelLengthIn){
        super(xIn, yIn, imageLocation);
        levelLength = levelLengthIn;
        this.generateCollisionShape();
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
    
    public boolean contains(Rectangle2D rectangleIn){
        if (((Rectangle2D)this.getCollisionShape()).contains(rectangleIn)){
            return true;
        }
        else{
            return false;
        }        
    }

    @Override
    public void generateCollisionShape() {
        this.setCollisionShape(new Rectangle(this.getX(),0,levelLength - this.getX(),HeliGameMain.GAME_HEIGHT));
    }

    @Override
    public void drawSprite(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
		// TODO Auto-generated method stub
		if(this.getSpriteImage() == null){//no image for sprite
			g2.setColor(Color.RED);
			g2.fillRect(this.getX(), this.getY(), WIDTH, HEIGHT);
		}
		else{
			//this.getSpriteImage().paintIcon(null, g, this.getX(), this.getY());
			g.drawImage(this.getSpriteImage(), this.getX(), this.getY(), null);
		}
    }
    
}
