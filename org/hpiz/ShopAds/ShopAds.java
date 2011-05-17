package org.hpiz.ShopAds;

/**
 *
 * @author Hpiz
 */
import com.iConomy.*;
import com.iConomy.system.Account;
import com.iConomy.system.Holdings;

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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import java.util.Calendar;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * ShopAds for Bukkit
 *
 * @author Hpiz
 */
public class ShopAds extends org.bukkit.plugin.java.JavaPlugin {

    private timerThread thread; //Thread that counts the interval
    public iConomy iConomy = null; // iConomy object
    public static PermissionHandler permissionHandler; // permissions object
    public static final Logger log = Logger.getLogger("Minecraft"); // logging to console
    public Server server; //Server object
    private File config = new File("plugins/ShopAds/config.yml"); // Config File
    private File dir = new File("plugins/ShopAds/"); //Plugin Directory
    private File userdir = new File("plugins/ShopAds/players/"); //Advertisements Directory
    private Properties pr = new Properties(); //Config Properties
    private Properties ps = new Properties(); //User Properties
    private Properties[] pa; //Ads
    public boolean pluginState = false; //Plugin State (true:false)
    private File[] listOfFiles = userdir.listFiles(); //List of files in the advertisements directory
    public Player[] onlinePlayers;
    public boolean random;
    private boolean sendToAll;
    private ChatColor color;
    private ShopAdsShop[] Shops;
    private File user = new File("plugins/ShopAds/user.dat");

    public ShopAds() {
        super();
        thread = new timerThread(this);

    }

    public void onDisable() {
        pluginState=false;
    }

    public void announce(int index) {
        /**
        log.info(String.valueOf(index));
        log.info(String.valueOf(Shops.length));
        log.info(Shops[index].getName());              
         */
        if (Shops[index] != null) {
            if (!Shops[index].getName().equalsIgnoreCase("expired")) {
                announce(Shops[index].getAd(), Shops[index].getName());
            }
        }
        return;
    }

    public void announce(String line, String shopName) {







        if (sendToAll) {
            getServer().broadcastMessage(line);
        } else {
            Player[] player = this.getOnlinePlayers();
            for (int i = 0; i < getServer().getOnlinePlayers().length; i++) {

                if (ps.containsKey(player[i].getName())) {
                    if (ps.getProperty(player[i].getName()).equalsIgnoreCase("on")) {
                        player[i].sendMessage(color.GOLD + "[" + shopName + "] " + color.GRAY + line);
                    }
                }
            }
        }
    }   

    @Override
    public void onEnable() {
        server = getServer();
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " loading.");
        setupPermissions();
        setupIconomy();
        this.reload();
        BukkitScheduler scheduler = getServer().getScheduler();
        Long interval = Long.valueOf(pr.getProperty("interval"));
        

