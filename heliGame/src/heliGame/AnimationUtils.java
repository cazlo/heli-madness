package heliGame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.*;

public class AnimationUtils {
    static enum LoopTypes{
            PLAY_ONCE, LOOP_FOREVER
    }
    private LoopTypes loopType;

    private BufferedImage [] imageFrames;
    private int currentFrameNum;
    private int numFrames;
    private int animationDelay;//number of updates before advancing to next image
    private int updatesSinceAnimation;
    private boolean animationDone;

    AnimationUtils(int numFramesIn,
                   String baseFileName,
                   int [] fileNumbers,
                   String fileExtension,
                   int animationDelayIn,
                   LoopTypes loopTypeIn){
        this.numFrames = numFramesIn;
        this.currentFrameNum = 0;
        this.animationDelay = animationDelayIn;
        this.loopType = loopTypeIn;
        this.setAnimationDone(false);

        loadImageFrames(baseFileName,fileNumbers, fileExtension);
    }
    
    AnimationUtils(int numFramesIn,
                   int animDelay,
                   LoopTypes loopTypeIn){//,
                   //BufferedImage [] imageFramesReference){
        this.numFrames = numFramesIn;
        this.currentFrameNum = 0;
        this.animationDelay = animDelay;
        this.loopType = loopTypeIn;
        this.setAnimationDone(false);
        imageFrames = null;
        //imageFrames = imageFramesReference;
    }

    //---------------tools to load images-----------------
    private void loadImageFrames(String baseFileName,
                                 int [] fileNumbers,
                                 String fileExtension){
        imageFrames = new BufferedImage[this.numFrames];
        for (int currentImage = 0; currentImage < this.numFrames; currentImage++){
                imageFrames[currentImage] = loadImage(baseFileName + fileNumbers[currentImage] + fileExtension);
        }
    }

    private BufferedImage loadImage(String fileName){
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(fileName));
        }
        catch(IOException e) {
            System.out.println("ERROR: could not load image:" + fileName);
            image = null;
        }
        return image;
    }

    //load an image statically (called by AnimationUtils.staticLoadImage(filename)
    public static BufferedImage staticLoadImage(String fileName){
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(fileName));
        }
        catch(IOException e) {
            System.out.println("ERROR: could not load image:" + fileName);
            image = null;
        }
        return image;
    }

    //---------------------------tools for animation----------------
    public void updateImageAnimation(){
        switch (this.loopType){
            case PLAY_ONCE:
                if (this.currentFrameNum == this.numFrames - 1){
                    this.setAnimationDone(true);
                }
                else if (this.updatesSinceAnimation < this.animationDelay){
                    this.updatesSinceAnimation++;
                }
                else if (this.updatesSinceAnimation == this.animationDelay){
                    if (this.currentFrameNum < this.numFrames - 1){
                        this.currentFrameNum++;
                        //if (this.currentFrameNum == this.numFrames - 1){
                        //	this.setAnimationDone(true);
                        //}
                    this.updatesSinceAnimation = 0;
                    }
                }
                break;
            case LOOP_FOREVER:
                if (this.updatesSinceAnimation < this.animationDelay){
                    this.updatesSinceAnimation++;
                }
                else if (this.updatesSinceAnimation == this.animationDelay){
                    if (this.currentFrameNum < this.numFrames- 1){
                        this.currentFrameNum++;
                    }
                    else if (this.currentFrameNum == this.numFrames - 1){
                            this.currentFrameNum = 0;
                    }
                    this.updatesSinceAnimation = 0;
                }
                break;
        }
    }

    public BufferedImage getCurrentFrame(){
        if (imageFrames == null){
            return null;
        }
        else{
            return imageFrames[currentFrameNum];
        }
    }
    
    public int getCurrentFrameIndex(){
        return currentFrameNum;
    }

    public boolean animationIsDone() {
        return animationDone;
    }

    public void setAnimationDone(boolean animationDone) {
        this.animationDone = animationDone;
    }	

    public void setCurrentFrame(int frameNum){
        this.currentFrameNum = frameNum;
    }
}
