/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hpiz.ShopAds;

/**
 *
 * @author Chris
 */
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.Material;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;


/**
 * <pluginname> block listener
 * @author <yourname>
 */
public class ShopAdsBlockListener extends BlockListener {
    private final ShopAds plugin;

    public ShopAdsBlockListener(final ShopAds plugin) {
        this.plugin = plugin;
    }

    //put all Block related code here
}