package me.undeadguppy.vanillactf.game;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
	PreCounter precounter;

	public Game(VanillaCTF core, GameManager gamemanager, WorldManager worldmanager, TeamManager teammanager,
			GameCounter counter, PreCounter precounter) {
		this.core = core;
		this.gamemanager = gamemanager;
		this.worldmanager = worldmanager;
		this.teammanager = teammanager;
		this.gamecounter = counter;
		this.precounter = precounter;
	}

	public VanillaCTF getCore() {
		return core;
	}

	public boolean isRunning() {
		return gamemanager.getPhase() != GamePhase.PRE || gamemanager.getPhase() != GamePhase.POST;
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

	public void enterFightPhase() {
		// Cancel forcefied
		worldmanager.removeForcefield();
		gamemanager.setPhase(GamePhase.FIGHT);
		gamecounter.runTaskTimer(core, 0L, 20L * 60);
		// Started game timer^^

	}

	private void populateTeams() {
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (teammanager.getTeamSize(Team.AARDVARK) >= teammanager.getTeamSize(Team.BADGER)) {
				Bukkit.getLogger().info("VCTF - DEBUG: " + player.getName() + " is in AARDVARK");
				teammanager.setTeam(player, Team.AARDVARK);
			} else if (teammanager.getTeamSize(Team.BADGER) <= teammanager.getTeamSize(Team.AARDVARK)) {
				Bukkit.getLogger().info("VCTF - DEBUG: " + player.getName() + " is in BADGER");

				teammanager.setTeam(player, Team.BADGER);
			}
		}
	}

	public void begin() {
		// check if world is resetting
		if (!worldmanager.isResetting() && !isRunning()) {
			// teleport players
			populateTeams();
			worldmanager.setupWorldBorder();
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
					p.teleport(new Location(Bukkit.getWorld("world"), 500, 100, 505));
					p.setGameMode(GameMode.SPECTATOR);
				}
			}
			worldmanager.setupForcefield();
			Bukkit.getServer().broadcastMessage(ChatColor.RED
					+ "The games have begun! You have a 10 minutes to gather resources and fortify your flag.");
			gamemanager.setPhase(GamePhase.PREPARE);
			precounter.runTaskTimer(core, 0L, 20L);
		}
	}

	public void end() {
		gamemanager.setPhase(GamePhase.POST);
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.setInvulnerable(true);
			p.setGameMode(GameMode.ADVENTURE);
			p.getInventory().clear();
			p.setHealth(20);
			p.setFoodLevel(20);
			teammanager.reset();
			worldmanager.reset();
			if (teammanager.isDraw()) {
				Bukkit.broadcastMessage(ChatColor.RED + "Draw! Nobody wins!");
			} else {
				Bukkit.broadcastMessage(ChatColor.RED + teammanager.getWinner().getName() + " has won the game!");
			}
			worldmanager.resetWorld();

		}
	}
}
