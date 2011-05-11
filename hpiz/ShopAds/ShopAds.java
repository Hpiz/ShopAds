package hpiz.ShopAds;

/**
 *
 * @author Chris
 */
import com.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;
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

/**
 * ShopAds for Bukkit
 *
 * @author Hpiz
 */
public class ShopAds extends org.bukkit.plugin.java.JavaPlugin {
    private final ShopAdsPlayerListener playerListener = new ShopAdsPlayerListener(this);
    private final ShopAdsBlockListener blockListener = new ShopAdsBlockListener(this);
    private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
    private boolean wantsToCreateAd = false;
    public static Permissions Permissions = null;
    public static iConomy iConomy = null;
    static final Logger log = Logger.getLogger("Minecraft");
    private String commandSent;
    private String name;
    Server server = getServer();
    private String shopname;
    private String time;
    private int maximumShops;
    private String key;
    private String[] message;
    private File file = new File("plugins/ShopAds/config.yml");
    private File dir = new File( "plugins/ShopAds");
    private String[] ads;
    
    
    public void onDisable() {
    
     
    }

  
    public void onEnable() {

        PluginManager pm = getServer().getPluginManager();
        this.reload();
        setupPermissions();
    	setupIconomy();
        server = getServer();
    
        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        
       
       
      
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
        }
    private void readShops (){
        String fileName;
        
        File[] listOfFiles = dir.listFiles(); 
        for(int i=0;i<listOfFiles.length;i++){
         if (listOfFiles[i].isFile()) 
          {
          fileName = listOfFiles[i].getName();
             if (fileName.endsWith(".txt") || fileName.endsWith(".TXT"))
             {
                FileReader fr;
                    try {
                        fr = new FileReader(dir + fileName);
                    } catch (FileNotFoundException ex) {
                        log.info("[ShopAds] Failes to automatically load announcements");
                        fr=null;
                    }
                BufferedReader br = new BufferedReader(fr);
                    try {
                        key=br.readLine();
                    } catch (IOException ex) {
                        Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                    }
                 
            }
        }
    }
    }

    
     public void reload()
    {
    	if (file.exists())
    	{
            Properties pr = new Properties();
            try
            {
                FileInputStream in = new FileInputStream(file);
                pr.load(in);
                FileReader fr = new FileReader("plugins/ShopAds/config.yml");
                BufferedReader br = new BufferedReader(fr);
                key = br.readLine();
                log.info("[ShopAds] Config loaded");

            }
            catch (IOException e)
            {
                log.info("[ShopAds] There was an error reading the config");
            }
    	}
    	else
    	{
    		

        	if (!dir.exists())
        	{
        		dir.mkdir();
                      
        	}

    	    try
    	    {
                file.createNewFile();
                log.info("[ShopAds] No config found, creating default config");
                 
                try
                {
        			PrintWriter out = new PrintWriter(new FileWriter("plugins/ShopAds/config.yml"));

        			out.println("maxshops=1");
        			out.println("interval=120");
        			out.close();
        		}
                catch (IOException e)
        		{
                            log.info("[ShopAds] Error writing to config");
        		}
    	     }
    	    catch(IOException ioe)
    	    {
                log.info("[ShopAds] Error creating config file");
    	    }
    	}
        readShops();
                      
    }

    @Override
   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        String[] action = args;
        String constructedMessage;
            if (sender instanceof Player) {

                Player player = (Player) sender;
                
                if (commandLabel.equalsIgnoreCase("ad")) {
                    
                    if (action.length == 0){
                    player.sendMessage("[ShopAds]");
                    player.sendMessage("/ad [shopname] [time] [message] - Creates an advertisement for the desired shop and run-time");
                    player.sendMessage("/ad rates - Returns the current daily rate");
                    }
                   // if (action[1].equalsIgnoreCase("rates")){
                   //     player.sendMessage("[ShopAds] Current rates are ");
                   // }
             
                    if (action.length >= 4){
                        String playerName = player.getName();
                        File shops = new File("plugins/ShopAds/" + playerName + ".txt");
                        shopname = action[0];
                        time = action[1].toString();
                     
                        int x=0;
                        log.info(action[2]);
                        if (isValidNumber(time, player)){
                        constructedMessage = (time + " " + action[2]);
                        for (int z=3; z<=((action.length)-1);z++){
                          
                            constructedMessage = (constructedMessage + " " + action[z]);
                           }
                     try 
                            {
                        if(!shops.exists()){
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
                            
                            
                        
                        player.sendMessage("[ShopAds] Advertisement has been created for " + time + " days.");
                    }
                       
                            
                        
                }
                }
            }

                 return true;
            }
    
 private boolean isValidNumber(String T, Player p){
    int test;
     try{
         test = Integer.parseInt(T);
     }
     catch (Exception e){
         p.sendMessage("[ShopAds] You must enter a number from 1 to 14 for the days value");
         test=0;
     
 }
     if(test <= 0){
         return false;
     }
     else{if(test >=15){
         return false;
     }
     else{
         return true;
     }
     }
     
 
 }
 
    
 public void setupPermissions() {
    	Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

    	if(this.Permissions == null) {
    	    if(test != null) {
    	    	this.Permissions = (Permissions)test;
    	    } else {
    	    	log.info( "[ShopAds] Permission system not found. Disabling plugin." );
    	    	this.getServer().getPluginManager().disablePlugin(this);
    	    }
    	}
    }

    public void setupIconomy() {
    	Plugin test = this.getServer().getPluginManager().getPlugin("iConomy");

    	if(this.iConomy == null) {
    	    if(test != null) {
    	    	this.iConomy = (iConomy)test;
    	    } else {
    	    	log.info( "[ShopAds] iConomy not found. Disabling plugin." );
                this.getServer().getPluginManager().disablePlugin(this);
    	    }
    	}
    }

}

      
    

  



  
      
    

