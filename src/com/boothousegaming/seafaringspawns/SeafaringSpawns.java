package com.boothousegaming.seafaringspawns;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class SeafaringSpawns extends JavaPlugin implements Listener {
	
	double cowChance;
	double creeperChance;
	double endermanChance;
	double pigChance;
	double sheepChance;
	double skeletonChance;
	double zombieChance;
	
	@Override
	public void onEnable() {
		FileConfiguration config;
		
		config = getConfig();
		ConfigurationSection chanceSection = config.getConfigurationSection("chance");
		cowChance = chanceSection.getDouble("cow");
		creeperChance = chanceSection.getDouble("creeper");
		endermanChance = chanceSection.getDouble("enderman");
		pigChance = chanceSection.getDouble("pig");
		sheepChance = chanceSection.getDouble("sheep");
		skeletonChance = chanceSection.getDouble("skeleton");
		zombieChance = chanceSection.getDouble("zombie");
		
		saveDefaultConfig();
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		double chance;
		switch (event.getEntityType()) {
		case COW: {
			chance = cowChance;
			break;
		}
		case CREEPER: {
			chance = creeperChance;
			break;
		}
		case ENDERMAN: {
			chance = endermanChance;
			break;
		}
		case PIG: {
			chance = pigChance;
			break;
		}
		case SHEEP: {
			chance = sheepChance;
			break;
		}
		case SKELETON: {
			chance = skeletonChance;
			break;
		}
		case ZOMBIE: {
			chance = zombieChance;
			break;
		}
		default:
			return;
		}
		if (chance >= Math.random())
			event.getEntity().setMetadata("seafaring", new FixedMetadataValue(this, true));
	}

}
