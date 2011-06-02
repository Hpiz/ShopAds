package org.hpiz.ShopAds;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

class timerThread extends Thread
  {

    private final Random randomGenerator;
    private final ShopAds plugin;
    private int lastAnnouncement = 0;

    public timerThread(ShopAds plugin)
      {
        this.randomGenerator = new Random();
        this.plugin = plugin;
      }

    public void run()
      {
        if (this.plugin.pluginState())
          {


            this.plugin.loadShops();

            this.plugin.loadUsers();

            if (this.plugin.getShopsLength() > 0)
              {
                if (this.plugin.random)
                  {

                    this.lastAnnouncement = (Math.abs(this.randomGenerator.nextInt()) % this.plugin.getShopsLength());

                  }
                else if (this.lastAnnouncement >= this.plugin.getShopsLength())
                  {
                    this.lastAnnouncement = 0;
                  }


                if (this.lastAnnouncement < this.plugin.getShopsLength())
                  {
                    try
                      {
                        while (this.plugin.timeUpdater(this.lastAnnouncement))
                          {
                            // System.out.println (String.valueOf(this.plugin.timeUpdater(this.lastAnnouncement)));
                            lastAnnouncement++;
                          }
                      }
                    catch (FileNotFoundException ex)
                      {
                        Logger.getLogger(timerThread.class.getName()).log(Level.SEVERE, null, ex);
                      }
                    catch (IOException ex)
                      {
                        Logger.getLogger(timerThread.class.getName()).log(Level.SEVERE, null, ex);
                      }
                    this.plugin.announce(this.lastAnnouncement);
                  }
              }
          }
      }
  }