package me.undeadguppy.vanillactf.world;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import me.undeadguppy.vanillactf.game.Game;
import me.undeadguppy.vanillactf.game.GamePhase;
import me.undeadguppy.vanillactf.teams.Team;
import net.md_5.bungee.api.ChatColor;

public class DropTimer extends BukkitRunnable {

	private Game game;
	private Team team;
	int x;
	int y;
	int z;

	public DropTimer(Team team, Game game) {
		this.game = game;
		this.team = team;
		x = game.getWorldManager().getFlagLocation(team).getBlockX();
		y = game.getWorldManager().getFlagLocation(team).getBlockY();
		z = game.getWorldManager().getFlagLocation(team).getBlockZ();

	}

	@Override
	public void run() {

		if (game.getGameManager().getPhase() == GamePhase.POST) {
			cancel();
		}

		if (!game.getWorldManager().isFlagDropped(team)) {
			// Flag no longer dropped
			cancel();
			return;
		}

		Bukkit.getServer().broadcastMessage(
				ChatColor.RED + team.getName() + "'s flag is dropped at (" + x + ", " + y + ", " + z + ")");
	}
}
