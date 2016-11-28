package uk.co.ycleptjohn.donationtracker;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import uk.co.ycleptjohn.donationtracker.commands.cmdAddDonation;
import uk.co.ycleptjohn.donationtracker.configuration.ConfigHandler;

public class DonationTracker extends JavaPlugin implements Listener {

	private static Plugin plugin;
	private ConfigHandler cf;
	
	public void onEnable() {
		plugin = this;
		//registerEvents(this, new WhateverHandler());
		cf = new ConfigHandler();
		cf.regenerateAllMissingConfigs();
		getCommand("adddonation").setExecutor(new cmdAddDonation());
	}
	
	public void onDisable() {
		plugin = null;
	}
	
	public static Plugin getPlugin() {
		return plugin;
	}
	
	public static void registerEvents(Plugin plugin, Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
		}
	}
	
}
