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
    
    public ShopAdsShop (String n, String a, double[] l, Double t, File f){
        name=n;
        location =  l;
        ad = a;
        timeToEnd = t;
        shopFile = f;        
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
    public void setName (String s){
        name=s;
    }
    
}
