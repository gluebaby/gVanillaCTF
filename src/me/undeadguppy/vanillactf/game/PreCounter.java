package me.undeadguppy.vanillactf.game;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class PreCounter extends BukkitRunnable {

	private Game game;

	public PreCounter(Game game) {
		this.game = game;
	}

	private int counter = 10 * 60;

	@Override
	public void run() {

		if (!game.isRunning()) {
			cancel();
			return;
		}
		if (counter == 5 * 60) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "There are 5 minutes left in the grace period.");
		}
		if (counter == 1 * 60) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "There are 60 seconds left in the grace period.");

		}
		if (counter == 30) {
			Bukkit.getServer().broadcastMessage(ChatColor.RED + "There are 30 seconds left in the grace period.");
		}

		if (counter <= 10) {
			Bukkit.getServer()
					.broadcastMessage(ChatColor.RED + "There are " + counter + " seconds left in the grace period.");
		}

		if (counter <= 0) {
			Bukkit.getServer()
					.broadcastMessage(ChatColor.RED + "The grace period has ended! 20 minutes remain in the match.");
			cancel();
			game.enterFightPhase();
			return;
		}

		counter--;

	}

}
