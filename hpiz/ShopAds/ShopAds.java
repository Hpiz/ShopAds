package hpiz.ShopAds;

/**
 *
 * @author Hpiz
 */
import com.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
    public static Permissions Permissions = null;
    public static iConomy iConomy = null;
    public static final Logger log = Logger.getLogger("Minecraft");
    public Server server = getServer();
    private String shopname;
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
    public Player[] onlinePlayers;
    public boolean random;
    private boolean sendToAll;
    private ChatColor color;
    public String[] shopNames;
    public Location[] shopLocs;
    private File user = new File("plugins/ShopAds/user.dat");

    public void onDisable() {
    }

    public void announce(int index) {



        announce(messages[index], shopNames[index]);
    }

    public ShopAds() {
        super();

        thread = new timerThread(this);
    }

    public void announce(String line, String shopName) {







        if (sendToAll) {
            getServer().broadcastMessage(line);
        } else {
            Player[] player = this.getOnlinePlayers();
            for (int i = 0; i < getServer().getOnlinePlayers().length; i++) {


                if (receiveStatus(player[i].getName())) {
                    player[i].sendMessage(color.GOLD + "[" + shopName + "] " + color.GRAY + line);
                }

            }
        }
    }

    public boolean receiveStatus(String name) {
        if (user.exists()) {
            FileReader fr = null;
            try {
                fr = new FileReader(user.getPath());
            } catch (FileNotFoundException ex) {

                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
            }
            BufferedReader br = new BufferedReader(fr);

            while (0 < 1) {
                String temp = null;
                try {
                    temp = br.readLine();
                } catch (IOException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (temp != null) {
                    if (temp.equalsIgnoreCase(name)) {
                        
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }

        return false;
    }

    public int getNumberOfLines(File file) {
  
        if (file.exists()) {
            FileReader fr = null;
            try {
                fr = new FileReader(file.getPath());
            } catch (FileNotFoundException ex) {

                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
            }
            BufferedReader br = new BufferedReader(fr);
            boolean endOfFile = false;
            int lineNumber = 0;
            int maxLines = 0;

            
            while (!endOfFile) {
                String temp = null;
                try {
                    temp = br.readLine();
                    
                } catch (IOException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (temp != null) {
                    
                    lineNumber++;
                } else {
                    endOfFile = true;
                    maxLines = lineNumber;
                }
            }
            

            return maxLines;
        } else {
            return 0;
        }


    }

    public int getLineOfName(File file, String name) {
        if (file.exists()) {
            FileReader fr = null;
            try {
                fr = new FileReader(file.getPath());
            } catch (FileNotFoundException ex) {

                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
            }
            BufferedReader br = new BufferedReader(fr);
            boolean endOfFile = false;
            int lineNumber = 0;
            int maxLines = this.getNumberOfLines(file);
            int nameLine = -1;

            for (int i = 0; i < maxLines; i++) {
                String temp = null;
                try {
                    temp = br.readLine();
                } catch (IOException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (temp != null) {
                    lineNumber++;

                    if (temp.equalsIgnoreCase(name)) {
                        nameLine = lineNumber;


                    }
                }
                endOfFile = true;

            }

            return nameLine;
        } else {
            return 0;
        }


    }

    public void removeStatus(String name) {
        if (user.exists()) {
            int lines = this.getNumberOfLines(user);
            
            if (lines > 0) {
                if (lines > 1) {
                    FileReader fr = null;
                    try {
                        fr = new FileReader(user.getPath());
                    } catch (FileNotFoundException ex) {

                        Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    BufferedReader br = new BufferedReader(fr);
                    int maxLines = 0;
                    int nameLine = 0;
                    String[] file;
                    maxLines = this.getNumberOfLines(user);
                    
                    nameLine = this.getLineOfName(user, name);
                    file = new String[maxLines - 1];
                    for (int i = 0; i < maxLines; i++) {
                        String temp = null;
                        try {
                            temp = br.readLine();
                        } catch (IOException ex) {
                            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (nameLine < maxLines) {
                            if (i < nameLine - 1) {
                                file[i] = temp;
                            }
                            if (i > nameLine - 1) {
                                file[i - 1] = temp;
                            }
                        } else {
                            if (i < nameLine - 1) {
                                file[i] = temp;
                            }
                        }
                    }
                    PrintWriter out = null;
                    try {
                        out = new PrintWriter(new FileWriter("plugins/ShopAds/user.dat"));
                    } catch (IOException ex) {
                        Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    for (int i = 0; i < file.length; i++) {
                        out.println(file[i]);
                    }
                    out.close();
                } else {
                    
                     PrintWriter out = null;
                    try {
                        out = new PrintWriter(new FileWriter("plugins/ShopAds/user.dat"));
                    } catch (IOException ex) {
                        Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                    }
                   
                        out.println();
                   
                    out.close();
                    return;
                }
            }
        } else {
                              
                     PrintWriter out = null;
                    try {
                        out = new PrintWriter(new FileWriter("plugins/ShopAds/user.dat"));
                    } catch (IOException ex) {
                        Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                    }
                   
                        out.println();
                   
                    out.close();
                    return;
        }
        return;
    }

    public void addStatus(String name, Player player) {
        if (user.exists()) {
            if (!this.receiveStatus(name)) {

                int lines = this.getNumberOfLines(user);

                FileReader fr = null;
                try {
                    fr = new FileReader(user.getPath());
                } catch (FileNotFoundException ex) {

                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                }
                BufferedReader br = new BufferedReader(fr);
                int maxLines = 0;
                int nameLine = 0;
                String[] file = null;
                maxLines = this.getNumberOfLines(user);
                

                for (int i = 0; i < maxLines; i++) {
                    String temp = null;
                    try {
                        temp = br.readLine();

                    } catch (IOException ex) {
                        Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                    }
                   if(i==0){
                    if(temp.isEmpty()){
                        file = new String[maxLines];
                       file[i]=player.getName();
                   }
                   else{
                       file = new String[maxLines + 1];
                    file[i] = temp;
                   }
                   }
                }
                file[file.length - 1] = name;


                PrintWriter out = null;
                try {
                    out = new PrintWriter(new FileWriter("plugins/ShopAds/user.dat"));
                } catch (IOException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (int i = 0; i < file.length; i++) {
                    out.println(file[i]);
                }
                out.close();
                player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "You will now receive advertisements");
            } else {
                player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "You Are already receiving ads");
            }

        } else {
            log.info("[ShopAds] No user.dat file found, creating one");
            try {
                user.createNewFile();
                log.info("[ShopAds] User.dat created");
            } catch (IOException ex) {
                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
            }
            PrintWriter out = null;
            try {
                out = new PrintWriter(new FileWriter("plugins/ShopAds/user.dat"));
            } catch (IOException ex) {
                Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
            }
            player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "You Will now receive advertisements");
            out.println(name);
            out.close();

            return;
        }
    }

    @Override
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
        double cost = Double.parseDouble(pr.getProperty("cost"));

        scheduler.scheduleAsyncRepeatingTask(this, thread, interval, interval);
        PluginDescriptionFile pdfFile = this.getDescription();
        setupPermissions();
        log.info("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!");
        pluginState = true;
        random = false;
        sendToAll = false;



    }

    private Long readTimeLeft() {
        try {
            loadAds();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            FileInputStream in = new FileInputStream(config);
            pr.load(in);
            log.info("[ShopAds] Config loaded");
        }

        this.loadAds();
        log.info("[ShopAds] Advertisements have been loaded!");

    }

    public void setMessage(String temp, int z) {

        messages[z] = temp;

    }

    public void setShopName(String temp, int z) {

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

    }

    public void writeUserOn(Player player) {

        this.addStatus(player.getName(), player);

    }

    public void writeUserOff(Player player) {
        if (user.exists()) {
            if (this.receiveStatus(player.getName())) {
                removeStatus(player.getName());
                player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "You will no longer receive advertisements");
                return;
            }
        }
        player.sendMessage(color.GOLD + "[ShopAds]" + color.GRAY + "You weren't receiving any advertisements");


    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        String[] action = args;
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (commandLabel.equalsIgnoreCase("ad") || commandLabel.equalsIgnoreCase("ads")) {
                try {
                    this.timeUpdater();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (action.length == 0|| action[0].equalsIgnoreCase("?")) {
                    player.sendMessage(ChatColor.GOLD + "[ShopAds]");
                    player.sendMessage(ChatColor.GRAY + "/ad [shopname] [number of cycles] [message] - Creates an advertisement for the desired shop and 6hr cycles");
                    player.sendMessage(ChatColor.GRAY + "/ad rates - Returns the current daily rate");
                    player.sendMessage(ChatColor.GRAY + "/ad on - Start receiving ads");
                    player.sendMessage(ChatColor.GRAY + "/ad off - Stop receiving ads");
                    return true;
                }
                if (action[0].equalsIgnoreCase("on")) {
                    writeUserOn(player);
                }
                if (action[0].equalsIgnoreCase("off")) {
                    writeUserOff(player);
                }
                if (action[0].equalsIgnoreCase("rates")) {
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
