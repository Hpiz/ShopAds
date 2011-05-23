/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hpiz.ShopAds;

import java.io.File;

/**
 *
 * @author Hpiz
 */
public class ShopAdsShop extends ShopAds{
    private String name;
    private double [] location = new double [5];
    private String ad;
    private Double timeToEnd;
    private File shopFile;
    private boolean isAdvertising;
    private boolean isExpired;
    private String shopOwner;
    
    public ShopAdsShop (String owner, String shopName, Double time, double[] locationOfPlayer, String advert, boolean advertising, boolean e, File f){
        shopOwner=owner;
        isAdvertising=advertising;
        name=shopName;
        location =  locationOfPlayer;
        ad = advert;
        timeToEnd = time;
        shopFile = f;  
        isExpired=e;
        
    }
    public String getName(){
        return name;
    }
    public double[] getLocation (){
        return location;
    }
    public double getLocation (int i){
        return location[i];
    }
    public String getAd (){
        return ad;
    }
    public double getTimeToEnd (){
        return timeToEnd;
    }
    public File getShopFile (){
        return shopFile;
    }
    public boolean shopAdvertising(){
        return isAdvertising;
    }
    public void setShopAdvertising (boolean b){
        isAdvertising=b;
    }
    public boolean shopExpired (){
        return isExpired;
    }
    public void setShopExpired (boolean b){
        isExpired=b;
    }
    public String getShopOwner (){
        return shopOwner;
    }
    
}
