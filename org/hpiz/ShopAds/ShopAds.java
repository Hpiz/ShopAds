package org.hpiz.ShopAds;

/**
 *
 * @author Hpiz
 */
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.iConomy.*;
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
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import java.util.Properties;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import java.util.Calendar;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * ShopAds for Bukkit
 *
 * @author Hpiz
 */
public class ShopAds extends org.bukkit.plugin.java.JavaPlugin {

    private timerThread thread; //Thread that counts the interval
    public iConomy iConomy = null; // iConomy object
    public Essentials essentials; // iConomy object
    public static PermissionHandler permissionHandler; // permissions object
    public static final Logger log = Logger.getLogger("Minecraft"); // logging to console
    public Server server; //Server object
    private File config = new File("plugins/ShopAds/config.yml"); // Config File
    private File dir = new File("plugins/ShopAds/"); //Plugin Directory
    private File userdir = new File("plugins/ShopAds/players/"); //Advertisements Directory
    private Properties pr = new Properties(); //Config Properties
    private Properties ps = new Properties(); //User Properties
    private Properties pa; //Ad
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
        pluginState = false;
    }

    public void announce(int index) {
        /**
        log.info(String.valueOf(index));
        log.info(String.valueOf(Shops.length));
        log.info(Shops[index].getName());
         */
        if (Shops[index] != null) {
            if (Shops[index].shopAdvertising()) {
                if (!Shops[index].shopExpired()){
                if (Shops[index].getAd() != null) {
                    announce(Shops[index].getAd(), Shops[index].getName());
                }
            } else {
                thread.runNextItem(index);
            }
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
        Long interval = (Long.valueOf(pr.getProperty("announceInterval")) * 25);



        log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!");
        pluginState = true;
        random = Boolean.parseBoolean(pr.getProperty("random"));
        sendToAll = Boolean.parseBoolean(pr.getProperty("sendToAll"));
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
        if (listOfFiles != null) {
            if (listOfFiles.length > 0) {
                Shops = new ShopAdsShop[listOfFiles.length];
                for (int i = 0; i < listOfFiles.length; i++) {
                    try {
                        String[] temp;
                        String[] output;
                        try {
                            temp = new String[this.getNumberOfLines(listOfFiles[i])];
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        temp = this.getFileContents(listOfFiles[i]);
                        if (temp != null) {
                            int line = 0;

                            boolean endOfFile = false;
                            while (!endOfFile) {
                                boolean shopBegin = false;
                                while (!shopBegin) {
                                    while (!endOfFile) {                                        //Just gets the line number of the shop starting

                                        if (temp[line].equalsIgnoreCase("<shop>")) {
                                            shopBegin = true;
                                            endOfFile = true;
                                        }
                                        if (!endOfFile) {
                                            line++;

                                            if (line > listOfFiles.length) {
                                                endOfFile = true;
                                            }
                                        }
                                    }
                                    if (shopBegin) {

                                        output = new String[6];

                                        for (int q = 1; q < 7; q++) {
                                            //log.info("Putting { " + temp[line + q] + " } into the output");
                                            output[q - 1] = temp[line + q];

                                        }
                                        createShop(output, listOfFiles[i], i);
                                    }
                                }

                            }
                        }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                    }




                }
            }
        }
    }

    public void createShop(String[] temp, File f, int i) {
        /** DEBUG
        log.info("Writing Shop number: " + i + " in the array that has a length of" + String.valueOf(Shops.length));
        log.info(f.getName().substring(0, f.getName().indexOf(".")));
        log.info(temp[0]);
        log.info(temp[1]);
        log.info(temp[2]);
        log.info(temp[3]);
        log.info(temp[4]);
        log.info(temp[5]);
        */
        temp = parseShop(temp);
        /** DEBUG
        log.info( temp[0]);
        log.info(temp[1]);
        log.info(temp[2]);
        log.info(temp[3]);
        
        log.info(temp[4]);
        
        log.info(temp[5]);
         */
        Shops[i] = new ShopAdsShop((f.getName().substring(0, f.getName().indexOf("."))), temp[0], Double.parseDouble(temp[1]), this.parseShopLocation(temp[2]), temp[3], Boolean.parseBoolean(temp[4]), Boolean.parseBoolean(temp[5]), f);
    }

    public String[] parseShop(String[] temp) {
        temp[0] = temp[0].substring(temp[0].indexOf("=") + 1, temp[0].length());
        temp[1] = temp[1].substring(temp[1].indexOf("=") + 1, temp[1].length());
        temp[2] = temp[2].substring(temp[2].indexOf("=") + 1, temp[2].length());
        temp[3] = temp[3].substring(temp[3].indexOf("=") + 1, temp[3].length());
        temp[4] = temp[4].substring(temp[4].indexOf("=") + 1, temp[4].length());

        temp[5] = temp[5].substring(temp[5].indexOf("=") + 1, temp[5].length());
        return temp;

    }

    public void writeUsers() {
        if (user.exists()) {
            try {
                FileOutputStream in = new FileOutputStream(user);
                ps.store(in, "");
            } catch (IOException e) {
            }
        } else {
            try {
                user.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
            }
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
                    player.sendMessage(ChatColor.GRAY + "/ad create [shopname] [number of hrs] [message]");
                    player.sendMessage(ChatColor.GRAY + "/ad delete - Stop your currently running ad");
                    player.sendMessage(ChatColor.GRAY + "/ad rates - Returns the current daily rate");
                    player.sendMessage(ChatColor.GRAY + "/ad on - Start receiving ads");
                    player.sendMessage(ChatColor.GRAY + "/ad off - Stop receiving ads");
                    player.sendMessage(ChatColor.GRAY + "/shop(s) - List shops available to tp");
                    return true;
                }
                if (action[0].equalsIgnoreCase("on")) {
                    if (ps.containsKey(player.getName())) {
                        if (ps.getProperty(player.getName()).equalsIgnoreCase("on")) {
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
                    if (!ps.containsKey(player.getName()) || ps.getProperty(player.getName()).equalsIgnoreCase("off")) {
                        player.sendMessage(ChatColor.GOLD + "[ShopAds]" + ChatColor.GRAY + "You weren't receiving ads");
                        return true;
                    } else {
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

                if (action[0].equalsIgnoreCase("create")) {
                    String[] temp = new String[action.length - 1];
                    for (int i = 0; i < temp.length; i++) {
                        temp[i] = action[i + 1];
                    }
                    action = new String[temp.length];
                    action = temp;
                    if (hasPermission(player, "sa.create")) {
                        if (action.length > 2) {
                            if (chargePlayer(player, Integer.parseInt(action[1]))) {
                                String playerName = player.getName();
                                Location loc = player.getLocation();
                                try {
                                    writeShops(playerName, action, player, loc);
                                    log.info("[ShopAds] " + player.getName() + " made a shop ad.");
                                } catch (FileNotFoundException ex) {
                                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                loadShops();
                            }
                        }

                        return true;
                    } else {

                        player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.RED + "You do not have permission for that command");
                        return true;
                    }
                }

                if (action[0].equalsIgnoreCase("yes") || action[0].equalsIgnoreCase("y")) {
                    return true;
                }

                if (action[0].equalsIgnoreCase("no") || action[0].equalsIgnoreCase("n")) {
                    return true;
                }

                if (action[0].equalsIgnoreCase("delete") || action[0].equalsIgnoreCase("del")) {
                    player.sendMessage (ChatColor.GOLD + "[ShopAds] " + ChatColor.RED + "You have removed your ad");
                    for(int i=0; i<Shops.length; i++){
                        if(Shops[i].getShopFile().getName().equalsIgnoreCase(player.getName()+".yml")){
                            Shops[i].setShopExpired(true);
                            try {
                                this.timeUpdater(i);
                            } catch (FileNotFoundException ex) {
                                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                            return true;
                }
            }
            if (Boolean.parseBoolean(pr.getProperty("usingShops"))) {
                if (commandLabel.equalsIgnoreCase("shops")) {
                    String message = null;
                    if (action.length == 1) {
                        teleport(action[0], player);
                    } else {
                        if (Shops != null) {
                            if (Shops.length > 0) {
                                if (Shops[0]!=null){
                                if (!Shops[0].shopExpired()) {
                                    message = Shops[0].getName();
                                }
                                }
                                
                                for (int i = 1; i < Shops.length; i++) {
                                    if(Shops[i]!=null){
                                    if (!Shops[i].shopExpired()) {
                                        message = (message + ", " + Shops[i].getAd());
                                    }
                                    }
                                }
                                player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "The current shops available to teleport to are:");
                                player.sendMessage(color.GRAY + message);
                            }
                        }
                        player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "The are no shops currently advertising");
                    }
                }
            } else {
                if (commandLabel.equalsIgnoreCase("shop") || commandLabel.equalsIgnoreCase("shops")) {
                    String message = null;
                    if (action.length == 1) {
                        teleport(action[0], player);
                    } else {
                        if (Shops != null) {
                            if (Shops.length > 0) {
                                if (!Shops[0].shopExpired()) {
                                    message = Shops[0].getName();
                                }
                                for (int i = 1; i < Shops.length; i++) {
                                    if (!Shops[i].shopExpired()) {
                                        message = (message + ", " + Shops[i].getAd());
                                    }
                                }
                                player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "The current shops available to teleport to are:");
                                player.sendMessage(color.GRAY + message);
                            }
                        }
                        player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "There are no shops currently advertising");
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

    private boolean chargePlayer(Player player, int hours) {
        if (hasAccount(player.getName())) {
            Holdings balance = iConomy.getAccount(player.getName()).getHoldings();
            if (balance.hasOver(hours * Double.parseDouble(pr.getProperty("cost")))) {
                balance.subtract(hours * Double.parseDouble(pr.getProperty("cost")));
                player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "You were charged " + iConomy.format(hours * Double.parseDouble(pr.getProperty("cost"))));
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "[ShopAds] You do not have enough money to make an ad that");
                player.sendMessage(ChatColor.RED + "long.");
                return false;
            }

        } else {
            player.sendMessage(ChatColor.RED + "[ShopAds] You do not have an account.");

        }
        return false;
    }

    private void makeConfig() {

        try {
            config.createNewFile();
            try {
                PrintWriter out = new PrintWriter(new FileWriter("plugins/ShopAds/config.yml"));
                out.println("#'maxShops' - The maximum number of ads allowed to each player !!NOT IMPLEMENTED!!");
                out.println("#'announceInterval' - The time in seconds between ad announcements [number]");
                out.println("#'random' - Should the ads be in a random order [true/false]");
                out.println("#'cost' - The cost per hour of advertising [number]");
                out.println("#'maxAdRunTime' - The longest time you want an ad to run for [number]");
                out.println("#'sendToAll' - Whether to send to all players, desregarding their choice [true/false]");
                out.println("#'usingShops' - Set this to true if you have another plugin that uses the /shop commands [true/false]");
                out.println("maxShops=1");
                out.println("announceInterval=10");
                out.println("cost=20");
                out.println("maxAdRunTime=24");
                out.println("random=false");
                out.println("sendToAll=false");
                out.println("usingShops=false");
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
            p.sendMessage(ChatColor.RED + "[ShopAds] You must enter a number for time");
            test = 0;
        }
        if (test <= 0) {
            p.sendMessage(ChatColor.RED + "[ShopAds] You must enter a number greater than zero");
            return false;
        } else {
            if (test > Integer.parseInt(pr.getProperty("maxAdRunTime"))) {
                p.sendMessage(ChatColor.RED + "[ShopAds] You must enter a number under " + pr.getProperty("maxAdRunTime"));
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

            String[] temporary = null;
            String[] output = null;
            try {
                temporary = this.getFileContents(file);
                if (this.getFileContents(file) != null) {
                    output = new String[this.getNumberOfLines(file) + 8];
                } else {
                    output = new String[8];
                }
            } catch (IOException ex) {
            }
            if (temporary != null) {
                if (temporary.length > 0) {
                    int i;
                    for (i = 0; i < temporary.length; i++) {
                        output[i] = temporary[i];
                    }
                    output[i] = ("<shop>");

                    output[i + 1] = ("Name=" + action[0]);
                    output[i + 2] = ("Ends=" + (String.valueOf(dateNow.getTime()) + ((3600000) * (Long.parseLong(action[1])))));
                    output[i + 3] = ("Location=" + (loc.getX() + "/" + loc.getY() + "/" + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw()));
                    message = action[2];
                    for (int z = 3; i < action.length; i++) {
                        message = message + " " + action[z];
                    }
                    output[i + 4] = ("Message=" + message);
                    output[i + 5] = ("Advertising=true");
                    output[i + 6] = ("Expired=false");
                    output[i + 7] = ("</shop>");
                    if (writeShop(output, file)) {

                        player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "Advertisement has been created for " + action[1] + " hours.");
                    } else {
                        player.sendMessage(ChatColor.RED + "[ShopAds] You shop encountered an error in creation");
                    }
                }
            } else {
                int i = 0;
                output[i] = ("<shop>");
                Long ends = (dateNow.getTime()+ (3600000*Long.parseLong(action[1])));
                output[i + 1] = ("Name=" + action[0]);
                output[i + 2] = ("Ends=" + String.valueOf(ends));
                output[i + 3] = ("Location=" + (loc.getX() + "/" + loc.getY() + "/" + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw()));
                message = action[2];
                if (action.length > 3) {
                    for (int z = 3; z < action.length; z++) {
                        //log.info(String.valueOf(z));
                        message = message + " " + action[z];
                    }
                }
                output[i + 4] = ("Message=" + message);
                output[i + 5] = ("Advertising=true");
                output[i + 6] = ("Expired=false");
                output[i + 7] = ("</shop>");
                if (writeShop(output, file)) {

                    player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "Advertisement has been created for " + action[1] + " hours.");
                } else {
                    player.sendMessage(ChatColor.RED + "[ShopAds] You shop encountered an error in creation");
                }

            }

        }
        this.reload();
    }

    private boolean writeShop(String[] output, File file) {
        //log.info("Writing Shop");
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
        } catch (IOException ex) {
            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
        }
        PrintWriter out = new PrintWriter(fw);

        for (int i = 0; i < output.length; i++) {
           // log.info("Writing Line");
            out.println(output[i]);
        }
        out.close();

        return true;
    }

    private String[] getFileContents(File f) throws FileNotFoundException, IOException {
        String[] file;
        FileReader fr = null;
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(f));
        int lines = this.getNumberOfLines(f);
        if (lines > 0) {
            file = new String[lines];
            for (int i = 0; i < this.getNumberOfLines(f); i++) {
                file[i] = br.readLine();
            }
            return file;
        } else {
            return null;
        }


    }

    private int getNumberOfLines(File f) throws FileNotFoundException, IOException {
        String temp = null;
        int count = 0;
        FileReader fr = null;
        BufferedReader br = null;
        br = new BufferedReader(new FileReader(f));
        temp = br.readLine();
        while (temp != null) {
            count++;
            temp = br.readLine();

        }
        return count;
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
            if (Shops[index]!=null){
               // log.info(String.valueOf(Shops[index].shopExpired()));
            if (Shops[index].getTimeToEnd()<timeNow||Shops[index].shopExpired()) {
                log.info("[ShopAds] " + Shops[index].getName() + " has expired");
                //(Shops[index].getShopFile()).delete();
                String[] output = new String[this.getFileContents(Shops[index].getShopFile()).length-8];
                if(output.length==0){
                    this.writeShop(output, Shops[index].getShopFile());
                    return;
                }
                String[] temp = this.getFileContents(Shops[index].getShopFile());
                int i=0;
                boolean match=false;
                int line=-1;
                while (!match){
                   if(i<temp.length){
                       if(!temp[i].equalsIgnoreCase("<shop>")&&!temp[i].equalsIgnoreCase("</shop>")){

                           String parse= temp[i].substring(temp[i].indexOf("=")+1, temp[i].length());
                    if(parse.equalsIgnoreCase(Shops[index].getName())){
                        line=i-1;
                        match=true;
                    }
                       }
                   }else{
                       match=true;
                   }
                   i++;
                }
       log.info("Line Number is : " + String.valueOf(line));
                if(line!=-1){
                    int z=0;
                for (z=0; z<line;z++){
                 
                    output[z]=temp[z];
                }
                for (int y=line+8; y<temp.length;y++){
                    //log.info (String.valueOf(y));
                    output[z]=temp[y];
                    z++;
                    
                }
                this.writeShop(output, Shops[index].getShopFile());
                }else{
                    return;
                }
            }
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
        if (Shops != null) {
            if (Shops.length > 0) {
                return Shops.length;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private void teleport(String name, Player player) {
        if (shopExists(name) != -1) {
            teleportToShop(shopExists(name), player);
            player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "You have been teleported to " + Shops[shopExists(name)].getName());
        }
    }

    public void teleportToShop(int index, Player player) {
        Location loc = new Location(player.getWorld(), Shops[index].getLocation(0), Shops[index].getLocation(1), Shops[index].getLocation(2), Float.parseFloat(String.valueOf(Shops[index].getLocation(4))), Float.parseFloat(String.valueOf(Shops[index].getLocation(3))));
        player.teleport(loc);

    }

    private int shopExists(String name) {

        for (int i = 0; i < Shops.length; i++) {
            if (Shops[i].getName().toLowerCase().startsWith(name.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }
}
