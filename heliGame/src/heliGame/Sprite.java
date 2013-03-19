package heliGame;

//import java.awt.Color;
//import java.awt.Polygon;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
//import java.io.File;
//import java.net.MalformedURLException;
//import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
//import java.io.IOException;
//import javax.imageio.ImageIO;

//sprite class which graphical objects will inherit from

public abstract class Sprite {
	//static final String IMAGE_DIR = "images/"; 
	
	private BufferedImage spriteImage;
	//private ImageIcon spriteIcon; //use ImageIcon for its support of animated gifs
	//private int height;
	//private int width;
	private int x; //x coordinate relative to the whole level
	private int y; //y coordinate relative to the whole level
	private int xSpeed; //x velocity
	private Shape collisionShape;
	
	
	Sprite(int xPos, int yPos){
		this.setX(xPos);
		this.setY(yPos);
		this.xSpeed = 0;
		this.spriteImage = null;
	}
	
	Sprite(int xPos, int yPos, String imageName){//, Polygon collisionShape){
		this.setX(xPos);
		this.setY(yPos);
		this.spriteImage = ImageUtils.staticLoadImage(imageName);
		//this.spriteIcon = loadImage(IMAGE_DIR + imageName);
		this.xSpeed = 0;
	}
	//-----------------------abstract stuff--------------
	//must be overridden and implemented for any sprites
	
	public abstract boolean intersects(Rectangle2D rectangleIn);
	public abstract void generateCollisionShape();
	public abstract void drawSprite(Graphics g);
	/*
	protected void drawSprite(Graphics g){
		if(spriteImage == null){//no image for sprite
			g.setColor(Color.RED);
			g.fillRect(canvasX, y, width, height);//draw a rectangle the size of the sprite
		}
		else{
			g.drawImage(spriteImage, canvasX, y, null);
		}
	}*/

	//------------------stuff which will get inherited-------------
	//-----------------------getters and seters
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public int getXSpeed(){
		return xSpeed;
	}
	public void setXSpeed(int xSpeedIn){
		xSpeed = xSpeedIn;
	}
	public void setX(int xIn){
		x = xIn;
	}
	public void setY(int yIn){
		y = yIn;
	}
	/*
	public void setWidth(int widthIn){
		width = widthIn;
	}
	
	public void setHeight(int heightIn){
		height = heightIn;
	}*/
	
	public Shape getCollisionShape(){
		return collisionShape;
	}
	public Rectangle2D getCollisionRectangle2D(){
		return (Rectangle2D)collisionShape;
	}
	protected void setCollisionShape(Shape shapeIn){
		collisionShape = shapeIn;
	}
	
	public BufferedImage getSpriteImage() {
		return spriteImage;
		//return spriteIcon;
	}
	public void setSpriteImage(BufferedImage spriteImage) {
		this.spriteImage = spriteImage;
		//this.spriteIcon = spriteImage;
	}
	
	//-----------------------update x location of sprite----------------
	public void updateSprite(){
		x += xSpeed;
	}
	
	//-----------------------load image---------------
	/*
	public BufferedImage loadImage(String fileName){
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(fileName));
		}
		catch(IOException e) {
			System.out.println("ERROR: could not load image:" + fileName);
			image = null;
		}
		return image;
	}*
	
	//--------------load image icon-------------------
	public ImageIcon loadImage(String fileName){
		ImageIcon image = null;
		File fileIn = new File(fileName);
		if (fileIn.exists()){
			try {
				image = new ImageIcon(fileIn.toURI().toURL());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.out.println("ERROR: could not load image:" + fileName);
			image = null;
		}
		return image;
	}*/
}
