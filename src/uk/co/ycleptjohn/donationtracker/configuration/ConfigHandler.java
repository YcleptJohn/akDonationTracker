package uk.co.ycleptjohn.donationtracker.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;

public class ConfigHandler {
	//private Plugin plugin = VoteShop.getPlugin();
	
	public ConfigHandler() {
	}
	
	public ConfigFile get(Config config) {
		return config.getConfigFile();
	}
	
	public File getFile(Config config) {
		return config.getConfigFile().getFile();
	}
	

	public void regenerateConfig(Config configToReset) {
		configToReset.getConfigFile().generateDefault();
	}
	
	public void regenerateAllConfigs() {
		for(Config c : Config.values()) {
			c.getConfigFile().generateDefault();
		}
	}
	
	public void regenerateAllConfigsIfMissing() {
		for(Config c : Config.values()) {
			ConfigFile cf = c.getConfigFile();
			try {
				cf.load(cf.getFile());
			} catch (FileNotFoundException e) {
				c.getConfigFile().generateDefault();
			} catch (InvalidConfigurationException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//TODO Get all config fields based on config name param
	//TODO flesh out functional methods; save etc
	
}
