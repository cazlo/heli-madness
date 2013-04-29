package heliGame;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GameOverMessage {
    int x, y;
    String gameOverCause, scoreString;
    int score, maxScore;
    
    GameOverMessage(String reason, int scoreIn, int maxScoreIn ){
        //set its position to the middle of the screen
        this.x = HeliGameMain.GAME_WIDTH/2;
        this.y = HeliGameMain.GAME_HEIGHT/2;
        gameOverCause = reason;
        score = scoreIn;
        maxScore = maxScoreIn;
        scoreString = "";
    }
    
    public void draw(Graphics g){
        Graphics g2 = (Graphics2D)g;
        
        g2.setFont(new Font("Lucidia", Font.BOLD, 96));
        g2.setColor(Color.WHITE);
        g2.drawString("Game Over",
                     x - (g2.getFontMetrics().stringWidth("Game Over")/2),
                     y);
        int heightOfMessage = g2.getFontMetrics().getHeight() + 6;
        
        g2.setFont(new Font("Lucidia", Font.PLAIN, 69));
        g2.drawString(this.gameOverCause,
                     x - (g2.getFontMetrics().stringWidth(this.gameOverCause)/2),
                     y + heightOfMessage);
        heightOfMessage += g2.getFontMetrics().getHeight() + 6;
        
        this.scoreString = "Score: "+score+"/"+maxScore;
        g2.drawString(this.scoreString,
                     x - (g2.getFontMetrics().stringWidth(this.scoreString)/2),
                     y + heightOfMessage);
    }
}
