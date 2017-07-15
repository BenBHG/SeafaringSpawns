package com.boothousegaming.seafaringspawns;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class SeafaringSpawns extends JavaPlugin implements Listener {
	
	@Override
	public void onEnable() {
		FileConfiguration config;
		
		config = getConfig();
		
		saveDefaultConfig();
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		
	}

}
