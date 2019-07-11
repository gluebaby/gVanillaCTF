package me.undeadguppy.vanillactf;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import me.undeadguppy.vanillactf.game.Game;
import me.undeadguppy.vanillactf.game.GameCounter;
import me.undeadguppy.vanillactf.game.GameManager;
import me.undeadguppy.vanillactf.game.PreCounter;
import me.undeadguppy.vanillactf.listeners.PlayerListeners;
import me.undeadguppy.vanillactf.teams.Team;
import me.undeadguppy.vanillactf.teams.TeamManager;
import me.undeadguppy.vanillactf.world.WorldManager;

public class VanillaCTF extends JavaPlugin {
//A = NETHERRACK
//B = SOULSAND
	private Game game;

	public Game getGame() {
		return game;
	}

	@Override
	public void onEnable() {

		this.game = new Game(this, new GameManager(), new WorldManager(this), new TeamManager(game),
				new GameCounter(game), new PreCounter(game));
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListeners(game), this);

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("begin")) {
			if (sender.getName().equals("Undead_Guppy")) {
				if (game.isRunning()) {
					sender.sendMessage("Game's running.");
					return true;
				}
				game.begin();

			} else {
				sender.sendMessage("No.");
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("list")) {
			StringBuilder sb = new StringBuilder();
			for (UUID id : game.getTeamManager().getPlayers()) {
				if (!game.getTeamManager().getTeam(Bukkit.getServer().getPlayer(id)).equals(Team.SPECTATOR)) {
					if (game.getTeamManager().getTeam(Bukkit.getServer().getPlayer(id)).equals(Team.AARDVARK)) {
						sb.append(Bukkit.getServer().getPlayer(id).getName() + "(A), ");
					} else {
						sb.append(Bukkit.getServer().getPlayer(id).getName() + "(B), ");
					}
				}
			}
			String list = sb.toString();
			sender.sendMessage("PLAYERS:");
			sender.sendMessage(list);
		} else if (cmd.getName().equalsIgnoreCase("cancel")) {
			if (sender.getName().equals("Undead_Guppy")) {
				if (!game.isRunning()) {
					sender.sendMessage("Game's Not running.");
					return true;
				}
				game.end();

			} else {
				sender.sendMessage("No.");
				return true;
			}
		}

		return true;
	}

}
