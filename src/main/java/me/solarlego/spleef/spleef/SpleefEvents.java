package me.solarlego.spleef.spleef;

import me.solarlego.solarmain.Stats;
import me.solarlego.solarmain.hub.Hub;
import me.solarlego.spleef.Spleef;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SpleefEvents implements Listener {

    private final SpleefGame game;

    public SpleefEvents(SpleefGame spleef) {
        game = spleef;
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            Hub.resetPlayer(event.getPlayer());
            event.getPlayer().setPlayerListName(Stats.get(event.getPlayer().getUniqueId()).getPrefix() + event.getPlayer().getName());

            FileConfiguration playersFile = YamlConfiguration.loadConfiguration(new File(Spleef.getPlugin().getDataFolder(), "players.yml"));
            playersFile.addDefault(event.getPlayer().getUniqueId() + ".coins", 0);
            playersFile.options().copyDefaults(true);
            try {
                playersFile.save(new File(Spleef.getPlugin().getDataFolder(), "players.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            game.playerJoin(event.getPlayer());
        } else if (game.checkWorld(event.getFrom())) {
            game.playerLeave(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                if (Math.floor(event.getTo().getBlockY()) - 1 == 82 || Math.floor(event.getTo().getBlockY()) - 1 == 75) {
                    for (int x = -1; x < 2; x++) {
                        for (int z = -1; z < 2; z++) {
                            Location newLoc = new Location(event.getTo().getWorld(), event.getTo().getBlockX() + x, event.getTo().getBlockY() - 1, event.getTo().getBlockZ() + z);
                            game.decayBlock(newLoc);
                        }
                    }
                } else if (event.getTo().getBlockY() <= 55) {
                    game.playerDeath(event.getPlayer());
                }
            } else if (event.getTo().getBlockY() <= 76 && event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
                event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), 0.5, 83, 0.5));
            } else if (Math.abs(event.getTo().getBlockX()) > 90 || Math.abs(event.getTo().getBlockZ()) > 90) {
                event.setTo(event.getFrom());
            }

        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            game.playerLeave(event.getPlayer());
        }
    }

    @EventHandler
    public void onArrowLand(ProjectileHitEvent event) {
        if (game.checkWorld(event.getEntity().getWorld()) && event.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getEntity();
            if (event.getEntity().getLocation().getY() > 60) {
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        if (arrow.isOnGround()) {
                            Bukkit.getServer().getScheduler().runTask(Spleef.getPlugin(), () -> {
                                if (game.isRunning) {
                                    for (int x = -1; x < 2; x++) {
                                        for (int z = -1; z < 2; z++) {
                                            Location loc = arrow.getLocation();
                                            loc.setX(loc.getX() + x);
                                            loc.setY(loc.getY());
                                            loc.setZ(loc.getZ() + z);
                                            if (loc.getBlock().getType() == Material.AIR) {
                                                loc.getBlock().setType(Material.FIRE);
                                            }
                                        }
                                    }
                                }
                                arrow.remove();
                                timer.cancel();
                            });
                        }
                    }
                }, 0, 50);
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            if (game.time <= 0 || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK || event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            } else {
                event.setDamage(0);
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (game.checkWorld(event.getBlock().getWorld())) {
            if (event.getBlock().getY() < 60 || !game.isRunning) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (game.checkWorld(event.getBlock().getWorld())) {
            if (event.getBlock().getY() < 60 || !game.isRunning) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (game.checkWorld(event.getEntity().getWorld())) {
            event.setCancelled(true);
        }
    }

}
