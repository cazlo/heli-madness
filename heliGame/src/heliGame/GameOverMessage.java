/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//
package heliGame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GameOverMessage {
    int x, y;
    String gameOverCause;
    
    GameOverMessage(String reason){
        this.x = HeliGameMain.GAME_WIDTH/2;
        this.y = HeliGameMain.GAME_HEIGHT/2;
        gameOverCause = reason;
    }
    
    public void draw(Graphics g){
        Graphics g2 = (Graphics2D)g;
        
        g2.setFont(new Font("Lucidia", Font.BOLD, 96));
        g2.setColor(Color.BLACK);
        g2.drawString("Game Over",
                     x - (g2.getFontMetrics().stringWidth("Game Over")/2),
                     y);
        int heightOfGameOver = g2.getFontMetrics().getHeight();
        
        g2.setFont(new Font("Lucidia", Font.PLAIN, 69));
        g2.drawString(this.gameOverCause,
                     x - (g2.getFontMetrics().stringWidth(this.gameOverCause)/2),
                     y + heightOfGameOver + 6);
    }
}
