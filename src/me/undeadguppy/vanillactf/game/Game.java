package me.undeadguppy.vanillactf.game;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.undeadguppy.vanillactf.VanillaCTF;
import me.undeadguppy.vanillactf.teams.Team;
import me.undeadguppy.vanillactf.teams.TeamManager;
import me.undeadguppy.vanillactf.world.WorldManager;
import net.md_5.bungee.api.ChatColor;

public class Game {

	VanillaCTF core;
	GameManager gamemanager;
	TeamManager teammanager;
	WorldManager worldmanager;
	GameCounter gamecounter;

	public Game(VanillaCTF core, GameManager gamemanager, WorldManager worldmanager, TeamManager teammanager,
			GameCounter counter) {
		this.core = core;
		this.gamemanager = gamemanager;
		this.worldmanager = worldmanager;
		this.teammanager = teammanager;
		this.gamecounter = counter;
	}

	public VanillaCTF getCore() {
		return core;
	}

	public boolean isRunning() {
		return !gamemanager.getPhase().equals(GamePhase.PRE);
	}

	public GameManager getGameManager() {
		return gamemanager;
	}

	public TeamManager getTeamManager() {
		return teammanager;
	}

	public WorldManager getWorldManager() {
		return worldmanager;
	}

	public GameCounter getFightCounter() {
		return gamecounter;
	}

	private void enterFightPhase() {
		// Cancel forcefied
		gamemanager.setPhase(GamePhase.FIGHT);
		worldmanager.resetFFBlocks();
		gamecounter.runTaskTimer(core, 0L, 20L * 60);
		// Started game timer^^

	}

	private void populateTeams() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (teammanager.getTeamSize(Team.AARDVARK) >= teammanager.getTeamSize(Team.BADGER)) {
				teammanager.setTeam(player, Team.AARDVARK);
			} else if (teammanager.getTeamSize(Team.BADGER) <= teammanager.getTeamSize(Team.AARDVARK)) {
				teammanager.setTeam(player, Team.BADGER);
			}
		}
	}

	public void begin() {
		// check if world is resetting
		if (!worldmanager.isResetting()) {
			// teleport players
			populateTeams();
			worldmanager.generateFlagTowers();
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if (teammanager.getTeam(p).equals(Team.BADGER)) {
					p.teleport(worldmanager.getBSpawn());
					p.setHealth(20);
					p.setFoodLevel(20);
					p.setGameMode(GameMode.SURVIVAL);
					p.setInvulnerable(false);
				} else if (teammanager.getTeam(p).equals(Team.AARDVARK)) {
					p.teleport(worldmanager.getASpawn());
					p.setHealth(20);
					p.setFoodLevel(20);
					p.setGameMode(GameMode.SURVIVAL);
					p.setInvulnerable(false);
				} else if (teammanager.getTeam(p).equals(Team.SPECTATOR)) {
					p.teleport(worldmanager.getCenter());
					p.setGameMode(GameMode.SPECTATOR);
				}
			}
			worldmanager.setupForcefield();
			Bukkit.getServer().broadcastMessage(ChatColor.RED
					+ "The games have begun! You have a 10 minutes to gather resources and fortify your flag.");
			gamemanager.setPhase(GamePhase.PREPARE);
			new BukkitRunnable() {
				private int counter = 10 * 60;

				@Override
				public void run() {
					if (counter == 5 * 60) {
						Bukkit.getServer()
								.broadcastMessage(ChatColor.RED + "There are 5 minutes left in the grace period.");
					}
					if (counter == 1 * 60) {
						Bukkit.getServer()
								.broadcastMessage(ChatColor.RED + "There are 60 seconds left in the grace period.");

					}
					if (counter == 30) {
						Bukkit.getServer()
								.broadcastMessage(ChatColor.RED + "There are 30 seconds left in the grace period.");
					}

					if (counter <= 10) {
						Bukkit.getServer().broadcastMessage(
								ChatColor.RED + "There are " + counter + " seconds left in the grace period.");
					}

					if (counter <= 0) {
						Bukkit.getServer().broadcastMessage(
								ChatColor.RED + "The grace period has ended! 20 minutes remain in the match.");
						cancel();
						enterFightPhase();
						return;
					}

					counter--;

				}

			}.runTaskTimer(core, 0L, 20L);
		}
	}

	public void end() {
		gamemanager.setPhase(GamePhase.POST);
		if (!gamecounter.isCancelled())
			gamecounter.cancel();

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.setInvulnerable(true);
			p.setGameMode(GameMode.ADVENTURE);
			p.getInventory().clear();
			p.setHealth(20);
			p.setFoodLevel(20);
			teammanager.reset();
			if (teammanager.getTeam(p).equals(teammanager.getWinner())) {
				new BukkitRunnable() {

					int counter = 10;

					@Override
					public void run() {
						if (counter <= 0) {
							cancel();
							worldmanager.resetWorld();
						}
						p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
						counter--;

					}
				}.runTaskTimer(core, 0L, 40L);

			}
		}
	}

}
