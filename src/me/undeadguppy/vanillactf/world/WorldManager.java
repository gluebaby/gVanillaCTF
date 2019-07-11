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

import com.boydti.fawe.util.task.TaskBuilder;

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
	private Location aFlag;
	private Location bFlag;
	private World voidWorld;
	private Cuboid forcefield;
	private boolean resetting;
	private VanillaCTF ctf;
	private HashMap<Team, Boolean> flagDropped;

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
		bFlag = new Location(Bukkit.getWorld("world"), 500, 100, 250);
		aFlag = new Location(Bukkit.getWorld("world"), 500, 100, 750);
		voidWorld = Bukkit.getServer().getWorld("limbo");
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
			setFlagLocation(Team.BADGER, new Location(Bukkit.getWorld("world"), 500, 100, 250));
			setFlagLocation(Team.AARDVARK, new Location(Bukkit.getWorld("world"), 500, 100, 750));
			getFlagLocation(Team.AARDVARK).getBlock().setType(Material.NETHERRACK);
			getFlagLocation(Team.BADGER).getBlock().setType(Material.SOUL_SAND);
			setFlagDropped(Team.AARDVARK, false);
			setFlagDropped(Team.BADGER, false);
		}
	}

	public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public void setupWorldBorder() {
		if (Bukkit.getWorld("world") != null) {
			Bukkit.getWorld("world").getWorldBorder().reset();
			Bukkit.getWorld("world").getWorldBorder().setCenter(500, 500);
			Bukkit.getWorld("world").getWorldBorder().setSize(550);
			Bukkit.getWorld("world").getWorldBorder().setDamageAmount(0.0);
			Bukkit.getWorld("world").getWorldBorder().setDamageBuffer(0.0);

		}
	}

	public Cuboid getForcefield() {
		return forcefield;
	}

	public void setupForcefield() {
		forcefield = new Cuboid("world", 275.0, 0.0, 501.0, 725.0, 256.0, 499.0);
		new TaskBuilder().syncWhenFree(new TaskBuilder.SplitTask(20) {

			@Override
			public Object exec(Object arg0) {
				for (Block block : forcefield) {
					if (block.getType() == Material.AIR || block.getType() == Material.WATER) {
						block.setType(Material.BARRIER);
						split();
					}
				}
				return null;
			}
		}).build();
	}

	public void removeForcefield() {
		if (forcefield != null) {
			new TaskBuilder().syncWhenFree(new TaskBuilder.SplitTask(20) {

				@Override
				public Object exec(Object arg0) {
					for (Block block : forcefield) {
						if (block.getType() == Material.BARRIER) {
							block.setType(Material.AIR);
							split();
						}
					}
					return null;
				}
			}).build();
		}
	}

	public void resetWorld() {
		if (!isResetting()) {
			resetting = true;
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.teleport(new Location(Bukkit.getServer().createWorld(new WorldCreator("limbo")), 0, 0, 0));
				player.setInvulnerable(true);
				player.getInventory().clear();
				player.setHealth(20);
				player.setFoodLevel(20);
			}
			if (Bukkit.getWorld("world") != null) {
				// unload world
				Bukkit.getServer().unloadWorld(Bukkit.getWorld("world"), true);
				// delete world
				deleteDirectory(Bukkit.getWorld("world").getWorldFolder());
			}
			// Create new world
			Bukkit.getServer().createWorld(new WorldCreator("world"));
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

	// Bflag is at 500, 100, 250
	// Aflag is at 500, 100, 750

	public void recoverFlag(Team team) {
		if (isFlagDropped(team)) {
			switch (team) {
			case AARDVARK:
				setFlagDropped(team, false);
				setFlagLocation(team, new Location(Bukkit.getWorld("world"), 500, 100, 750));
				Bukkit.getWorld("world").getBlockAt(new Location(Bukkit.getWorld("world"), 500, 100, 750))
						.setType(Material.NETHERRACK);
				break;
			case BADGER:
				setFlagDropped(team, false);
				setFlagLocation(team, new Location(Bukkit.getWorld("world"), 500, 100, 250));
				Bukkit.getWorld("world").getBlockAt(new Location(Bukkit.getWorld("world"), 500, 100, 250))
						.setType(Material.SOUL_SAND);
				break;
			default:

			}
		}
	}
}
