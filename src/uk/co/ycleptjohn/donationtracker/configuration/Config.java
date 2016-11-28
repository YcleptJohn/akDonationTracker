package uk.co.ycleptjohn.donationtracker.configuration;

import java.io.File;

import org.bukkit.plugin.Plugin;

import uk.co.ycleptjohn.donationtracker.DonationTracker;

public enum Config {
	GENERAL("general", "general.yml"),
	PERKS("perks", "perks.yml"),
	MESSAGES("messages", "messages.yml");
	
	private Plugin plugin = DonationTracker.getPlugin();
	private String name;
	private ConfigFile conf;
	
	Config(String configName, String fileName) {
		name = configName;
		conf = new ConfigFile(new File(plugin.getDataFolder(), fileName), fileName);
	}
	
	public ConfigFile getConfigFile() {
		return conf;
	}
	
	public String getName() {
		return name;
	}
}
