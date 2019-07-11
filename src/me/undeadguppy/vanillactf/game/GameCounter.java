package me.undeadguppy.vanillactf.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class GameCounter extends BukkitRunnable {

	private int counter;
	private Game game;

	public GameCounter(Game game) {
		this.game = game;
		this.counter = 20;
	}

	@Override
	public void run() {

		if (game.getGameManager().getPhase() == GamePhase.POST) {
			cancel();
		}

		if (counter == 15) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "15 minutes remain in the game.");
		}

		if (counter == 10) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "10 minutes remain in the game.");
		}

		if (counter == 5) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "5 minutes remain in the game.");
		}

		if (counter == 1) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "1 minute remains in the game. Hurry!");
		}

		if (counter <= 0) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "The game is over! Calculating results...");
			if (game.getTeamManager().isDraw()) {
				Bukkit.getServer().broadcastMessage(ChatColor.RED + "It was a draw!");
			}
			game.end();
			this.cancel();
			return;
		}
	}

}
