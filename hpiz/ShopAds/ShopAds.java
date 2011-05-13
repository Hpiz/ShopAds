package hpiz.ShopAds;

/**
 *
 * @author Hpiz
 */
import com.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
import org.bukkit.scheduler.BukkitScheduler;

/**
 * ShopAds for Bukkit
 *
 * @author Hpiz
 */
public class ShopAds extends org.bukkit.plugin.java.JavaPlugin {

    private timerThread thread;
    public static PermissionHandler permissionHandler;
    private final ShopAdsPlayerListener playerListener = new ShopAdsPlayerListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    private boolean wantsToCreateAd = false;
    public static Permissions Permissions = null;
    public static iConomy iConomy = null;
    public static final Logger log = Logger.getLogger("Minecraft");
    private String commandSent;
    private String name;
    public Server server = getServer();
    private String shopname;
    private int maximumShops;
    private String key;
    private String[] message;
    private File config = new File("plugins/ShopAds/config.yml");
    private File dir = new File("plugins/ShopAds/");
    public String[] ads;
    private Calendar cal = Calendar.getInstance();
    private Properties pr = new Properties();
    private String constructedMessage;
    public Date date = cal.getTime();
    public Long serverStartTime = date.getTime();
    public boolean pluginState = false;
    private int lastMessage;
    private File[] listOfFiles = dir.listFiles();
    private String[] messages;
    private boolean running = false;
    public Player[] onlinePlayers;
    public boolean random;
    private boolean sendToAll;
    private ChatColor color;
    public String [] shopNames;
    public Location [] shopLocs;
    

    public void onDisable() {
    }

    public void announce(int index) {
        
        
        
        announce(messages[index], shopNames[index]);
    }
    public ShopAds(){
                super();

        thread = new timerThread(this);
    }
    public void announce(String line, String shopName) {







        if (sendToAll) {
            getServer().broadcastMessage(line);
        } else {
            for (Player player : getServer().getOnlinePlayers()) {
                
                
                    player.sendMessage(color.GOLD + "[" + shopName + "] " + color.GRAY + line);

                
            }
        }
    }

    public void onEnable() {
        
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
        try {
            this.reload();
            /*setupPermissions();
            setupIconomy();
            server = getServer();
            
            // EXAMPLE: Custom code, here we just output some info so we can check all is well
            
            
            
             */
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
        }
        BukkitScheduler scheduler = getServer().getScheduler();
        Long interval = Long.valueOf(pr.getProperty("interval"));
                
        scheduler.scheduleAsyncRepeatingTask(this, thread, interval, interval);
        PluginDescriptionFile pdfFile = this.getDescription();
        setupPermissions();
        log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!");
        pluginState = true;
        random = false;
        sendToAll = false;



    }

    private Long readTimeLeft() {
        Calendar calNow = Calendar.getInstance();
        Date dateNow = calNow.getTime();
        Long timeLeft, timeMade, runTime, timeNow;
        String t;
        timeNow = dateNow.getTime();
        t = constructedMessage.substring(constructedMessage.indexOf("||") + 2, constructedMessage.indexOf(":") - 1);
        timeMade = Long.parseLong(t);
        t = constructedMessage.substring(constructedMessage.indexOf(":") + 1, constructedMessage.lastIndexOf("||") - 1);
        runTime = Long.parseLong(t);
        timeLeft = (timeNow - (timeMade + runTime));
        return timeLeft;
    }

    public void reload() throws FileNotFoundException, IOException {

        if (config.exists()) {
            try {
                FileInputStream in = new FileInputStream(config);
                pr.load(in);
                log.info("[ShopAds] Config loaded");
            } catch (IOException e) {
                log.info("[ShopAds] There was an error reading the config");
            }
        } else {
            if (!dir.exists()) {
                dir.mkdir();
            }
            this.makeConfig();
        }

        this.loadAds();

    }

    public void setMessage(String temp, int z) {

        messages[z] = temp;

    }
    
        public void setShopName (String temp, int z) {
log.info(temp);
        shopNames[z] = temp;

    }

