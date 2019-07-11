package me.undeadguppy.vanillactf.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import me.undeadguppy.vanillactf.game.Game;
import me.undeadguppy.vanillactf.game.GamePhase;
import me.undeadguppy.vanillactf.teams.Team;
import me.undeadguppy.vanillactf.world.CarryTimer;
import me.undeadguppy.vanillactf.world.DropTimer;

public class PlayerListeners implements Listener {

	private Game game;

	public PlayerListeners(Game game) {
		this.game = game;
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		// GAMEPHASE IS FIGHT
		if (game.getGameManager().getPhase() == GamePhase.FIGHT) {
			Player p = event.getPlayer();
			// PLACED BLOCK IS NOT FLAG
			if (event.getBlockPlaced().getType() != Material.SOUL_SAND
					|| event.getBlockPlaced().getType() != Material.NETHERRACK) {
				return;
			}
			// Placing against netherrack or soul sand.
			if (event.getBlockAgainst().getType() != Material.SOUL_SAND
					|| event.getBlockAgainst().getType() != Material.NETHERRACK) {
				// placing against non flag!
				event.getPlayer().sendMessage(ChatColor.RED + "You must place the enemy flag on your own!");
				event.setCancelled(true);
				return;
			}

			switch (event.getBlockPlaced().getType()) {
			case SOUL_SAND:
				// Aardvark wins
				if (game.getTeamManager().getTeam(event.getPlayer()) == Team.AARDVARK) {
					game.getTeamManager().addCapture(Team.AARDVARK);
					Bukkit.getServer().broadcastMessage(ChatColor.RED + p.getName() + " has captured Badger's flag!");
					game.end();
					break;
				}
			case NETHERRACK:
				// BADGER wins
				if (game.getTeamManager().getTeam(event.getPlayer()) == Team.BADGER) {
					game.getTeamManager().addCapture(Team.BADGER);
					Bukkit.getServer().broadcastMessage(ChatColor.RED + p.getName() + " has captured Aardvark's flag!");
					game.end();
					break;
				}
			default:
				break;

			}

		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
			if (game.getTeamManager().areAllies((Player) event.getEntity(), (Player) event.getDamager())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onFlag(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (block.getType() == Material.GLOWSTONE) {
			event.setCancelled(true);
			return;
		}

		if (game.getGameManager().getPhase() == GamePhase.PREPARE) {
			if (game.getWorldManager().getForcefield().containsLocation(block.getLocation())) {
				// IN FORCEFIELD
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks within the forcefield!");
				return;
			}
		}

		if (block.getType() != Material.NETHERRACK && block.getType() != Material.SOUL_SAND) {
			Bukkit.getLogger().info("interacted with non flag");
			return;
		}
		if (!player.getInventory().getItemInMainHand().getType().equals(Material.IRON_PICKAXE)) {
			player.sendMessage(ChatColor.RED + "You must use an Iron Pickaxe to mine the flag!");
			event.setCancelled(true);
			return;
		}
		if (block.getType() == (Material.NETHERRACK)) {
			switch (game.getTeamManager().getTeam(player)) {

			// ON AARDVARK TEAM
			case AARDVARK:
				event.setCancelled(true);
				break;
			case BADGER:
				event.getBlock().getLocation().getWorld().strikeLightningEffect(event.getBlock().getLocation());
				Bukkit.getServer().broadcastMessage(ChatColor.RED + player.getName() + " has mined Aardvark's flag!");
			default:
				break;
			}
		} else if (block.getType() == (Material.SOUL_SAND)) {
			switch (game.getTeamManager().getTeam(player)) {
			case BADGER:
				event.setCancelled(true);
				break;
			case AARDVARK:
				event.getBlock().getLocation().getWorld().strikeLightningEffect(event.getBlock().getLocation());
				Bukkit.getServer().broadcastMessage(ChatColor.RED + player.getName() + " has mined Badger's flag!");
				break;
			default:
				break;

			}
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		int x = event.getItemDrop().getLocation().getBlockX();
		int y = event.getItemDrop().getLocation().getBlockY();
		int z = event.getItemDrop().getLocation().getBlockZ();
		String loc = x + ", " + y + ", " + z;
		if (event.getItemDrop().getItemStack().getType().equals(Material.SOUL_SAND)) {
			game.getWorldManager().setFlagDropped(Team.BADGER, true);
			game.getWorldManager().setFlagLocation(Team.BADGER, event.getItemDrop().getLocation());
			Bukkit.getServer().broadcastMessage(
					ChatColor.RED + event.getPlayer().getName() + " has dropped Badger's flag at (" + loc + ")!");
			Bukkit.getServer().getWorld("world").strikeLightningEffect(event.getItemDrop().getLocation());
			game.getWorldManager().setFlagLocation(Team.BADGER, event.getItemDrop().getLocation());
			new DropTimer(Team.BADGER, game).runTaskTimer(game.getCore(), 20L * 10, 20L * 10);
		} else if (event.getItemDrop().getItemStack().getType().equals(Material.NETHERRACK)) {
			game.getWorldManager().setFlagDropped(Team.AARDVARK, true);
			game.getWorldManager().setFlagLocation(Team.AARDVARK, event.getItemDrop().getLocation());
			Bukkit.getServer().broadcastMessage(
					ChatColor.RED + event.getPlayer().getName() + " has dropped Aardvark's flag at (" + loc + ")!");
			Bukkit.getServer().getWorld("world").strikeLightningEffect(event.getItemDrop().getLocation());
			game.getWorldManager().setFlagLocation(Team.AARDVARK, event.getItemDrop().getLocation());
			new DropTimer(Team.AARDVARK, game).runTaskTimer(game.getCore(), 20L * 10, 20L * 10);
		}
	}

	// TODO CURRENT ISSUES: PICKUP IS NOT DETECTED

	@EventHandler
	public void onPickup(EntityPickupItemEvent event) {
		if (event.getItem().getItemStack().getType() != Material.SOUL_SAND
				|| event.getItem().getItemStack().getType() != Material.NETHERRACK) {
			Bukkit.getServer().getLogger().info("VCTF - DEBUG: entity  picked up block");

			return;
		}
		if (!(event.getEntity() instanceof Player)) {
			event.setCancelled(true);
			Bukkit.getServer().getLogger().info("VCTF - DEBUG: entity other than player picked up block");
			return;
		}
		Player player = (Player) event.getEntity();
		// ITEM IS FLAG, PICKUP IS PLAYER

		if (!game.getTeamManager().hasTeam(player) && game.getTeamManager().getTeam(player) == Team.SPECTATOR) {
			Bukkit.getServer().getLogger().info("VCTF - DEBUG: player on team picked up item");
			return;
		}

		switch (game.getTeamManager().getTeam(player)) {
		case AARDVARK:
			// PLAYER IS ON A
			if (event.getItem().getItemStack().getType() == Material.SOUL_SAND) {
				// PLAYER PICKED UP BADGER FLAG
				// flag is not dropped anymore
				game.getWorldManager().setFlagDropped(Team.BADGER, false);
				Bukkit.getServer().broadcastMessage(ChatColor.RED + player.getName() + " has picked up Badger's flag!");
				// TIMER FOR PLAYER CARRYING BADGER's FLAG
				new CarryTimer(player, Team.BADGER, game).runTaskTimer(game.getCore(), 20L * 30, 20L * 30);
			} else if (event.getItem().getItemStack().getType() == Material.NETHERRACK) {
				// PLAYER RECOVERED AARDVARK
				// flag isn't dropped anymore
				Bukkit.getServer()
						.broadcastMessage(ChatColor.RED + player.getName() + " has recovered Aardvark's flag!");
				player.getInventory().remove(new ItemStack(Material.NETHERRACK));
				game.getWorldManager().recoverFlag(Team.AARDVARK);

			}
			break;
		case BADGER:
			// PLAYER IS ON B
			if (event.getItem().getItemStack().getType() == Material.SOUL_SAND) {
				// PLAYER RECOVERED BADGER FLAG
				Bukkit.getServer().broadcastMessage(ChatColor.RED + player.getName() + " has recovered Badger's flag!");
				player.getInventory().remove(new ItemStack(Material.SOUL_SAND));
				game.getWorldManager().recoverFlag(Team.BADGER);
			} else if (event.getItem().getItemStack().getType() == Material.NETHERRACK) {
				game.getWorldManager().setFlagDropped(Team.AARDVARK, false);
				Bukkit.getServer()
						.broadcastMessage(ChatColor.RED + player.getName() + " has picked up Aardvark's flag!");
				// TIMER FOR PLAYER CARRYING BADGER's FLAG
				new CarryTimer(player, Team.AARDVARK, game).runTaskTimer(game.getCore(), 20L * 30, 20L * 30);
			}
			break;
		default:
			break;
		}

	}

	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		game.getTeamManager().removeLife((Player) event.getEntity());
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		switch (game.getTeamManager().getTeam(event.getPlayer())) {
		case AARDVARK:
			event.setRespawnLocation(game.getWorldManager().getASpawn());
			break;
		case BADGER:
			event.setRespawnLocation(game.getWorldManager().getBSpawn());
			break;
		default:
			event.setRespawnLocation(game.getWorldManager().getSpawnLocation(event.getPlayer().getWorld(), 500, 500));
			break;
		}
	}

}
