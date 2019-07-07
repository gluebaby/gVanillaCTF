package me.undeadguppy.vanillactf.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
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
		if (game.getGameManager().getPhase() == GamePhase.FIGHT) {
			Player p = event.getPlayer();
			if (event.getBlockPlaced().getType() != Material.SOUL_SAND
					|| event.getBlockPlaced().getType() != Material.NETHERRACK) {
				return;
			}
			// Placing netherrack or soul sand.
			if (event.getBlockAgainst().getType() != Material.SOUL_SAND
					|| event.getBlockAgainst().getType() != Material.NETHERRACK) {
				// placing against non flag!
				event.getPlayer().sendMessage(ChatColor.RED + "You must place the enemy flag on your own!");
				event.setCancelled(true);
				return;
			}

			switch (event.getBlockPlaced().getType()) {
			case NETHERRACK:
				// Aardvark wins
				game.getTeamManager().addCapture(Team.AARDVARK);
				Bukkit.getServer().broadcastMessage(ChatColor.RED + p.getName() + " has captured Badger's flag!");
				game.end();
				break;
			case SOUL_SAND:
				// BADGER wins
				game.getTeamManager().addCapture(Team.BADGER);
				Bukkit.getServer().broadcastMessage(ChatColor.RED + p.getName() + " has captured Aardvark's flag!");
				game.end();
				break;
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
	public void onBreakBlock(BlockBreakEvent event) {
		// Check if flag
		if (event.getBlock().getType() != Material.NETHERRACK || event.getBlock().getType() != Material.SOUL_SAND) {
			// NOT FLAG
			if (event.getBlock().getType() == Material.GLOWSTONE) {
				event.setCancelled(true);
			}
			if (game.getGameManager().getPhase() == GamePhase.PREPARE) {
				if (game.getWorldManager().getForcefieldA().containsLocation(event.getBlock().getLocation())
						|| game.getWorldManager().getForcefieldB().containsLocation(event.getBlock().getLocation())) {
					// IN FORCEFIELD
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.RED + "You cannot break blocks within the forcefield!");
				}
			}
		} else {
			// YES FLAG
			if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.IRON_PICKAXE) {
				switch (event.getBlock().getType()) {
				case NETHERRACK:
					switch (game.getTeamManager().getTeam(event.getPlayer())) {
					case AARDVARK:
						event.setCancelled(true);
						event.getPlayer().sendMessage(ChatColor.RED + "You cannot mine your own flag!");
						break;
					case BADGER:
						Bukkit.getServer().broadcastMessage(
								ChatColor.RED + event.getPlayer().getName() + " has mined Aardvark's flag!");
						event.getBlock().getLocation().getWorld().strikeLightningEffect(event.getBlock().getLocation());
						break;
					default:
						break;
					}
				case SOUL_SAND:
					switch (game.getTeamManager().getTeam(event.getPlayer())) {
					case BADGER:
						event.setCancelled(true);
						event.getPlayer().sendMessage(ChatColor.RED + "You cannot mine your own flag!");
						break;
					case AARDVARK:
						Bukkit.getServer().broadcastMessage(
								ChatColor.RED + event.getPlayer().getName() + " has mined Badger's flag!");
						event.getBlock().getLocation().getWorld().strikeLightningEffect(event.getBlock().getLocation());
						break;
					default:
						break;
					}

				default:
					break;

				}
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + "You must use an iron pickaxe to mine the flag!");
				event.setCancelled(true);
				return;
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
			Bukkit.getServer().broadcastMessage(
					ChatColor.RED + event.getPlayer().getName() + " has dropped Badger's flag at (" + loc + ")!");
			Bukkit.getServer().getWorld("world").strikeLightningEffect(event.getItemDrop().getLocation());
			game.getWorldManager().setFlagLocation(Team.BADGER, event.getItemDrop().getLocation());
			new DropTimer(Team.BADGER, game).runTaskTimer(game.getCore(), 20L * 10, 20L * 10);
		} else if (event.getItemDrop().getItemStack().getType().equals(Material.NETHERRACK)) {
			Bukkit.getServer().broadcastMessage(
					ChatColor.RED + event.getPlayer().getName() + " has dropped Aardvark's flag at (" + loc + ")!");
			Bukkit.getServer().getWorld("world").strikeLightningEffect(event.getItemDrop().getLocation());
			game.getWorldManager().setFlagLocation(Team.AARDVARK, event.getItemDrop().getLocation());
			new DropTimer(Team.AARDVARK, game).runTaskTimer(game.getCore(), 20L * 10, 20L * 10);
		}
	}

	@EventHandler
	public void onPickup(EntityPickupItemEvent event) {
		if (event.getItem().getItemStack().getType() == Material.SOUL_SAND
				|| event.getItem().getItemStack().getType() == Material.NETHERRACK) {
			if (event.getEntityType() != EntityType.PLAYER) {
				event.setCancelled(true);
				return;
			}
			Player player = (Player) event.getEntity();
			// ITEM IS FLAG, PICKUP IS PLAYER
			if (game.getTeamManager().getTeam(player) == Team.AARDVARK) {
				// player is on aardvark team
				if (event.getItem().getItemStack().getType() == Material.NETHERRACK) {
					// recover
					Bukkit.getServer()
							.broadcastMessage(ChatColor.RED + player.getName() + " has recovered Aardvark's flag!");
					player.getInventory().remove(event.getItem().getItemStack());
					game.getWorldManager().recoverFlag(Team.BADGER);
				} else {
					Bukkit.getServer()
							.broadcastMessage(ChatColor.RED + player.getName() + " has picked up Badger's flag!");
					new CarryTimer(player, Team.AARDVARK).runTaskTimer(game.getCore(), 20L * 30, 20L * 30);
				}
			} else if (game.getTeamManager().getTeam(player) == Team.BADGER) {
				if (event.getItem().getItemStack().getType() == Material.NETHERRACK) {
					Bukkit.getServer()
							.broadcastMessage(ChatColor.RED + player.getName() + " has picked up Aardvark's flag!");
					new CarryTimer(player, Team.BADGER).runTaskTimer(game.getCore(), 20L * 30, 20L * 30);
				} else {
					// recover
					Bukkit.getServer()
							.broadcastMessage(ChatColor.RED + player.getName() + " has recovered Badger's flag!");
					player.getInventory().remove(event.getItem().getItemStack());
					game.getWorldManager().recoverFlag(Team.BADGER);
				}
			}
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
		case BADGER:
			event.setRespawnLocation(game.getWorldManager().getBSpawn());
		default:
			event.setRespawnLocation(game.getWorldManager().getSpawnLocation(event.getPlayer().getWorld(), 500, 500));
		}
	}

}
