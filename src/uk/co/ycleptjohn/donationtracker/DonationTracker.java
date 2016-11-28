package uk.co.ycleptjohn.donationtracker;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class DonationTracker extends JavaPlugin implements Listener {

	private static Plugin plugin;
	
	public void onEnable() {
		plugin = this;
		//registerEvents(this, new WhateverHandler());
		
	}
	
	public void onDisable() {
		plugin = null;
	}
	
	public static Plugin getPluhgin() {
		return plugin;
	}
	
	public static void registerEvents(Plugin plugin, Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
		}
	}

}