    public void loadAds() throws FileNotFoundException, IOException {

        int z = 0;
        for (int i = 0; i < listOfFiles.length; i++) {
            String fileName;
            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();
                if (fileName.endsWith(".txt") || fileName.endsWith(".TXT")) {
                    z++;
                }
            }
        }
        messages = new String[z];
        shopNames = new String[z];
        z = 0;
        for (int i = 0; i < listOfFiles.length; i++) {
            String fileName;

            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();
                if (fileName.endsWith(".txt") || fileName.endsWith(".TXT")) {
                    String temp;
                       String temp2;
                    FileReader fr;
                    fr = new FileReader(listOfFiles[i].getPath());
                    BufferedReader br = new BufferedReader(fr);
                    temp = br.readLine();
                    temp2 = temp.substring(0, temp.indexOf("_"));
                    
                    
                    temp = temp.substring(temp.lastIndexOf("||") + 2, temp.length());
                    
                    this.setMessage(temp, z);
                    this.setShopName(temp2, z);

                    z = z + 1;
                }
            }
        }
        log.info("[ShopAds] Advertisements have been loaded!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        String[] action = args;
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (commandLabel.equalsIgnoreCase("ad")||commandLabel.equalsIgnoreCase("ads")) {
                try {
                    this.timeUpdater();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (action.length == 0) {
                    player.sendMessage(ChatColor.GOLD + "[ShopAds]");
                    player.sendMessage(ChatColor.GRAY + "/ad [shopname] [number of cycles] [message] - Creates an advertisement for the desired shop and 6hr cycles");
                    player.sendMessage(ChatColor.GRAY + "/ad rates - Returns the current daily rate");
                    return true;
                }
                if (action[1].equalsIgnoreCase("rates")) {
                    player.sendMessage(ChatColor.GRAY + "Current rate is: 15 Dollars for every 6 hours");
                    return true;
                }

                if (action.length >= 3) {
                    String playerName = player.getName();
                    Location loc = player.getLocation();
                    writeShop(playerName, action, player, loc);
                    return true;
                }



                return true;
            }
        } else {
            log.info("[ShopAds] Only players currently on the server can use this plugins functions!");

        }
        return false;
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
            p.sendMessage("[ShopAds] You must enter a number from 1 to 14 for the days value");
            test = 0;
        }
        if (test <= 0) {
            return false;
        } else {
            if (test >= 15) {
                return false;
            } else {
                return true;
            }
        }
    }

    public void writeShop(String playerName, String[] action, Player player, Location loc) {

        Calendar calNow = Calendar.getInstance();
        Date dateNow = calNow.getTime();
        String time;
        File shops = new File("plugins/ShopAds/" + playerName + ".txt");
        shopname = action[0];
        if (isValidNumber(action[1], player)) {
            time = (String.valueOf(dateNow.getTime()) + ":" + (3600000) * (Integer.parseInt(action[1])));

            constructedMessage = (shopname + "_" + String.valueOf(loc.getX()) + "," + String.valueOf(loc.getBlockY()) + "," + String.valueOf(loc.getBlockZ()) + "||" + time + "||" + action[2]);
            if (action.length >= 3) {
                for (int z = 3; z <= ((action.length) - 1); z++) {
                    constructedMessage = (constructedMessage + " " + action[z]);
                }
            }
            try {
                if (!shops.exists()) {
                    try {
                        shops.createNewFile();
                        log.info("[ShopAds] No Shops file found for " + playerName + ", file created");
                    } catch (IOException ex) {
                        log.info("[ShopAds] There was a problem creating the shops file");
                    }
                }
                PrintWriter out2 = new PrintWriter(new FileWriter("plugins/ShopAds/" + playerName + ".txt"));
                out2.print(constructedMessage);
                out2.close();
            } catch (IOException ex) {
                log.info("[ShopAds] There was a problem writing to the shops file");
            }
            player.sendMessage(ChatColor.GOLD + "[ShopAds] " + ChatColor.GRAY + "Advertisement has been created for " + action[1] + " days.");
        } else {
            player.sendMessage(ChatColor.RED + "[ShopAds] You did not enter a valid number for cycles.");
        }
    }

    public void setupPermissions() {

        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
        if (this.Permissions == null) {
            if (test != null) {
                this.Permissions = (Permissions) test;
                log.info("[ShopAds] Hooked into permissions plugin");
            } else {
                log.info("[ShopAds] Permission system not found. Disabling plugin.");
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    public void setupIconomy() {

        Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");
        if (this.iConomy == null) {
            if (test != null) {
                this.iConomy = (iConomy) test;
            } else {
                log.info("[ShopAds] iConomy not found. Disabling plugin.");
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    public void timeUpdater() throws FileNotFoundException, IOException {

        Calendar calNow = Calendar.getInstance();
        Date dateNow = calNow.getTime();
        Long timeNow;
        Long timeLeft;

        File[] listOfFiles = dir.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            String fileName;
            if (listOfFiles[i].isFile()) {
                fileName = listOfFiles[i].getName();
                if (fileName.endsWith(".txt") || fileName.endsWith(".TXT")) {
                    FileReader fr;
                    fr = new FileReader(listOfFiles[i].getPath());
                    BufferedReader br = new BufferedReader(fr);
                    constructedMessage = br.readLine();
                    timeLeft = this.readTimeLeft();
                    if (timeLeft <= 0) {
                        listOfFiles[i].delete();


                    }
                }
            }
        }
    }

    public int getLastMessage() {
        
        return lastMessage;
    }

    public String[] getMessages() {
        return messages;
    }

    public boolean pluginState() {
        if (pluginState) {
            return true;
        } else {
            return false;
        }



    }

    public void announceManager() {


        for (int i = 0; i < messages.length; i++) {
     

                this.announce(i);

          
        }
    }

    public int numberOfAds() {
        return ads.length;
    }

    public Player[] getOnlinePlayers() {
        return getServer().getOnlinePlayers();
    }

    public int getMessagesLength() {
        return messages.length;
    }
    /**   public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
    log.info("someone joined");
    Player[] onlinePlayers;
    onlinePlayers = getOnlinePlayers();
    if (!running) {
    if (onlinePlayers.length == 1) {
    announceDelay.run(onlinePlayers, messages, lastMessage, pluginState);
    running=true;
    }
    }
    }*/
}
