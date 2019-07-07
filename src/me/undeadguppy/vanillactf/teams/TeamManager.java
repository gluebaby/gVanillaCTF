package me.undeadguppy.vanillactf.teams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import me.undeadguppy.vanillactf.game.Game;
import net.md_5.bungee.api.ChatColor;

public class TeamManager {

	private HashMap<UUID, Team> teams;
	private HashSet<UUID> aTeam;
	private HashSet<UUID> bTeam;
	private HashMap<Team, Integer> captures;
	private HashMap<UUID, Integer> lives;
	private Game game;

	public TeamManager(Game game) {
		this.game = game;
		this.aTeam = new HashSet<UUID>();
		this.bTeam = new HashSet<UUID>();
		this.teams = new HashMap<UUID, Team>();
		this.captures = new HashMap<Team, Integer>();
		this.lives = new HashMap<UUID, Integer>();
		captures.put(Team.AARDVARK, 0);
		captures.put(Team.BADGER, 0);
	}

	public void reset() {
		this.teams.clear();
		this.captures.clear();
		this.lives.clear();
		this.aTeam.clear();
		this.bTeam.clear();
	}

	public int getTeamSize(Team team) {
		switch (team) {
		case AARDVARK:
			return aTeam.size();
		case BADGER:
			return bTeam.size();
		default:
			return 0;
		}
	}

	public Set<UUID> getPlayers() {

		return teams.keySet();
	}

	public int getLives(Player player) {
		return lives.get(player.getUniqueId());
	}

	public void setTeam(Player player, Team team) {
		if (!hasTeam(player) && !lives.containsKey(player.getUniqueId()))
			this.teams.put(player.getUniqueId(), team);
		switch (team) {
		case AARDVARK:
			this.aTeam.add(player.getUniqueId());
		case BADGER:
			this.bTeam.add(player.getUniqueId());
		default:
			break;
		}
		this.lives.put(player.getUniqueId(), 3);

	}

	public void removeLife(Player player) {
		if (hasTeam(player) && lives.containsKey(player.getUniqueId())) {
			if (lives.get(player.getUniqueId()) - 1 <= 0) {
				// remove player
				lives.remove(player.getUniqueId());
				setSpectator(player);
				Bukkit.getServer().broadcastMessage(ChatColor.RED + player.getName() + " has been eliminated!");
				return;
			}

			lives.put(player.getUniqueId(), lives.get(player.getUniqueId()) - 1);
			player.sendMessage(
					ChatColor.RED + "You have lost a life! " + lives.get(player.getUniqueId()) + "/3 lives remaining.");

		}

	}

	public void setSpectator(Player player) {
		if (hasTeam(player)) {
			teams.remove(player.getUniqueId());
		}
		teams.put(player.getUniqueId(), Team.SPECTATOR);
		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(game.getWorldManager().getCenter());
	}

	public Team getTeam(Player player) {
		return this.teams.get(player.getUniqueId());
	}

	public boolean areAllies(Player player, Player player2) {
		return this.teams.get(player.getUniqueId()).equals(this.teams.get(player2.getUniqueId()));
	}

	public boolean hasTeam(Player player) {
		return this.teams.containsKey(player.getUniqueId());
	}

	public int getCaptures(Team team) {
		if (!captures.containsKey(team)) {
			return 0;
		}
		return captures.get(team);
	}

	public void addCapture(Team team) {
		if (captures.containsKey(team) && captures.get(team) < 1) {
			captures.put(team, captures.get(team) + 1);
		}
	}

	public boolean isDraw() {
		return captures.get(Team.AARDVARK) == captures.get(Team.BADGER);
	}

	public Team getWinner() {
		if (captures.get(Team.AARDVARK) > captures.get(Team.BADGER)) {
			return Team.AARDVARK;
		}
		if (captures.get(Team.BADGER) > captures.get(Team.AARDVARK)) {
			return Team.BADGER;
		}
		return null;

	}

}
