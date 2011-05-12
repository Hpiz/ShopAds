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
import java.util.Calendar;
import java.util.Date;

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
    private int time;
    private int maximumShops;
    private String key;
    private String[] message;
    private File config = new File("plugins/ShopAds/config.yml");
    private File dir = new File("plugins/ShopAds/");
    private String[] ads;
    private Calendar cal = Calendar.getInstance();
    private Properties pr = new Properties();
    private String constructedMessage;
    public Date date = cal.getTime();
    public Long serverStartTime = date.getTime();
    

    
    
    
    public void onDisable() {
    
     
    }

  
    public void onEnable() {
        
        PluginManager pm = getServer().getPluginManager();
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
     
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info( "[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!" );
        
    }
    
    
    private String readTimeLeft () throws FileNotFoundException, IOException{
                double timeLeft;
                String t;
                t = constructedMessage.substring(0,constructedMessage.indexOf(" "));
                
                return t; 
               
                        
                //int t = findTimeLeft (key);
                
                 
            
        
    

    }

    

    
     public void reload() throws FileNotFoundException, IOException
    {

    	if (config.exists())
    	{
            
            try
            {
                FileInputStream in = new FileInputStream(config);
                pr.load(in);
              
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
               this.makeConfig();

    	    
    	}
        
                      
    }

    @Override
   public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        String[] action = args;
        
            if (sender instanceof Player) {

                Player player = (Player) sender;
               
                if (commandLabel.equalsIgnoreCase("ad")) {
                try {
                    this.timeUpdater();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ShopAds.class.getName()).log(Level.SEVERE, null, ex);
                }
                    if (action.length == 0){
                    player.sendMessage("[ShopAds]");
                    player.sendMessage("/ad [shopname] [number of cycles] [message] - Creates an advertisement for the desired shop and 6hr cycles");
                    player.sendMessage("/ad rates - Returns the current daily rate");
                    }
                   // if (action[1].equalsIgnoreCase("rates")){
                   //     player.sendMessage("[ShopAds] Current rates are ");
                   // }
             
                    if (action.length >= 3){
                        String playerName = player.getName();
                        writeShop(playerName, action, player);
                        
                
                       
                            
                    }
                }
            
                if (commandLabel.equalsIgnoreCase("rate")){
                    player.sendMessage("Current rate is: 15 Dollars for every 6 hours");
                }
         

               
            
          
        }
            log.info("[ShopAds] Only players currently on the server can use this plugins functions!");
            return true;
    }
    
 private void makeConfig (){
     try
    	    {
                config.createNewFile();
                
                 
                try
                {
        			PrintWriter out = new PrintWriter(new FileWriter("plugins/ShopAds/config.yml"));

        			out.println("maxshops=1");
        			out.println("interval=120");
                                out.println("cost=120");
        			out.close();
                                log.info("[ShopAds] No config found, created default config");
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
 
 private void writeShop (String playerName, String[] action, Player player){
     
     
     File shops = new File("plugins/ShopAds/" + playerName + ".txt");
                        shopname = action[0];
                        
                        
                        if (isValidNumber(action[1], player)){
                        time = ((21600000) * (Integer.parseInt(action[1])));
                        constructedMessage = (time + " " + action[2]);
                        if (action.length >= 3){
                        for (int z=3; z<=((action.length)-1);z++){
                          
                            constructedMessage = (constructedMessage + " " + action[z]);
                        }
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
                            
                            
                        
                        player.sendMessage("[ShopAds] Advertisement has been created for " + action[1] + " days.");
 }
                        else {
                            player.sendMessage("You did not enter a valid number for cycles.");
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

    
    
private void timeUpdater() throws FileNotFoundException, IOException{
            
    Calendar calNow = Calendar.getInstance();
    Long timeNow ;
    Long timeLeft;
    String left;
    Date dateNow = calNow.getTime();
            
        File[] listOfFiles = dir.listFiles(); 
        for(int i=0;i<listOfFiles.length;i++){ 
              String fileName;
              
         if (listOfFiles[i].isFile()) 
          {
          
          fileName = listOfFiles[i].getName();
          
             if (fileName.endsWith(".txt") || fileName.endsWith(".TXT"))
             {
            FileReader fr;
           
                fr = new FileReader(listOfFiles[i].getPath());
                BufferedReader br = new BufferedReader(fr);
                constructedMessage = br.readLine();
                left = this.readTimeLeft();
                timeLeft = Long.parseLong(left);  
                timeNow = dateNow.getTime();
                timeLeft = timeLeft - (timeNow - serverStartTime);
                constructedMessage = (String.valueOf(timeLeft) + constructedMessage.substring(constructedMessage.indexOf(" "), (constructedMessage.length())));
                PrintWriter out2 = new PrintWriter(new FileWriter(listOfFiles[i].getPath()));
                        
                                 out2.print(constructedMessage);
                                 
                                 out2.close();
           
             
            
        
        
            }
        }
    }
  }       







}