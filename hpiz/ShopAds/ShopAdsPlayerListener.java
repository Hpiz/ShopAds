/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hpiz.ShopAds;

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
 * @author <yourname>
 */
public class ShopAdsPlayerListener extends PlayerListener {
    
    
    private ShopAds plugin;
    public boolean running = false;
    private static final Logger log = Logger.getLogger("Minecraft");

    public ShopAdsPlayerListener(ShopAds instance) {
        plugin = instance;

    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.log.info("playerJoined");

               

    }



    //Insert Player related code he
}