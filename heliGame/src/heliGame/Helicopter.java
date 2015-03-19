package heliGame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Helicopter extends Sprite{	
    private static final String IMAGE_NAME = "heliSprite.gif";
    static final int MAX_X_SPEED = 10;//in pixels
    static final int MAX_Y_SPEED = 4;
    static final int MAX_X_LANDING_SPEED = 2;
    static final int MAX_Y_LANDING_SPEED = -2;
    static final int DROP_STEP_HEIGHT = 150;//the step height at which the heli wil lose more speed
                                            //if it falls this height without engine power, the heli will
                                            //fall even quicker
    //static final int LIFT_OFF_SPEED = 100;

    static final int HELI_WIDTH = 55;
    static final int HELI_HEIGHT = 43;
    static final int TOP_OFFSET = 10;//how much of the image is empty at the top
                                     //in order to see the whole helicopter when
                                     //it is rotated
    static final int BOTTOM_OFFSET = 8;//how much room the landing skids take up

    public static enum ThrottleStates{//the state of the throttle which effects the amount of lift produced by the engine
        //IDLE means the blades are not moving; the engine for the heli is off
        //NO_LIFT means the blades are moving, but not provifing enough lift
        //HOVER means the blades are producing just enough lift to hover
        //LIFT means the blades are prducing enough lift to accelerate the heli up
        IDLE, NO_LIFT, HOVER, LIFT
    }



    public static enum MovementStates{//the state of horizontal movement of the heli
        STATIC_LEFT, STATIC_RIGHT, MOVING_LEFT, MOVING_RIGHT, TURNING_R2L, TURNING_L2R		
    }

    private ThrottleStates throttleStatus;
    private MovementStates movementStatus;

    private int canvasX;//location relative to the visible screen
    private int ySpeed; //y velocity; + indicates that it is moving up, - indicates moving down
    //private int rotorSpeed; //the speed of the rotor, depends on how much throttle is given
    private int fallYTop;//the y at which the heli begins to fall
                         //which is the point which its rotor state goes from no_lift to idle
                         //in reality if the engine failed, the blades would still be spinning,
                         //providing some lift
    private boolean landed;
    private int theta;//amount of rotation the heli is tilted 
    static final int THETA_STEP_SIZE = 2; 
    /**------------------------------------------------------------------------------
    //-------------------------------stuff for animation-------------------------------
    -------------------------------------------------------------------------------*/
    //---------------------------------the static images-----------------------------
    private BufferedImage staticFacingLeft, staticFacingRight;
    private static final String staticLeftLocation = "images/heli/static/Left.png";
    static final String staticRightLocation = "images/heli/static/Right.png";
    //--------------------------------the animated images------------------------------
    AnimationUtils facingRightAnim, facingLeftAnim, turningRightToLeft, turningLeftToRight;//, hoverAnim;
    private static final int [] rightImageNumbers = {1,2,3};
    private static final int [] leftImageNumbers = {1,2,3};
    //private static final int [] hoverImageNumbers = {1,2,3};
    private static final int [] turningR2LNumbers = {1,2,3};
    private static final int [] turningL2RNumbers = {3,2,1};
    //private static final String hoverLocation = "images/heli/hover/HoverFrame";
    private static final String rightLocation = "images/heli/facingRight/rightFrame";
    private static final String leftLocation = "images/heli/facingLeft/leftFrame";
    private static final String turningLocation = "images/heli/turning/turningFrame";
    //private static final int hoverAnimDelay = 20;
    private static final int rightAnimDelay = 5;
    private static final int leftAnimDelay = 5;
    private static final int turningAnimDelay = 7;

    //---------------------------------constructor------------------------
    Helicopter(int xIn, int yIn){
        super(xIn, yIn);
        staticFacingLeft = AnimationUtils.staticLoadImage(staticLeftLocation);
        staticFacingRight = AnimationUtils.staticLoadImage(staticRightLocation);
        facingRightAnim = new AnimationUtils(rightImageNumbers.length,
                                             rightLocation,
                                             rightImageNumbers,
                                             ".png",
                                             rightAnimDelay,
                                             AnimationUtils.LoopTypes.LOOP_FOREVER);
        facingLeftAnim = new AnimationUtils(leftImageNumbers.length,
                                            leftLocation,
                                            leftImageNumbers,
                                            ".png",
                                            leftAnimDelay,
                                            AnimationUtils.LoopTypes.LOOP_FOREVER);
        turningRightToLeft = new AnimationUtils(turningR2LNumbers.length,
                                                turningLocation,
                                                turningR2LNumbers,
                                                ".png",
                                                turningAnimDelay,
                                                AnimationUtils.LoopTypes.PLAY_ONCE);
        turningLeftToRight = new AnimationUtils(turningL2RNumbers.length,
                                                turningLocation,
                                                turningL2RNumbers,
                                                ".png",
                                                turningAnimDelay,
                                                AnimationUtils.LoopTypes.PLAY_ONCE);
        this.movementStatus = MovementStates.STATIC_RIGHT;
        //hoverAnim = new ImageUtils(hoverImageNumbers.length, hoverLocation,hoverImageNumbers, ".png", hoverAnimDelay, ImageUtils.LoopTypes.LOOP_FOREVER);

        this.ySpeed = 0;
        this.throttleStatus = ThrottleStates.IDLE;
        this.landed = true;
        this.theta = 0;
        //this.rotorSpeed = 0;
        //this.setWidth(HELI_WIDTH);
        //this.setHeight(HELI_HEIGHT);
        this.fallYTop = 20000;//initialize it to really high number so that willCrossAccelerationPoint() works right
        generateCollisionShape();
    }

    //---------------methods for drawing the sprite------------------
    @Override
    public void drawSprite(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        switch (this.movementStatus){
            case STATIC_LEFT:
                drawSpriteOrRect(staticFacingLeft, g2);
                //g2.drawImage(updateSpriteRotation().filter(staticFacingLeft, null), canvasX, this.getY(), null);
                break;
            case STATIC_RIGHT:
                drawSpriteOrRect(staticFacingRight, g2);
                //g2.drawImage(updateSpriteRotation().filter(staticFacingRight, null), canvasX, this.getY(), null);
                break;
            case MOVING_LEFT:
                drawSpriteOrRect(this.facingLeftAnim.getCurrentFrame(), g2);
                //g2.drawImage(updateSpriteRotation().filter(this.facingLeftAnim.getCurrentFrame(), null), canvasX, this.getY(), null);
                break;
            case MOVING_RIGHT:
                drawSpriteOrRect(this.facingRightAnim.getCurrentFrame(), g2);
                //g2.drawImage(updateSpriteRotation().filter(this.facingRightAnim.getCurrentFrame(), null), canvasX, this.getY(), null);
                break;
            case TURNING_R2L:
                drawSpriteOrRect(this.turningRightToLeft.getCurrentFrame(), g2);
                //g2.drawImage(updateSpriteRotation().filter(this.turningRightToLeft.getCurrentFrame(), null), canvasX, this.getY(), null);
                break;
            case TURNING_L2R:
                drawSpriteOrRect(this.turningLeftToRight.getCurrentFrame(), g2);
                //g2.drawImage(updateSpriteRotation().filter(this.turningLeftToRight.getCurrentFrame(), null), canvasX, this.getY(), null);
                break;
        }
    }

    private void drawSpriteOrRect(BufferedImage imageIn, Graphics2D g2){
        if (imageIn == null){
            g2.setColor(Color.RED);
            g2.fillRect(this.getCanvasX(), this.getY(), HELI_WIDTH, HELI_HEIGHT);//draw a rectangle the size of the sprite
        }
        else{
            g2.drawImage(updateSpriteRotation().filter(imageIn, null), canvasX, this.getY(), null);
        }
    }

    private AffineTransformOp updateSpriteRotation(){
        double rotationRequired = Math.toRadians(this.getTheta());
        AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, HELI_WIDTH/2, HELI_HEIGHT/2); 
        return new AffineTransformOp(tx,AffineTransformOp.TYPE_BILINEAR);
    }

    //-------------------Getters and setters----------------
    public int getCanvasX() {
        return canvasX;
    }
    public void setCanvasX(int canvasXIn) {
        this.canvasX = canvasXIn;
    }
    public int getYSpeed(){
        return ySpeed;
    }
    //public int getRotorSpeed(){
    //	return rotorSpeed;
    //}

    public void setYSpeed(int ySpeedIn){
        this.ySpeed = ySpeedIn;
    }

    //public void setRotorSpeed(int rotorSpeedIn){
    //	this.rotorSpeed = rotorSpeedIn;
    //}

    public int getFallYTop() {
        return fallYTop;
    }

    public void setFallYTop(int fallYTop) {
        this.fallYTop = fallYTop;
    }

    public void setThrottleStatus(ThrottleStates stateIn){
        this.throttleStatus = stateIn;
    }

    public ThrottleStates getThrottleStatus(){
        return this.throttleStatus;
    }
    /*
    public CanvasStates getCanvasStatus() {
            return canvasStatus;
    }

    public void setCanvasStatus(CanvasStates canvasStatus) {
            this.canvasStatus = canvasStatus;
    }*/
    public MovementStates getMovementStatus() {
        return movementStatus;
    }

    public void setMovementStatus(MovementStates movementStatus) {
        this.movementStatus = movementStatus;
    }


    public int getTheta() {
        return theta;
    }

    public void setTheta(int thetaIn) {
        this.theta = thetaIn;
    }

    public boolean isLanded() {
        return landed;
    }

    public void setLanded(boolean landed) {
        this.landed = landed;
    }

    //-----------------methods for collision detection---------------------
    @Override
    public boolean intersects(Rectangle2D rectangleIn) {
        if (rectangleIn.intersects(this.getCollisionRectangle2D())){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void generateCollisionShape() {
            this.setCollisionShape(new Rectangle(this.getX(),this.getY() + 10,HELI_WIDTH,HELI_HEIGHT - 10));
    }
    
    //returns a Rectangle2D representation of a slightly smaller collision box
    //relative to the canvas (game screen)
    public Rectangle getCanvasCollisionBox(){
        return( new Rectangle(this.getCanvasX(),
                              this.getY() + TOP_OFFSET,
                              HELI_WIDTH,
                              HELI_HEIGHT-(TOP_OFFSET + BOTTOM_OFFSET)));//dont include the landing skids
    } 

    //-------method to update the y of the heli, and x relative to level for heli----------------
    public void updateHeli() {
        switch (this.throttleStatus){
            case IDLE:
                if (this.getY() < HeliGameMain.GAME_HEIGHT){//only move it down if its not on the bottom
                    this.setY(this.getY() - this.ySpeed);
                }
                break;
            case NO_LIFT:
                if (this.getY() < HeliGameMain.GAME_HEIGHT){//only move it down if its not on the bottom
                    this.setY(this.getY() - this.ySpeed);
                }
                break;
            case HOVER:
                break;
            case LIFT:
                if(this.getY() > 0){//only move it up if its not at the top
                    this.setY(this.getY() - this.ySpeed);
                }
                break;
        }

        switch (this.movementStatus){
            case STATIC_LEFT:
            case STATIC_RIGHT:
                //don't need to update anim for static sprites
                break;
            case MOVING_LEFT:
                this.facingLeftAnim.updateImageAnimation();
                break;
            case MOVING_RIGHT:
                this.facingRightAnim.updateImageAnimation();
                break;
            case TURNING_R2L:
                if (this.turningRightToLeft.animationIsDone()){
                    if (this.getThrottleStatus() == ThrottleStates.IDLE){
                        this.setMovementStatus(MovementStates.STATIC_LEFT);
                    }
                    else{
                        this.setMovementStatus(MovementStates.MOVING_LEFT);
                    }
                    //--reset the animation for next time-----
                    this.turningRightToLeft.setCurrentFrame(0);
                    this.turningRightToLeft.setAnimationDone(false);
                }
                else{
                    this.turningRightToLeft.updateImageAnimation();
                }
                break;
            case TURNING_L2R:
                if (this.turningLeftToRight.animationIsDone()){
                    if (this.getThrottleStatus() == ThrottleStates.IDLE){
                        this.setMovementStatus(MovementStates.STATIC_RIGHT);
                    }
                    else{
                        this.setMovementStatus(MovementStates.MOVING_RIGHT);
                    }
                    //--reset the animation for next time-----
                    this.turningLeftToRight.setCurrentFrame(0);
                    this.turningLeftToRight.setAnimationDone(false);
                }
                else{
                    this.turningLeftToRight.updateImageAnimation();
                }
                break;
        }

        this.updateSprite();//updates the x relative to the level for the heli
        //this.generateCollisionShape();//re-generate collision shape at the new position
        this.getCollisionRectangle2D().setRect(this.getX(),
                                               this.getY(),
                                               HELI_WIDTH,
                                               HELI_HEIGHT);
        //move its collision shape to the new position
    }

    //-----------------game logic rules--------------------

    public boolean willCrossTopOfCanvas(){
        if (this.ySpeed > 0){ //moving up
            if((this.ySpeed + this.getY()) <= 0){
                return true;
            }
            else{
                return false;
            }
        }
        else{//speed is 0 or moving down
            return false;
        }
    }
    public boolean willCrossBottomOfCanvas(){
        if (this.ySpeed < 0){ //moving down
            if((this.ySpeed + this.getY()) >= (HeliGameMain.GAME_HEIGHT - HELI_HEIGHT)){
                return true;
            }
            else{
                return false;
            }
        }
        else{//speed is 0 or moving up
            return false;
        }
    }

    public boolean willCrossSideOfCanvas(int levelLength){//returns true if the next update will put the heli past the canvas boundary
        if (this.getXSpeed() < 0){//moving to the left
            if(this.getXSpeed() + this.getX() < 0){
                return true;
            }
            else{
                return false;
            }
        }
        else if (this.getXSpeed() > 0){//moving to the right
            if((this.getXSpeed() + this.getX() + Helicopter.HELI_WIDTH) > levelLength){
                return true;
            }
            else{
                return false;
            }
        }
        else{//speed is 0
            return false;
        }
    }

    public boolean willCrossBeginningChange(){
        if (this.getXSpeed() < 0){//moving to the left
            if (this.getX()>= HeliGameMain.MIDDLE_OF_FRAME &&
               (this.getX() + this.getXSpeed()) <= HeliGameMain.MIDDLE_OF_FRAME){//if it crosses the first point
                return true;
            }
            else{
                return false;
            }
        }
        else if (this.getXSpeed() > 0){//moving to the right
            if (this.getX()<= HeliGameMain.MIDDLE_OF_FRAME &&
               (this.getX() + this.getXSpeed()) >= HeliGameMain.MIDDLE_OF_FRAME){//if it crosses the first point
                return true;
            }
            else{
                return false;
            }			
        }
        else{//speed is 0
            return false;
        }
    }

    public boolean willCrossEndChange(int levelLength){
        if (this.getXSpeed() < 0){
            if ((this.getX()>= (levelLength - HeliGameMain.MIDDLE_OF_FRAME - HELI_WIDTH)) &&
                (this.getX() + this.getXSpeed()) <= (levelLength - HeliGameMain.MIDDLE_OF_FRAME - HELI_WIDTH)){//if it crosses the end point
                return true;
            }
            else{
                return false;
            }
        }
        else if (this.getXSpeed() > 0){
            if ((this.getX()<= (levelLength - HeliGameMain.MIDDLE_OF_FRAME - HELI_WIDTH)) &&
                (this.getX() + this.getXSpeed()) >= (levelLength - HeliGameMain.MIDDLE_OF_FRAME - HELI_WIDTH)){//if it crosses the end point
                return true;
            }
            else{
                return false;
            }			
        }
        else{//speed is 0
            return false;
        }
    }

    public boolean willCrossAccelerationPoint(){
        if (this.getYSpeed() < 0){
            if ((this.getY()<= (this.getFallYTop() + Helicopter.DROP_STEP_HEIGHT)) &&
                ((this.getY() - this.getYSpeed()) >= (this.getFallYTop() + Helicopter.DROP_STEP_HEIGHT))){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
}
