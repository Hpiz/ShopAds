package org.hpiz.ShopAds;

import java.io.File;

public class ShopAdsShop extends ShopAds
  {

    private String name;
    private double[] location = new double[5];
    private String world;
    private String ad;
    private Double timeToEnd;
    private File shopFile;
    private boolean isAdvertising;
    private boolean isExpired;
    private String shopOwner;

    public ShopAdsShop(String owner, String shopName, Double time, double[] locationOfPlayer, String w, String advert, boolean advertising, boolean e, File f)
      {
        this.shopOwner = owner;
        this.isAdvertising = advertising;
        this.name = shopName;
        this.location = locationOfPlayer;
        this.ad = advert;
        this.timeToEnd = time;
        this.shopFile = f;
        this.isExpired = e;
        this.world = w;
      }

    public String getWorld()
      {
        return this.world;
      }

    public String getName()
      {
        return this.name;
      }

    public double[] getLocation()
      {
        return this.location;
      }

    public double getLocation(int i)
      {
        return this.location[i];
      }

    public String getAd()
      {
        return this.ad;
      }

    public double getTimeToEnd()
      {
        return this.timeToEnd.doubleValue();
      }

    public File getShopFile()
      {
        return this.shopFile;
      }

    public boolean shopAdvertising()
      {
        return this.isAdvertising;
      }

    public void setShopAdvertising(boolean b)
      {
        this.isAdvertising = b;
      }

    public boolean shopExpired()
      {
        return this.isExpired;
      }

    public void setShopExpired(boolean b)
      {
        this.isExpired = b;
      }

    public String getShopOwner()
      {
        return this.shopOwner;
      }
  }