        log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!");
        pluginState = true;
        random = false;
        sendToAll = false;
        scheduler.scheduleAsyncRepeatingTask(this, thread, interval, interval);
    }

    public void reload() {

        if (config.exists()) {
            try {
                FileInputStream in = new FileInputStream(config);
                pr.load(in);
                log.info("[ShopAds] Config loaded!");
            } catch (IOException e) {
                log.info("[ShopAds] There was an error reading the config!");
            }
        } else {
            if (!dir.exists()) {
                dir.mkdir();
            }
            this.makeConfig();
            FileInputStream in = null;
            try {
                in = new FileInputStream(config);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                pr.load(in);
            } catch (IOException ex) {
                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
            }
            log.info("[ShopAds] Config loaded!");
        }

        this.loadShops();
        log.info("[ShopAds] Advertisements have been loaded!");

    }

    public void loadShops() {
        //log.info("Loading Shops");
               
        int z = this.getNumberOfShopFiles();

       //  log.info(z + " shops found");

        Shops = new ShopAdsShop[z];
        pa = new Properties[z];

        z = 0;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (z < this.getNumberOfShopFiles()) {

                String fileName;
                if (listOfFiles[i].isFile()) {

                    fileName = listOfFiles[i].getName();
                 //   log.info(fileName + " is a file");
                    if (fileName.endsWith(".yml") || fileName.endsWith(".YML")) {
                   //     log.info("The file ends with .yml");
                        
            try {
                FileInputStream in = new FileInputStream(listOfFiles[i]);
                pa[z] = new Properties();
                pa[z].load(in);
                Shops[z] = new ShopAdsShop(pa[z].getProperty("Name"),pa[z].getProperty("Message"), this.parseShopLocation(pa[z].getProperty("Location")) , Double.parseDouble(pa[z].getProperty("Ends")), listOfFiles[i]);
                z++;
              
            } catch (IOException e) {
 
            }
        
                      
                                
                            

                    }
                }
            }
        }
    }

    public void writeUsers() {
        if (user.exists()) {
            try {
                FileOutputStream in = new FileOutputStream(user);
                ps.store(in, "");



            } catch (IOException e) {
            }
        }
        this.loadUsers();
    }

    public void loadUsers() {
        if (user.exists()) {
            try {
                FileInputStream in = new FileInputStream(user);
                ps.load(in);


            } catch (IOException e) {
            }
        }


    }

    public double[] parseShopLocation(String temp) {
        double[] location = new double[5];
        location[0] = Double.parseDouble(temp.substring(0, temp.indexOf("/")));
        location[1] = Double.parseDouble(temp.substring(temp.indexOf("/") + 1, temp.lastIndexOf("/")));
        location[2] = Double.parseDouble(temp.substring(temp.lastIndexOf("/") + 1, temp.indexOf(",")));
        location[3] = Double.parseDouble(temp.substring(temp.indexOf(",") + 1, temp.lastIndexOf(",")));
        location[4] = Double.parseDouble(temp.substring(temp.lastIndexOf(",") + 1, temp.length()));
        return location;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        String[] action = args;
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (commandLabel.equalsIgnoreCase("ad") || commandLabel.equalsIgnoreCase("ads")) {
                if (action.length == 0 || action[0].equalsIgnoreCase("?")) {
                    player.sendMessage(ChatColor.GOLD + "[ShopAds]");
                    player.sendMessage(ChatColor.GRAY + "/ad [shopname] [number of cycles] [message] - Creates an advertisement for the desired shop and 6hr cycles");
                    player.sendMessage(ChatColor.GRAY + "/ad rates - Returns the current daily rate");
                    player.sendMessage(ChatColor.GRAY + "/ad on - Start receiving ads");
                    player.sendMessage(ChatColor.GRAY + "/ad off - Stop receiving ads");
                    return true;
                }
                if (action[0].equalsIgnoreCase("on")) {
                    if(ps.containsKey(player.getName())){
                    if(ps.getProperty(player.getName()).equalsIgnoreCase("on")){
                        player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + "You were already receive ads");
                        return true;
                    }
                    }
                    
                    ps.setProperty(player.getName(), "on");
                    player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + "You will now receive ads");
                    this.writeUsers();
                    log.info("[ShopAds] " + player.getName() + " turned on ads.");
                    
                }
                if (action[0].equalsIgnoreCase("off")) {
                    if(!ps.containsKey(player.getName())||ps.getProperty(player.getName()).equalsIgnoreCase("off")){
                        player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + "You weren't receiving ads");
                        return true;
                    }else{
                    ps.setProperty(player.getName(), "off");
                    player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + "You will no longer recieve ads");
                    this.writeUsers();
                    log.info("[ShopAds] " + player.getName() + " turned off ads.");
                }
                }
                if (action[0].equalsIgnoreCase("rates")) {
                    if (Integer.parseInt(pr.getProperty("cost")) > 1) {
                        player.sendMessage(ChatColor.GRAY + "Current rate is " + pr.getProperty("cost") + " " + com.iConomy.util.Constants.BankMajor.get(1) + " per hour");
                    } else {
                        player.sendMessage(ChatColor.GRAY + "Current rate is " + pr.getProperty("cost") + " " + com.iConomy.util.Constants.BankMajor.get(0) + " per hour");
                    }
                    return true;
                }

                if (action.length >= 3) {
                    if (hasPermission(player, "sa.create")) {
                        chargePlayer(player, Integer.parseInt(action[1]));
                        String playerName = player.getName();
                        Location loc = player.getLocation();
                        try {
                            writeShops(playerName, action, player, loc);
                            log.info("[ShopAds] " + player.getName() + " made a shop ad.");
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        loadShops();

                        return true;
                    } else {

                        player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.RED + "You do not have permission for that command");
                        return true;

                    }
                }


                return true;
            }
            if (commandLabel.equalsIgnoreCase("shop")) {
                String message = null;
                if (action.length == 1) {
                    teleport(action[0], player);
                } else {
                    if (Shops.length > 0) {
                        if (!Shops[0].getName().equalsIgnoreCase("expired")) {
                            message = Shops[0].getName();


                            for (int i = 1; i < Shops.length; i++) {
                                message = (message + ", " + Shops[i].getAd());
                            }
                        }
                        player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "The current shops available to teleport to are:");
                        player.sendMessage(color.GRAY + message);
                    } else {
                        player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "The are no shops currently advertising");
                    }
                }

            }
        } else {
            log.info("[ShopAds] Only players currently on the server can use this plugins functions!");

        }
        return false;
    }

    public boolean hasPermission(Player player, String node) {
        return permissionHandler.has(player, node);
    }

    private boolean hasAccount(String name) {
        return (iConomy.Accounts.exists(name));
    }

    private void chargePlayer(Player player, int hours) {
        if (hasAccount(player.getName())) {
            Holdings balance = iConomy.getAccount(player.getName()).getHoldings();
            if (balance.hasOver(hours * Double.parseDouble(pr.getProperty("cost")))) {
                balance.subtract(hours * Double.parseDouble(pr.getProperty("cost")));
                player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "You were charged " + iConomy.format(hours * Double.parseDouble(pr.getProperty("cost"))));
            }
            return;
        } else {
            player.sendMessage("[ShopAds] You do not have an account.");

        }
        return;
    }

    private void makeConfig() {

        try {
            config.createNewFile();
            try {
                PrintWriter out = new PrintWriter(new FileWriter("plugins/ShopAds/config.yml"));
                out.println("maxshops=1");
                out.println("interval=120");
                out.println("cost=120");
                out.close();
                log.info("[ShopAds] No config found, created default config");
            } catch (IOException e) {
                log.info("[ShopAds] Error writing to config");
            }
        } catch (IOException ioe) {
            log.info("[ShopAds] Error creating config file");
        }
    }

    private boolean isValidNumber(String T, Player p) {

        int test;
        try {
            test = Integer.parseInt(T);
        } catch (Exception e) {
            p.sendMessage("[ShopAds] You must enter a number from 1 to 36 for the hours value");
            test = 0;
        }
        if (test <= 0) {
            return false;
        } else {
            if (test >= 36) {
                return false;
            } else {
                return true;
            }
        }
    }

    public void writeShops(String playerName, String[] action, Player player, Location loc) throws FileNotFoundException {
        if (this.isValidNumber(action[1], player)) {
            if (!userdir.exists()) {

                userdir.mkdir();

            }
            String message = null;
            Calendar calNow = Calendar.getInstance();
            Date dateNow = calNow.getTime();
            File file = new File("plugins/ShopAds/players/" + player.getName() + ".yml");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            PrintWriter out = null;
            try {
                out = new PrintWriter(new FileWriter(file));
               // log.info("Writer initialized");
            } catch (IOException ex) {
                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
            }
            out.println("Name=" + action[0]);
          //  log.info("Printed: " + ("Name=" + action[0]));
            out.println("Ends=" + (String.valueOf(dateNow.getTime()) + ((3600000.0) * (Long.parseLong(action[1])))));
            out.println("Location=" + (loc.getX() + "/" + loc.getY() + "/" + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw()));
            message = action[2];
            for (int i = 3; i < action.length; i++) {
                message = message + " " + action[i];
            }
            out.print("Message=" + message);
            out.close();

            player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "Advertisement has been created for " + action[1] + " hours.");
        } else {
            player.sendMessage(ChatColor.RED + "[ShopAds] You did not enter a valid number for cycles.");
        }
    }

    private void setupPermissions() {
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

        if (this.permissionHandler == null) {
            if (permissionsPlugin != null) {
                log.info("[ShopAds] Permissions Plugin Found");
                this.permissionHandler = ((Permissions) permissionsPlugin).getHandler();
            } else {
                log.info("[ShopAds] Permission system not found. Disabling plugin");
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    public void setupIconomy() {

        Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");
        if (this.iConomy == null) {
            if (test != null) {
                log.info("[ShopAds] Successfully hooked into iConomy");
                this.iConomy = (iConomy) test;

            } else {
                log.info("[ShopAds] iConomy NOT FOUND, disabling plugin");
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    public void timeUpdater(int index) throws FileNotFoundException, IOException {

        Calendar calNow = Calendar.getInstance();
        Date dateNow = calNow.getTime();
        Long timeNow;
        timeNow = dateNow.getTime();
        if (Shops.length > 0) {

            if ((Shops[index].getTimeToEnd() - (timeNow / 60000)) < 0.0) {


                Shops[index].setName("expired");
                log.info("[ShopAds] " + Shops[index].getShopFile().getName() + " has expired");
                PrintWriter out = new PrintWriter(Shops[index].getShopFile());
                out.println("expired");
                out.close();



            }
        }
        return;
    }

    public boolean pluginState() {
        if (pluginState) {
            return true;
        } else {
            return false;
        }



    }

    public Player[] getOnlinePlayers() {
        return getServer().getOnlinePlayers();
    }

    public int getShopsLength() {
        return Shops.length;
    }

    private void teleport(String name, Player player) {
        if (shopExists(name) != -1) {
            teleportToShop(shopExists(name), player);
        }
    }

    public void teleportToShop(int index, Player player) {
        float pitch = 0;

        Location loc = new Location(player.getWorld(), Shops[index].getLocation(0), Shops[index].getLocation(1), Shops[index].getLocation(2), Float.parseFloat(String.valueOf(Shops[index].getLocation(4))), Float.parseFloat(String.valueOf(Shops[index].getLocation(3))));
        player.teleport(loc);
        player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "You have been teleported to " + Shops[index].getName());


    }

    private int shopExists(String name) {

        for (int i = 0; i < Shops.length; i++) {
            if (Shops[i].getName().toLowerCase().startsWith(name.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    public int getNumberOfShopFiles() {
        int z = 0;
       // log.info(String.valueOf(listOfFiles.length) + " files are in the players dir");
        for (int i = 0; i < listOfFiles.length; i++) {
            String fileName;
            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();
                if (fileName.endsWith(".yml") || fileName.endsWith(".YML")) {
                    z++;
                }
            }
        }
        return z;
    }
}

