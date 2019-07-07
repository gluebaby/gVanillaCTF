package me.undeadguppy.vanillactf.world;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.undeadguppy.vanillactf.VanillaCTF;
import me.undeadguppy.vanillactf.teams.Team;
import net.md_5.bungee.api.ChatColor;

public class WorldManager {

	// ForceField is 100 Blocks Thick.
	// FF Edges are 450 Z and 550 Z
	// Bflag is at 500, 100, 250
	// Aflag is at 500, 100, 750
	// World Border Size is 550
	// World Center is (500, 500)

	private final Location bSpawn;
	private final Location aSpawn;
	private final Location worldCenter;
	private Location aFlag;
	private Location bFlag;
	private World voidWorld;
	private Cuboid forcefieldA;
	private Cuboid forcefieldB;
	private boolean resetting;
	private VanillaCTF ctf;
	private HashMap<Team, Boolean> flagDropped;
	private HashMap<Location, Material> ffBlocks;

	public Location getSpawnLocation(World world, int x, int z) {
		int i = 255;
		while (i > 0) {
			if (new Location(world, x, i, z).getBlock().getType() != Material.AIR)
				return new Location(world, x, i, z).add(0, 1, 0);
			i--;
		}
		return new Location(world, x, 1, z);
	}

	public void reset() {
		bFlag = new Location(Bukkit.getWorld("world"), 500, 100, 250);
		aFlag = new Location(Bukkit.getWorld("world"), 500, 100, 750);
		flagDropped.clear();
	}

	public WorldManager(VanillaCTF core) {
		this.ctf = core;
		bSpawn = getSpawnLocation(Bukkit.getWorld("world"), 490, 250);
		aSpawn = getSpawnLocation(Bukkit.getWorld("world"), 490, 750);
		worldCenter = getSpawnLocation(Bukkit.getWorld("world"), 0, 0);
		bFlag = new Location(Bukkit.getWorld("world"), 500, 100, 250);
		aFlag = new Location(Bukkit.getWorld("world"), 500, 100, 750);
		voidWorld = Bukkit.getWorld("limbo");
		this.ffBlocks = new HashMap<Location, Material>();
		this.flagDropped = new HashMap<Team, Boolean>();
		flagDropped.put(Team.AARDVARK, false);
		flagDropped.put(Team.BADGER, false);
		resetting = false;
	}

	public void setFlagDropped(Team team, boolean dropped) {
		flagDropped.put(team, dropped);
	}

	public boolean isFlagDropped(Team team) {
		return flagDropped.containsKey(team) && flagDropped.get(team);
	}

	public Location getFlagLocation(Team team) {
		switch (team) {
		case BADGER:
			return bFlag;
		case AARDVARK:
			return aFlag;
		default:
			return null;
		}
	}

	public void setFlagLocation(Team team, Location location) {
		switch (team) {
		case BADGER:
			this.bFlag = location;
			break;
		case AARDVARK:
			this.aFlag = location;
			break;
		default:
			break;
		}
	}

	public Location getBSpawn() {
		return bSpawn;
	}

	public Location getASpawn() {
		return aSpawn;
	}

	public Location getCenter() {
		return worldCenter;
	}

	public World getLimbo() {
		return voidWorld;
	}

	public boolean isResetting() {
		return resetting;
	}

	public void generateFlagTowers() {
		if (!isResetting()) {
			// spawn in tower
			for (double i = 0; i < 99; i++) {
				// place blocks at coords
				// bFlag = new Location(Bukkit.getWorld("world"), 500, 100, 250);
				// aFlag = new Location(Bukkit.getWorld("world"), 500, 100, 750);
				Location bTower = getSpawnLocation(Bukkit.getWorld("world"), 500, 250);
				Location aTower = getSpawnLocation(Bukkit.getWorld("world"), 500, 750);
				bTower.getBlock().setType(Material.GLOWSTONE);
				aTower.getBlock().setType(Material.GLOWSTONE);
				aTower.setY(i);
				bTower.setY(i);

			}
			getFlagLocation(Team.AARDVARK).getBlock().setType(Material.NETHERRACK);
			getFlagLocation(Team.BADGER).getBlock().setType(Material.SOUL_SAND);
		}
	}

	private boolean deleteWorld(File path) {
		if (path.exists()) {
			File files[] = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteWorld(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private void setupWorldBorder() {
		if (Bukkit.getWorld("world") != null) {
			Bukkit.getWorld("world").getWorldBorder().reset();
			Bukkit.getWorld("world").getWorldBorder().setCenter(500, 500);
			Bukkit.getWorld("world").getWorldBorder().setSize(550);
			Bukkit.getWorld("world").getWorldBorder().setDamageAmount(0.0);
			Bukkit.getWorld("world").getWorldBorder().setDamageBuffer(0.0);

		}
	}

	public Cuboid getForcefieldA() {
		return forcefieldA;
	}

	public Cuboid getForcefieldB() {
		return forcefieldB;
	}

	public void setupForcefield() {
		forcefieldA = new Cuboid("world", 275.0, 0.0, 550.0, 725.0, 256.0, 550.0);
		forcefieldB = new Cuboid("world", 275.0, 0.0, 450.0, 725.0, 256.0, 450.0);

		for (Block block : forcefieldA) {
			ffBlocks.put(block.getLocation(), block.getType());
			if (block.getType() == Material.AIR || block.getType() == Material.WATER) {
				block.setType(Material.BARRIER);
			}
		}

		for (Block block : forcefieldB) {
			ffBlocks.put(block.getLocation(), block.getType());
			if (block.getType() == Material.AIR || block.getType() == Material.WATER) {
				block.setType(Material.BARRIER);
			}
		}

	}

	public void resetFFBlocks() {
		for (Location loc : ffBlocks.keySet()) {
			loc.getBlock().setType(ffBlocks.get(loc));

		}
	}

	public void resetWorld() {
		if (!isResetting()) {
			resetting = true;
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.teleport(new Location(Bukkit.getWorld("voidWorld"), 0, 0, 0));
				player.setInvulnerable(true);
				player.getInventory().clear();
				player.setHealth(20);
				player.setFoodLevel(20);
			}
			if (Bukkit.getWorld("world") != null) {
				// unload world
				Bukkit.getServer().unloadWorld(Bukkit.getWorld("world"), true);
				// delete world
				deleteWorld(Bukkit.getWorld("world").getWorldFolder());
			}
			// Create new world
			Bukkit.getServer().createWorld(new WorldCreator("world"));
			setupWorldBorder();
			new BukkitRunnable() {

				private int counter = 10;

				@Override
				public void run() {

					if (counter <= 0) {

						Bukkit.getServer().broadcastMessage(ChatColor.RED + "Teleporting!");
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							p.teleport(getSpawnLocation(Bukkit.getWorld("world"), 500, 500));
							p.setHealth(20);
							p.setFoodLevel(20);
							p.setInvulnerable(false);
							p.getInventory().clear();
							p.setGameMode(GameMode.ADVENTURE);
						}
						cancel();
						return;
					}

					Bukkit.getServer()
							.broadcastMessage(ChatColor.RED + "Teleporting to world in " + counter + " seconds...");
					counter--;

				}
			}.runTaskTimer(ctf, 0L, 20L);
			this.resetting = false;

		}
	}

	public void recoverFlag(Team team) {
		// TODO Auto-generated method stub

	}
}
