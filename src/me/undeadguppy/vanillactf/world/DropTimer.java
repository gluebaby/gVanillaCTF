package me.undeadguppy.vanillactf.world;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import me.undeadguppy.vanillactf.game.Game;
import me.undeadguppy.vanillactf.teams.Team;
import net.md_5.bungee.api.ChatColor;

public class DropTimer extends BukkitRunnable {

	private Game game;
	private Team team;

	public DropTimer(Team team, Game game) {
		this.game = game;
		this.team = team;
	}

	int x = game.getWorldManager().getFlagLocation(team).getBlockX();
	int y = game.getWorldManager().getFlagLocation(team).getBlockY();
	int z = game.getWorldManager().getFlagLocation(team).getBlockZ();

	@Override
	public void run() {

		if (game.getWorldManager().getFlagLocation(Team.BADGER) == new Location(Bukkit.getWorld("world"), 500, 100, 250)
				|| game.getWorldManager().getFlagLocation(Team.AARDVARK) == new Location(Bukkit.getWorld("world"), 500,
						100, 750)) {
			cancel();
			return;
		}

		Bukkit.getServer().broadcastMessage(
				ChatColor.RED + team.getName() + "'s flag is dropped at (" + x + ", " + y + ", " + z + ")");
	}
}
