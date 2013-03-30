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
    
    GameOverMessage(){
        this.x = HeliGameMain.GAME_WIDTH/2;
        this.y = HeliGameMain.GAME_HEIGHT/2;
    }
    
    public void draw(Graphics g){
        Graphics g2 = (Graphics2D)g;
        
        g2.setFont(new Font("Lucidia", Font.BOLD, 96));
        g2.setColor(Color.BLACK);
        g2.drawString("Game Over",
                     x - (g2.getFontMetrics().stringWidth("Game Over")/2),
                     y);
    }
}
