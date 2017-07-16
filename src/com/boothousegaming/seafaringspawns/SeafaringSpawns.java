package com.boothousegaming.seafaringspawns;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class SeafaringSpawns extends JavaPlugin implements Listener, CommandExecutor {
	
	double cowChance;
	double creeperChance;
	double endermanChance;
	double pigChance;
	double sheepChance;
	double skeletonChance;
	double zombieChance;
	long seafaringTaskPeriod;
	int seafaringTaskId;
	
	@Override
	public void onEnable() {
		FileConfiguration config;
		
		config = getConfig();
		ConfigurationSection chanceSection = config.getConfigurationSection("chance");
		skeletonChance = chanceSection.getDouble("skeleton");
		seafaringTaskPeriod = config.getLong("task-period", 50L);
		
		saveDefaultConfig();
		
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("sfstest").setExecutor(this);
		
		seafaringTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			
			public void run() {
				for (World world: getServer().getWorlds()) {
					for (LivingEntity livingEntity: world.getLivingEntities()) {
						if (livingEntity.isDead())
							continue;
						if (livingEntity.getMetadata("seafaring").isEmpty())
							continue;
						if (!livingEntity.isInsideVehicle()) {
							if (!livingEntity.getEyeLocation().getBlock().isEmpty())
								continue;
							Location location = livingEntity.getLocation();
							switch (location.getBlock().getType()) {
							case WATER:
							case STATIONARY_WATER: {
								Boat boat = world.spawn(location.add(0.0, 2.0, 0.0), Boat.class);
								if (!boat.addPassenger(livingEntity)) {
									boat.remove();
								}
							}
							default:
								break;
							}
						}
					}
				}
			}
			
		}, 0L, seafaringTaskPeriod);
	}
	
	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTask(seafaringTaskId);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("sfstest")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;
				Skeleton sfMob = player.getWorld().spawn(player.getLocation(), Skeleton.class);
				sfMob.setMetadata("seafaring", new FixedMetadataValue(this, true));
				sfMob.setCustomName("SFSTEST");
				sfMob.setCustomNameVisible(true);
			}
		}
		return true;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		double chance;
		switch (event.getEntityType()) {
		case SKELETON: {
			chance = skeletonChance;
			break;
		}
		default:
			return;
		}
		if (chance >= Math.random())
			event.getEntity().setMetadata("seafaring", new FixedMetadataValue(this, true));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		LivingEntity livingEntity = event.getEntity();
		if (livingEntity.getMetadata("seafaring").isEmpty())
			return;
		Entity vehicle = livingEntity.getVehicle();
		if (vehicle == null)
			return;
		if (!vehicle.getType().equals(EntityType.BOAT))
			return;
		vehicle.remove();
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (!vehicle.getType().equals(EntityType.BOAT))
			return;
		if (vehicle.isEmpty())
			return;
		Location location = event.getBlock().getLocation().add(0.0, 1.0, 0.0);
		if (!location.getBlock().isEmpty())
			return;
		boolean removeBoat = false;
		for (Entity entity: vehicle.getPassengers()) {
			if (entity.getMetadata("seafaring").isEmpty())
				continue;
			if (entity.teleport(location, TeleportCause.PLUGIN))
				removeBoat = true;
		}
		if (removeBoat)
			vehicle.remove();
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onVehicleExit(VehicleExitEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (!vehicle.getType().equals(EntityType.BOAT))
			return;
		if (!vehicle.isEmpty())
			return;
		LivingEntity livingEntity = event.getExited();
		if (livingEntity.getMetadata("seafaring").isEmpty())
			return;
		vehicle.remove();
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onVehicleUpdate(VehicleUpdateEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (!vehicle.getType().equals(EntityType.BOAT))
			return;
		if (vehicle.isEmpty())
			return;
		for (Entity entity: vehicle.getPassengers()) {
			if (entity.getMetadata("seafaring").isEmpty())
				continue;
			Location entityLocation = entity.getLocation();
			Location boatLocation = vehicle.getLocation();
			final double chanceChangeDir = 0.1;
			if (Math.random() < chanceChangeDir) {
				// using facing direction to read ai
				Vector direction = entityLocation.getDirection();
				if (!(entity instanceof Monster) || ((Monster)entity).getTarget() == null) {
					// by default ai not looking left or right when not targeting
					// this is giving that some randomness
					float angle = (float)Math.atan2(direction.getZ(), direction.getX()) + (float)Math.random() * (float)Math.PI / 4.0f - (float)Math.PI / 8.0f;
					direction.setX(Math.cos(angle));
					direction.setZ(Math.sin(angle));
					entityLocation.setDirection(direction);
				}
				boatLocation.setDirection(direction.setY(0.0));
				// hack using teleports to steer the boat
				entity.teleport(entityLocation, TeleportCause.PLUGIN);
				vehicle.teleport(boatLocation, TeleportCause.PLUGIN);
				vehicle.addPassenger(entity);
			}
			final double speed = 0.1;
			Vector velocity = vehicle.getVelocity();
			velocity.setX(vehicle.getLocation().getDirection().getX() * speed);
			velocity.setZ(vehicle.getLocation().getDirection().getZ() * speed);
			vehicle.setVelocity(velocity);
			break;
		}
	}

}
