/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hpiz.ShopAds;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

/**
 *
 * @author Chris
 */
public class announceDelay extends Thread{

    public static void run(Player[] onlinePlayers, String[] messages, int lastMessage, boolean pluginState) {
        
        
        
        
            
            
         for (int i = 0; i < onlinePlayers.length; i++) {
             
            
            onlinePlayers[i].sendMessage(messages[lastMessage]);
            
    }
            try {
                announceDelay.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(announceDelay.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
}