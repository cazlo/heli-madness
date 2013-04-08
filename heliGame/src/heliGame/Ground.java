package heliGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

public class Ground extends Sprite{
	private static final String IMAGE_LOCATION = "images/ground.gif";
	private static final int TEXTURE_WIDTH = 60;
	private static final int TEXTURE_HEIGHT = 60;	
	
	private int [] xPoints;
	private int [] yPoints;
	private GeneralPath groundPath;
	//constructor for ground with arraylists 
	//make sure this is only called after a level is loaded, so that the correct arrayLists are populated
	Ground(ArrayList<Integer> xList,ArrayList<Integer> yList) {
		super(xList.get(0),yList.get(0));//construct sprite at (x0,y0)
		
		//populate the ground x and y arrays for
		this.xPoints = new int [xList.size()];
		this.yPoints = new int [yList.size()];
		int i = 0;
		for(i = 0; i < xList.size(); i ++){
			this.xPoints [i] = xList.get(i);
		}
		for (i = 0; i < yList.size(); i++){
			this.yPoints [i] = yList.get(i);
		}
		
		
	}
		
	//------------------stuff to draw the ground-----------------------
	@Override
	public void drawSprite(Graphics g) {
		drawGround((Graphics2D)g);
	}
	
	public void drawGround(Graphics2D g2){
		try {
			//BufferedImage imageIn = new BufferedImage()
			g2.setColor(Color.GRAY);
			g2.setPaint(new TexturePaint(ImageIO.read(new File(IMAGE_LOCATION)),
                                                     new Rectangle(0,0,TEXTURE_WIDTH,TEXTURE_HEIGHT)));
			
		} catch (IIOException e) {
			System.out.println("ERROR: Could not load image:"+IMAGE_LOCATION);
			//e.printStackTrace();
		} catch (IOException ex){
			System.out.println("ERROR: Could not load image:"+IMAGE_LOCATION);
			//ex.printStackTrace();
		}
		g2.fill(generateGroundPath());
		
	}

	//-----------------------collision detection stuff------------------
	@Override
	public boolean intersects(Rectangle2D rectangleIn) {
		if (this.groundPath.intersects(rectangleIn)){
			return true;
		}
		else{
			return false;
		}
	}

	public boolean containsPoint(int xIn, int yIn){
		if (this.groundPath.contains(xIn, yIn)){
			return true;
		}
		else{
			return false;
		}
	}
	@Override
	public void generateCollisionShape() {
		this.groundPath = generateGroundPath();		
	}
	private GeneralPath generateGroundPath(){
		this.groundPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD,xPoints.length);
		this.groundPath.moveTo(0, HeliGameMain.GAME_HEIGHT);
		for (int i = 0; i < xPoints.length;i++){
			this.groundPath.lineTo(xPoints[i],yPoints[i]);
		}
		this.groundPath.lineTo(xPoints[xPoints.length - 1], HeliGameMain.GAME_HEIGHT);
		this.groundPath.closePath();
		return this.groundPath;
	}
}
