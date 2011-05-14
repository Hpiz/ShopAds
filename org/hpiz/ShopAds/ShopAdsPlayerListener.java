/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hpiz.ShopAds;

/**
 *
 * @author Chris
 */

import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;


/**
 * Handle events for all Player related events
 * @author hpiz
 */
public class ShopAdsPlayerListener extends PlayerListener {
    
    
    private ShopAds plugin;
    public boolean running = false;


    public ShopAdsPlayerListener(ShopAds instance) {
        plugin = instance;

    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        

               

    }



    //Insert Player related code he
}