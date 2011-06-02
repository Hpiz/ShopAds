package org.hpiz.ShopAds;

import com.earth2me.essentials.Essentials;
import com.iConomy.iConomy;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class ShopAds extends JavaPlugin
  {

    private timerThread thread;
    public iConomy iConomy = null;
    public Essentials essentials;
    public static PermissionHandler permissionHandler;
    public static final Logger log = Logger.getLogger("Minecraft");
    public Server server;
    private File config = new File("plugins/ShopAds/config.yml");
    private File dir = new File("plugins/ShopAds/");
    private File userdir = new File("plugins/ShopAds/players/");
    private Properties pr = new Properties();
    private Properties ps = new Properties();
    private Properties pa;
    public boolean pluginState = false;
    private File[] listOfFiles;
    public Player[] onlinePlayers;
    public boolean random;
    private boolean sendToAll;
    private ShopAdsShop[] Shops;
    private File user = new File("plugins/ShopAds/user.dat");
    //Sets the timerThread class to "thread"

    public ShopAds()
      {
        this.thread = new timerThread(this);
      }

    @Override
    //Shuts down the plugin
    public void onDisable()
      {
        this.pluginState = false;
        log.info("[ShopAds] Disabling plugin");
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.cancelTasks(this);

      }
    //Gets the Shops number and calls announce with the ad and the Shops number if it exists, is expired, or not advertising

    public void announce(int index)
      {
        //log.info("announce 1 trying to announce index: " + String.valueOf(index));
        if ((this.Shops[index] != null) && (this.Shops[index].shopAdvertising()))
          {
            // log.info("Shop is advertising and not null");
            if (!this.Shops[index].shopExpired())
              {
                // log.info("Shop has not expired");
                if (this.Shops[index].getAd() != null)
                  {
                    // log.info("Shop has an ad");
                    announce(this.Shops[index].getAd(), index);
                  }
              }
            else
              {
                // log.info("HmM the Shop is expired");
                this.thread.run();
              }
          }
      }
    // Announces according to config, either by /ad on or off, or by sendToAll

    public void announce(String line, int index)
      {
        // log.info("announce 2");
        if (this.sendToAll)
          {
            Player[] player = getOnlinePlayers();
            for (int i = 0; i < getServer().getOnlinePlayers().length; i++)
              {
                player[i].sendMessage(ChatColor.GOLD + "[" + Shops[index].getName() + "] " + ChatColor.GRAY + line);
              }
          }
        else
          {
            Player[] player = getOnlinePlayers();
            for (int i = 0; i < getServer().getOnlinePlayers().length; i++)
              {
                if ((!this.ps.containsKey(player[i].getName()))
                        || (!this.ps.getProperty(player[i].getName()).equalsIgnoreCase("on")))
                  {
                    continue;
                  }
                player[i].sendMessage(ChatColor.GOLD + "[" + Shops[index].getName() + "] " + ChatColor.GRAY + line);
              }
          }
      }

    public void announce(String line, String name)
      {
        // log.info("announce 3");
        if (this.sendToAll)
          {
            Player[] player = getOnlinePlayers();
            for (int i = 0; i < getServer().getOnlinePlayers().length; i++)
              {
                player[i].sendMessage(ChatColor.GOLD + "[" + name + "] " + ChatColor.GRAY + line);
              }
          }
        else
          {
            Player[] player = getOnlinePlayers();
            for (int i = 0; i < getServer().getOnlinePlayers().length; i++)
              {
                if ((!this.ps.containsKey(player[i].getName()))
                        || (!this.ps.getProperty(player[i].getName()).equalsIgnoreCase("on")))
                  {
                    continue;
                  }
                player[i].sendMessage(ChatColor.GOLD + "[" + name + "] " + ChatColor.GRAY + line);
              }
          }
      }

    @Override
    public void onEnable()
      {
        this.server = getServer();
        PluginDescriptionFile pdfFile = getDescription();
        log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " loading.");
        setupPermissions();
        setupIconomy();
        reload();
        log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!");
      }

    public void reload()
      {
        if (this.config.exists())
          {
            try
              {
                FileInputStream in = new FileInputStream(this.config);
                this.pr.load(in);
                log.info("[ShopAds] Config loaded!");
              }
            catch (IOException e)
              {
                log.info("[ShopAds] There was an error reading the config!");
              }
          }
        else
          {
            if (!this.dir.exists())
              {
                this.dir.mkdir();
              }

            makeConfig();
            FileInputStream in = null;

            try
              {
                in = new FileInputStream(this.config);
              }
            catch (FileNotFoundException ex)
              {
                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
              }
            try
              {
                this.pr.load(in);
              }
            catch (IOException ex)
              {
                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
              }

            log.info("[ShopAds] Config loaded!");
          }
        BukkitScheduler scheduler = getServer().getScheduler();
        Long interval = Long.valueOf(Long.valueOf(this.pr.getProperty("announceInterval")).longValue() * 25L);

        this.pluginState = true;
        this.random = Boolean.parseBoolean(this.pr.getProperty("random"));
        this.sendToAll = Boolean.parseBoolean(this.pr.getProperty("sendToAll"));
        scheduler.scheduleAsyncRepeatingTask(this, this.thread, interval.longValue(), interval.longValue());
        loadShops();
        log.info("[ShopAds] " + String.valueOf(Shops.length) + " advertisements were loaded");
      }

    public int getNumberOfShops()
      {
        int count = 0;
        for (int i = 0; i < listOfFiles.length; i++)
          {
            try
              {
                if (this.getNumberOfLines(listOfFiles[i]) > 0)
                  {
                    if ((this.getNumberOfLines(listOfFiles[i]) % 8) == 0)
                      {
                        int x = this.getNumberOfLines(listOfFiles[i]) / 8;
                        count = count + x;
                      }
                  }
              }
            catch (FileNotFoundException ex)
              {
              }
            catch (IOException ex)
              {
              }
          }
        return count;
      }

    public void loadShops()
      {
        //log.info("==========Loading Shops============");
        this.listOfFiles = this.userdir.listFiles();
        if ((this.listOfFiles != null) && (this.listOfFiles.length > 0))
          {
            this.Shops = new ShopAdsShop[this.getNumberOfShops()];
            int count = 0;
            for (int i = 0; i < this.listOfFiles.length; i++)
              {
                String[] temp = null;
                try
                  {
                    temp = new String[getNumberOfLines(this.listOfFiles[i])];
                    temp = getFileContents(this.listOfFiles[i]);

                  }
                catch (FileNotFoundException ex)
                  {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                  }
                catch (IOException ex)
                  {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                  }


                if (temp != null)
                  {
                    // log.info("temp  is not null");
                    int line = 0;

                    boolean endOfFile = false;
                    while (!endOfFile)
                      {

                        //  log.info (temp[line]);
                        if (temp[line].equalsIgnoreCase("<shop>"))
                          {
                            String[] output = new String[6];

                            for (int q = 1; q < 7; q++)
                              {
                                output[(q - 1)] = temp[(line + q)];
                              }

                            createShop(output, this.listOfFiles[i], count);
                            count++;
                            //   log.info("line: " + String.valueOf(line + 8));

                            //  log.info(String.valueOf(temp.length));
                            if (line + 8 >= temp.length)
                              {
                                //   log.info("Reached end of file");
                                endOfFile = true;
                              }
                            else
                              {
                                line = line + 8;



                              }
                          }

                      }








                  }
                else
                  {
                    //  log.info("temp is null");
                    listOfFiles[i].deleteOnExit();
                    listOfFiles[i].delete();
                  }

              }//End of files

          }
      }

    private void createShop(String[] temp, File f, int i)
      {
        temp = parseShop(temp);
        /** log.info("============Creating New Shop===========");
        log.info("Shop " + i);
        log.info(f.getName().substring(0, f.getName().indexOf(".")));
        log.info(temp[0]);
        log.info(temp[1]);
        log.info(temp[3]);
        log.info("============END=============");*/
        this.Shops[i] = new ShopAdsShop(f.getName().substring(0, f.getName().indexOf(".")), temp[0], Double.valueOf(Double.parseDouble(temp[1])), parseShopLocation(temp[2]), parseShopWorld(temp[2]), temp[3], Boolean.parseBoolean(temp[4]), Boolean.parseBoolean(temp[5]), f);
      }

    private String[] parseShop(String[] temp) //Removes the key and '='
      {
        temp[0] = temp[0].substring(temp[0].indexOf("=") + 1, temp[0].length());
        temp[1] = temp[1].substring(temp[1].indexOf("=") + 1, temp[1].length());
        temp[2] = temp[2].substring(temp[2].indexOf("=") + 1, temp[2].length());
        temp[3] = temp[3].substring(temp[3].indexOf("=") + 1, temp[3].length());
        temp[4] = temp[4].substring(temp[4].indexOf("=") + 1, temp[4].length());
        temp[5] = temp[5].substring(temp[5].indexOf("=") + 1, temp[5].length());
        return temp;
      }

    private String parseShopWorld(String loc)
      {
        return (loc.substring(loc.indexOf("_") + 1));
      }

    private void writeUsers()
      {
        if (this.user.exists())
          {
            try
              {
                FileOutputStream in = new FileOutputStream(this.user);
                this.ps.store(in, "");
              }
            catch (IOException e)
              {
              }
          }
        else
          {
            try
              {
                this.user.createNewFile();
              }
            catch (IOException ex)
              {
                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
              }
            try
              {
                FileOutputStream in = new FileOutputStream(this.user);
                this.ps.store(in, "");
              }
            catch (IOException e)
              {
              }
          }
        loadUsers();
      }

    public void loadUsers()
      {
        if (this.user.exists())
          {
            try
              {
                FileInputStream in = new FileInputStream(this.user);
                this.ps.load(in);
              }
            catch (IOException e)
              {
              }
          }
      }

    private double[] parseShopLocation(String temp)
      {
        double[] location = new double[5];
        location[0] = Double.parseDouble(temp.substring(0, temp.indexOf("/")));
        location[1] = Double.parseDouble(temp.substring(temp.indexOf("/") + 1, temp.lastIndexOf("/")));
        location[2] = Double.parseDouble(temp.substring(temp.lastIndexOf("/") + 1, temp.indexOf(",")));
        location[3] = Double.parseDouble(temp.substring(temp.indexOf(",") + 1, temp.lastIndexOf(",")));
        location[4] = Double.parseDouble(temp.substring(temp.lastIndexOf(",") + 1, temp.indexOf("_")));
        return location;
      }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
      {
        String[] action = args;
        if ((sender instanceof Player))
          {
            Player player = (Player) sender;

            if ((commandLabel.equalsIgnoreCase("ad")) || (commandLabel.equalsIgnoreCase("ads")))
              {
                if (action.length > 0)
                  {
                    if (action[0].equalsIgnoreCase("stats") || action[0].equalsIgnoreCase("stat"))
                      {
                        this.getMyShops(player);
                        return true;
                      }
                    if (action[0].equalsIgnoreCase("list"))
                      {
                        if (Shops != null)
                          {
                            if (Shops.length > 0)
                              {
                                for (int i = 0; i < Shops.length; i++)
                                  {
                                    if (Shops[i] != null)
                                      {
                                        player.sendMessage(ChatColor.GOLD + "[" + Shops[i].getName() + "] " + ChatColor.GRAY + Shops[i].getAd());
                                      }
                                  }
                                return true;
                              }
                          }
                        player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "There are no shops currently advertising");
                        return true;
                      }
                    if (action[0].equalsIgnoreCase("reload"))
                      {
                        if (this.hasPermission(player, "sa.admin"))
                          {
                            BukkitScheduler scheduler = getServer().getScheduler();
                            scheduler.cancelTasks(this);
                            this.reload();
                            player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "Config and Ads reloaded!");
                            return true;
                          }
                        else
                          {
                            player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.RED + "You do not have permission for that command");
                            return true;
                          }
                      }
                    if (action[0].equalsIgnoreCase("disable"))
                      {
                        if (this.hasPermission(player, "sa.admin"))
                          {

                            this.onDisable();
                            return true;
                          }
                        else
                          {
                            player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.RED + "You do not have permission for that command");
                            return true;
                          }
                      }
                    if (action[0].equalsIgnoreCase("on"))
                      {
                        if (!Boolean.parseBoolean(this.pr.getProperty("sendToAll")))
                          {
                            if ((this.ps.containsKey(player.getName()))
                                    && (this.ps.getProperty(player.getName()).equalsIgnoreCase("on")))
                              {
                                player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " You were already receive ads");
                                return true;
                              }

                            this.ps.setProperty(player.getName(), "on");
                            player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " You will now receive ads");
                            writeUsers();
                            log.info("[ShopAds] " + player.getName() + " turned on ads.");
                            return true;
                          }
                        player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " Server sends ads to everyone");
                        return true;
                      }

                    if (action[0].equalsIgnoreCase("off"))
                      {
                        if (!Boolean.parseBoolean(this.pr.getProperty("sendToAll")))
                          {
                            if ((!this.ps.containsKey(player.getName())) || (this.ps.getProperty(player.getName()).equalsIgnoreCase("off")))
                              {
                                player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " You weren't receiving ads");
                                return true;
                              }

                            this.ps.setProperty(player.getName(), "off");
                            player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " You will no longer recieve ads");
                            writeUsers();
                            log.info("[ShopAds] " + player.getName() + " turned off ads.");

                            return true;
                          }
                        player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " Server sends ads to everyone");
                        return true;
                      }

                    if (action[0].equalsIgnoreCase("rates"))
                      {
                        if (Integer.parseInt(this.pr.getProperty("cost")) > 1)
                          {
                            player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "Current rate is " + this.pr.getProperty("cost") + " " + (String) Constants.BankMajor.get(1) + " per hour");
                          }
                        else
                          {
                            player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "Current rate is " + this.pr.getProperty("cost") + " " + (String) Constants.BankMajor.get(0) + " per hour");
                          }
                        return true;
                      }

                    if (action[0].equalsIgnoreCase("create"))
                      {
                        String[] temp = new String[action.length - 1];
                        for (int i = 0; i < temp.length; i++)
                          {
                            temp[i] = action[(i + 1)];
                          }
                        action = new String[temp.length];
                        action = temp;
                        if (hasPermission(player, "sa.create"))
                          {
                            if (action.length > 2)
                              {
                                String playerName = player.getName();
                                Location loc = player.getLocation();
                                try
                                  {
                                    writeShops(playerName, action, player, loc);

                                  }
                                catch (FileNotFoundException ex)
                                  {
                                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                                  }

                                loadShops();
                              }

                            return true;
                          }

                        player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.RED + "You do not have permission for that command");
                        return true;
                      }

                    if ((action[0].equalsIgnoreCase("yes")) || (action[0].equalsIgnoreCase("y")))
                      {
                        player.sendMessage("Frodo: What are you saying yes too?");
                        return true;
                      }

                    if ((action[0].equalsIgnoreCase("no")) || (action[0].equalsIgnoreCase("n")))
                      {
                        player.sendMessage("Hitler: NINE NINE NINE NINE NINEEE!!!!");
                        return true;
                      }

                    if ((action[0].equalsIgnoreCase("delete")) || (action[0].equalsIgnoreCase("del")))
                      {
                        //log.info("Player wants to delete");
                        if (action.length == 2)
                          {
                            //  log.info("input correct");
                            if (this.shopExists(action[1]) != -1)
                              {
                                //   log.info("Shop exists");
                                this.deleteShop(action[1], player);


                                this.loadShops();
                              }
                          }
                        else
                          {
                            player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " You must enter a shop name");
                            return true;
                          }
                        return true;
                      }
                    player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.RED + "Unknown commmand ( " + ChatColor.YELLOW + action[0] + ChatColor.RED + " )");


                  }
              }

            if (commandLabel.equalsIgnoreCase("shops"))
              {
                String message = null;
                if (action.length == 1)
                  {
                    teleport(action[0], player);
                    return true;
                  }
                if (this.getShopsLength() > 0 && Shops != null && Shops.length > 0)
                  {
                    if (Shops[0] != null)
                      {
                        if (action.length == 0)
                          {
                            if ((this.Shops != null) && (this.Shops.length > 0))
                              {
                                if ((this.Shops[0] != null) && (!this.Shops[0].shopExpired()))
                                  {
                                    message = this.Shops[0].getName();
                                  }

                                for (int i = 1; i < this.Shops.length; i++)
                                  {
                                    if ((this.Shops[i] == null)
                                            || (this.Shops[i].shopExpired()))
                                      {
                                        continue;
                                      }
                                    message = message + ", " + this.Shops[i].getName();
                                  }

                                player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " The current shops available to teleport to are:");
                                player.sendMessage(ChatColor.GRAY + message);
                                return true;
                              }
                          }
                      }
                  }
                player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " There are no shops currently advertising");

                return true;
              }
            if (!Boolean.parseBoolean(pr.getProperty("sendToAll")))
              {
                player.sendMessage(ChatColor.GOLD + "[ShopAds]");
                if (this.hasPermission(player, "sa.create"))
                  {
                    player.sendMessage(ChatColor.GRAY + "/ad create [shopname] [number of hrs] [message]");
                    player.sendMessage(ChatColor.GRAY + "/ad delete [shopname] - Stop your currently running ad");
                    player.sendMessage(ChatColor.GRAY + "/ad stats - Display information about all your current ads");
                  }

                player.sendMessage(ChatColor.GRAY + "/ad rates - Returns the current daily rate");
                player.sendMessage(ChatColor.GRAY + "/ad list - Lists all the current ads");
                player.sendMessage(ChatColor.GRAY + "/ad on - Start receiving ads");
                player.sendMessage(ChatColor.GRAY + "/ad off - Stop receiving ads");
                if (this.hasPermission(player, "sa.admin"))
                  {
                    player.sendMessage(ChatColor.GRAY + "/ad reload - Reload the config and ads");
                    player.sendMessage(ChatColor.GRAY + "/ad disable - Disables the plugin instantly");
                  }
                player.sendMessage(ChatColor.GRAY + "/shops - List shops available to tp");
                return true;
              }
            else
              {
                player.sendMessage(ChatColor.GOLD + "[ShopAds]");
                if (this.hasPermission(player, "sa.create"))
                  {
                    player.sendMessage(ChatColor.GRAY + "/ad create [shopname] [number of hrs] [message]");
                    player.sendMessage(ChatColor.GRAY + "/ad delete [shopname] - Stop your currently running ad");
                    player.sendMessage(ChatColor.GRAY + "/ad stats - Display information about all your current ads");
                  }
                player.sendMessage(ChatColor.GRAY + "/ad rates - Returns the current daily rate");
                player.sendMessage(ChatColor.GRAY + "/ad list - Lists all the current ads");
                if (this.hasPermission(player, "sa.admin"))
                  {
                    player.sendMessage(ChatColor.GRAY + "/ad reload - Reload the config and ads");
                    player.sendMessage(ChatColor.GRAY + "/ad disable - Disables the plugin instantly");
                  }
                player.sendMessage(ChatColor.GRAY + "/shops - List shops available to tp");
                return true;
              }
          }
        log.info("[ShopAds] Only players currently on the server can use this plugins functions!");

        return false;
      }

    public boolean deleteShop(String name, Player p)
      {
        if (name != null)
          {
            if (Shops != null)
              {
                if (Shops.length > 0)
                  {

                    for (int i = 0; i < Shops.length; i++)
                      {
                        if (Shops[i].getName().toLowerCase().startsWith(name.toLowerCase()))
                          {
                            if (Shops[i].getShopOwner().equalsIgnoreCase(p.getName()))
                              {
                                Shops[i].setShopExpired(true);
                                try
                                  {
                                    this.timeUpdater(i);
                                  }
                                catch (FileNotFoundException ex)
                                  {
                                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                                  }
                                catch (IOException ex)
                                  {
                                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                                  }
                                p.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + (Shops[i].getName()) + " has been deleted");
                                return true;
                              }
                          }
                      }
                  }
              }
          }
        else
          {
            p.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "You must enter a name!");
          }
        p.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "No shop by that name found!");
        return false;
      }

    public boolean hasPermission(Player player, String node)
      {
        return permissionHandler.has(player, node);
      }

    private boolean chargePlayer(Player player, int hours)
      {
        if (iConomy.Accounts.exists(player.getName()))
          {
            Holdings balance = iConomy.getAccount(player.getName()).getHoldings();
            if (balance.hasOver(hours * Double.parseDouble(this.pr.getProperty("cost"))))
              {
                balance.subtract(hours * Double.parseDouble(this.pr.getProperty("cost")));
                player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "You were charged " + iConomy.format(hours * Double.parseDouble(this.pr.getProperty("cost"))));
                return true;
              }
            player.sendMessage(ChatColor.RED + "[ShopAds] You do not have enough money to make an ad that");
            player.sendMessage(ChatColor.RED + "long.");
            return false;
          }

        player.sendMessage(ChatColor.RED + "[ShopAds] You do not have an account.");

        return false;
      }

    private void makeConfig()
      {
        try
          {
            this.config.createNewFile();
            try
              {
                PrintWriter out = new PrintWriter(new FileWriter("plugins/ShopAds/config.yml"));
                out.println("#'maxShops' - The maximum number of ads allowed to each player");
                out.println("#'announceInterval' - The time in seconds between ad announcements [number(secs)]");
                out.println("#'random' - Should the ads be in a random order [true/false]");
                out.println("#'cost' - The cost per hour of advertising [number(currency)]");
                out.println("#'maxAdRunTime' - The longest time you want an ad to run for [number(hours)]");
                out.println("#'sendToAll' - Whether to send to all players, disregarding their choice [true/false]");
                out.println("maxShops=1");
                out.println("announceInterval=10");
                out.println("cost=20");
                out.println("maxAdRunTime=24");
                out.println("random=false");
                out.println("sendToAll=false");
                out.close();
                log.info("[ShopAds] No config found, created default config");
              }
            catch (IOException e)
              {
                log.info("[ShopAds] Error writing to config");
              }
          }
        catch (IOException ioe)
          {
            log.info("[ShopAds] Error creating config file");
          }
      }

    private void getMyShops(Player p)
      {
        int count = 0;
        DecimalFormat df = new DecimalFormat("#.##");
        Calendar calNow = Calendar.getInstance();
        Date dateNow = calNow.getTime();
        Long timeNow = Long.valueOf(dateNow.getTime());
        String message = null;
        if (Shops != null)
          {
            if (Shops.length > 0)
              {
                for (int i = 0; i < this.Shops.length; i++)
                  {
                    if (Shops[i] != null)
                      {
                        if (Shops[i].getShopOwner() != null)
                          {
                            if (Shops[i].getShopOwner().equalsIgnoreCase(p.getName()))
                              {
                                count++;
                                p.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + Shops[i].getName() + " : " + df.format((Shops[i].getTimeToEnd() - timeNow) / 3600000) + " hours left");
                              }
                          }
                      }
                  }
                if (count == 0)
                  {
                    p.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.RED + "It appears you have no ads");
                  }
                return;
              }
          }
        p.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.RED + "It appears you have no ads");
        return;
      }

    private boolean isValidNumber(String T, Player p)
      {
        int test;
        try
          {
            test = Integer.parseInt(T);
          }
        catch (Exception e)
          {
            p.sendMessage(ChatColor.RED + "[ShopAds] You must enter a number for time");
            test = 0;
            return false;
          }
        if (test <= 0)
          {
            p.sendMessage(ChatColor.RED + "[ShopAds] You must enter a number greater than zero");
            return false;
          }
        if (test > Integer.parseInt(this.pr.getProperty("maxAdRunTime")))
          {
            p.sendMessage(ChatColor.RED + "[ShopAds] You must enter a number " + this.pr.getProperty("maxAdRunTime") + " or under");
            return false;
          }
        return true;
      }

    private void writeShops(String playerName, String[] action, Player player, Location loc)
            throws FileNotFoundException
      {
        if (isValidNumber(action[1], player))
          {
            if (!this.userdir.exists())
              {
                this.userdir.mkdir();
              }
            String message = null;
            Calendar calNow = Calendar.getInstance();
            Date dateNow = calNow.getTime();
            File file = new File("plugins/ShopAds/players/" + player.getName() + ".yml");
            if (!file.exists())
              {
                try
                  {
                    file.createNewFile();
                  }
                catch (IOException ex)
                  {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                  }
              }

            String[] temporary = null;
            String[] output = null;
            try
              {
                temporary = getFileContents(file);
                if (getFileContents(file) != null)
                  {
                    output = new String[getNumberOfLines(file) + 8];
                  }
                else
                  {
                    output = new String[8];
                  }
              }
            catch (IOException ex)
              {
              }
            if (temporary != null)
              {
                if (temporary.length > 0)
                  {
                    int i = 0;
                    for (i = 0; i < temporary.length; i++)
                      {
                        output[i] = temporary[i];
                      }
                    Long ends = Long.valueOf(dateNow.getTime() + 3600000L * Long.parseLong(action[1]));
                    output[i] = "<shop>";

                    output[(i + 1)] = ("Name=" + action[0]);
                    output[(i + 2)] = ("Ends=" + String.valueOf(ends));
                    output[(i + 3)] = ("Location=" + loc.getX() + "/" + loc.getY() + "/" + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw() + "_" + loc.getWorld().getName());
                    message = action[2];

                    for (int z = 3; z < action.length; z++)
                      {
                        message = message + " " + action[z];
                      }
                    output[(i + 4)] = ("Message=" + message);
                    output[(i + 5)] = "Advertising=true";
                    output[(i + 6)] = "Expired=false";
                    output[(i + 7)] = "</shop>";
                    if (this.getUsersShops(player.getName()) < Integer.parseInt(pr.getProperty("maxShops")))
                      {
                        if (writeShop(output, file))
                          {
                            player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "Advertisement has been created for " + action[1] + " hours at your current location.");
                            this.announce(message, action[0]);
                          }
                        else
                          {
                            player.sendMessage(ChatColor.RED + "[ShopAds] You shop encountered an error in creation");
                          }
                      }
                    else
                      {
                        player.sendMessage(ChatColor.RED + "[ShopAds] You have too many ads (" + this.getUsersShops(player.getName()) + ")");
                        return;
                      }
                  }
              }
            else
              {
                int i = 0;
                output[i] = "<shop>";
                Long ends = Long.valueOf(dateNow.getTime() + 3600000L * Long.parseLong(action[1]));
                output[(i + 1)] = ("Name=" + action[0]);
                output[(i + 2)] = ("Ends=" + String.valueOf(ends));
                output[(i + 3)] = ("Location=" + loc.getX() + "/" + loc.getY() + "/" + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw() + "_" + loc.getWorld().getName());
                message = action[2];
                if (action.length > 3)
                  {
                    for (int z = 3; z < action.length; z++)
                      {
                        message = message + " " + action[z];
                      }
                  }
                output[(i + 4)] = ("Message=" + message);
                output[(i + 5)] = "Advertising=true";
                output[(i + 6)] = "Expired=false";
                output[(i + 7)] = "</shop>";
                if (this.getUsersShops(player.getName()) < Integer.parseInt(pr.getProperty("maxShops")))
                  {
                    if (chargePlayer(player, Integer.parseInt(action[1])))
                      {
                        if (writeShop(output, file))
                          {
                            player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "Advertisement has been created for " + action[1] + " hours");
                            player.sendMessage(ChatColor.GRAY + "          at your current location.");
                            this.announce(message, action[0]);
                          }
                        else
                          {
                            player.sendMessage(ChatColor.RED + "[ShopAds] You shop encountered an error in creation");
                          }
                      }
                  }
                else
                  {
                    player.sendMessage(ChatColor.RED + "[ShopAds] You have too many ads (" + this.getUsersShops(player.getName()) + ")");
                    return;
                  }

              }

          }

        this.loadShops();
      }

    private boolean writeShop(String[] output, File file)
      {
        FileWriter fw = null;
        try
          {
            fw = new FileWriter(file);
          }
        catch (IOException ex)
          {
            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
          }
        if (fw != null)
          {
            PrintWriter out = new PrintWriter(fw);

            for (int i = 0; i < output.length; i++)
              {
                out.println(output[i]);
              }
            out.close();
            return true;
          }
        else
          {
            return false;
          }
      }

    private int getUsersShops(String name)
      {
        int count = 0;
        if (Shops != null)
          {
            if (Shops.length > 0)
              {
                for (int i = 0; i < Shops.length; i++)
                  {
                    if (Shops[i].getShopOwner().equalsIgnoreCase(name))
                      {
                        count++;
                      }
                  }

              }
          }
        return count;
      }

    private String[] getFileContents(File f) throws FileNotFoundException, IOException
      {
        FileReader fr = null;
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(f));
        int lines = getNumberOfLines(f);
        if (lines > 0)
          {
            String[] file = new String[lines];
            for (int i = 0; i < getNumberOfLines(f); i++)
              {
                file[i] = br.readLine();
              }
            return file;
          }
        return null;
      }

    private int getNumberOfLines(File f)
            throws FileNotFoundException, IOException
      {
        String temp = null;
        int count = 0;
        FileReader fr = null;
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(f));
        temp = br.readLine();
        while (temp != null)
          {
            count++;
            temp = br.readLine();
          }

        return count;
      }

    private void setupPermissions()
      {
        Plugin permissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");

        if (permissionHandler == null)
          {
            if (permissionsPlugin != null)
              {
                log.info("[ShopAds] Permissions Plugin Found");
                permissionHandler = ((Permissions) permissionsPlugin).getHandler();
              }
            else
              {
                log.info("[ShopAds] Permission system not found. Disabling plugin");
                getServer().getPluginManager().disablePlugin(this);
              }
          }
      }

    public void setupIconomy()
      {
        Plugin test = getServer().getPluginManager().getPlugin("iConomy");
        if (this.iConomy == null)
          {
            if (test != null)
              {
                log.info("[ShopAds] Successfully hooked into iConomy");
                this.iConomy = ((iConomy) test);
              }
            else
              {
                log.info("[ShopAds] iConomy NOT FOUND, disabling plugin");
                getServer().getPluginManager().disablePlugin(this);
              }
          }
      }

    public boolean timeUpdater(int index)
            throws FileNotFoundException, IOException
      {
        Calendar calNow = Calendar.getInstance();
        Date dateNow = calNow.getTime();

        Long timeNow = Long.valueOf(dateNow.getTime());
        if (Shops != null)
          {
            //log.info (String.valueOf(index));
            if ((this.Shops.length > 0) && (this.Shops[index] != null))
              {
                if ((this.Shops[index].getTimeToEnd() < timeNow.longValue()) || (this.Shops[index].shopExpired()))
                  {
                    log.info("[ShopAds] " + this.Shops[index].getName() + " has expired");

                    String[] output = new String[getFileContents(this.Shops[index].getShopFile()).length - 8];
                    if (output.length == 0)
                      {
                        writeShop(output, this.Shops[index].getShopFile());
                        return true;
                      }
                    String[] temp = getFileContents(this.Shops[index].getShopFile());
                    int i = 0;
                    boolean match = false;
                    int line = -1;
                    while (!match)
                      {
                        if (i < temp.length)
                          {
                            if ((!temp[i].equalsIgnoreCase("<shop>")) && (!temp[i].equalsIgnoreCase("</shop>")))
                              {
                                String parse = temp[i].substring(temp[i].indexOf("=") + 1, temp[i].length());
                                if (parse.equalsIgnoreCase(this.Shops[index].getName()))
                                  {
                                    line = i - 1;
                                    match = true;
                                  }
                              }
                          }
                        else
                          {
                            match = true;
                          }

                        i++;
                      }

                    if (line != -1)
                      {
                        int z = 0;
                        for (z = 0; z < line; z++)
                          {
                            output[z] = temp[z];
                          }
                        for (int y = line + 8; y < temp.length; y++)
                          {
                            output[z] = temp[y];
                            z++;
                          }

                        writeShop(output, this.Shops[index].getShopFile());

                      }
                    else
                      {
                        return true;
                      }
                  }
              }
          }
        return false;
      }

    public boolean pluginState()
      {
        return this.pluginState;
      }

    public Player[] getOnlinePlayers()
      {
        return getServer().getOnlinePlayers();
      }

    public int getShopsLength()
      {
        if (this.Shops != null)
          {
            if (this.Shops.length > 0)
              {
                return this.Shops.length;
              }
            return 0;
          }

        return 0;
      }

    private void teleport(String name, Player player)
      {
        if (shopExists(name) != -1)
          {
            teleportToShop(shopExists(name), player);

            return;
          }
        player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " Could not find a shop by that name");
      }

    private void teleportToShop(int index, Player player)
      {
        List<World> activeWorlds = server.getWorlds();
        for (int i = 0; i < activeWorlds.size(); i++)
          {
            //  log.info (this.Shops[index].getWorld());
            // log.info (activeWorlds.get(i).getName());
            if (activeWorlds.get(i).getName().equalsIgnoreCase(this.Shops[index].getWorld()))
              {
                Location loc = new Location(activeWorlds.get(i), this.Shops[index].getLocation(0), this.Shops[index].getLocation(1), this.Shops[index].getLocation(2), Float.parseFloat(String.valueOf(this.Shops[index].getLocation(4))), Float.parseFloat(String.valueOf(this.Shops[index].getLocation(3))));
                player.teleport(loc);
                player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + " You have been teleported to " + this.Shops[index].getName());
                for (int x = 0; x < this.getOnlinePlayers().length; x++)
                  {
                    if (this.getOnlinePlayers()[x].getName().equalsIgnoreCase(this.Shops[index].getShopOwner()))
                      {
                        this.getOnlinePlayers()[x].sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + player.getName() + " just teleported to your shop!");
                      }
                  }
                return;

              }

          }
        player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.RED + " There was a problem finding the correct world, notify admin!");



      }

    private int shopExists(String name)
      {
        if (this.getShopsLength() != 0)
          {
            if (this.Shops[0] != null)
              {

                for (int i = 0; i < this.Shops.length; i++)
                  {

                    //log.info (Shops[i].getName());
                    if (this.Shops[i].getName().toLowerCase().startsWith(name.toLowerCase()))
                      {

                        //log.info (String.valueOf(i));
                        return i;
                      }
                  }
              }
          }
        return -1;
      }
  }
