/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hpiz.ShopAds;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread which handles the announcing.
 *
 * @author Michael Hohl
 */
class timerThread extends Thread {
    /** Tool used for generating random numbers. */
    private final Random randomGenerator;

    /** The plugin which holds this thread. */
    private final ShopAds plugin;

    /** The last announcement index. (Only for sequential announcing.) */
    private int lastAnnouncement = 0;

    /**
     * Allocates a new scheduled announcer thread.
     *
     * @param plugin the plugin which holds the thread.
     */
    public timerThread(ShopAds plugin) {
        randomGenerator = new Random();
        this.plugin=plugin;
    }

    /** The main method of the thread. */
    @Override
    public void run() {
        try {
            plugin.timeUpdater();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(timerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(timerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (plugin.pluginState()) {
            if (plugin.random) {
                lastAnnouncement = Math.abs(randomGenerator.nextInt()) % plugin.getMessagesLength();
            } else {
                if ((++lastAnnouncement) >= plugin.getMessagesLength()) {
                    lastAnnouncement = 0;
                }
            }

            if (lastAnnouncement < plugin.getMessagesLength()) {
                
                    plugin.announce(lastAnnouncement);
                try {
                    plugin.timeUpdater();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(timerThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(timerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
                    
         
                }
            }
        }
    }
