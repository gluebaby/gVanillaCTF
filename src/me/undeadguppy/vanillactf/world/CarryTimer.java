package me.undeadguppy.vanillactf.world;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.undeadguppy.vanillactf.game.Game;
import me.undeadguppy.vanillactf.teams.Team;
import net.md_5.bungee.api.ChatColor;

public class CarryTimer extends BukkitRunnable {

	private Player player;
	private Team team;
	private Game game;

	public CarryTimer(Player player, Team team, Game game) {
		this.player = player;
		this.team = team;
		this.game = game;
	}

	@Override
	public void run() {
		if (!game.isRunning()) {
			cancel();
		}

		switch (team) {
		case BADGER:
			if (!player.getInventory().contains(new ItemStack(Material.SOUL_SAND))) {
				cancel();
				break;
			}
		case AARDVARK:
			if (!player.getInventory().contains(new ItemStack(Material.NETHERRACK))) {
				cancel();
				break;
			}
		default:
			break;
		}

		Bukkit.getServer()
				.broadcastMessage(ChatColor.RED + player.getName() + " is carrying " + team.getName() + "'s flag!");
		player.damage(1);
		player.getWorld().strikeLightningEffect(player.getLocation());

	}

}
