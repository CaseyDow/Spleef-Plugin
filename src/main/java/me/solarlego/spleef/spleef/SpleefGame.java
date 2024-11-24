package me.solarlego.spleef.spleef;

import me.solarlego.solarmain.FileUtils;
import me.solarlego.solarmain.Stats;
import me.solarlego.solarmain.hub.Hub;
import me.solarlego.spleef.Spleef;
import me.solarlego.spleef.Scoreboard;
import me.solarlego.spleef.commands.CommandGame;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.io.File;
import java.util.*;

public class SpleefGame {

    public boolean isRunning = false;
    public World worldSpleef;
    public int time;
    public final String worldName;

    private final Scoreboard sb = new Scoreboard(this);
    private HashMap<UUID, PlayerInfo> players;
    private Timer timer;
    private Map<Location, DecayingBlock> decayingBlocks;

    public SpleefGame(String world) {
        Bukkit.getServer().getPluginManager().registerEvents(new SpleefEvents(this), Spleef.getPlugin());
        Bukkit.getServer().getPluginManager().registerEvents(new CommandGame(this), Spleef.getPlugin());
        Spleef.getPlugin().addGame(this);
        worldName = world;
        setup();
    }

    public void setup() {
        Bukkit.getServer().unloadWorld(worldSpleef, false);
        FileUtils.copyResourcesRecursively(Spleef.getPlugin(),"/spleef", new File("./" + worldName));
        worldSpleef = new WorldCreator(worldName).createWorld();

        time = -10;
        players = new HashMap<>();
        timer = new Timer();
        decayingBlocks = new HashMap<>();

        for (int y = 82; y > 74; y -= 7) {
            for (int r = 30; r >= 0; r--) {
                for (double i = 0.0; i < 360.0; i += 0.1) {
                    double angle = i * Math.PI / 180;
                    int x = (int) Math.floor(0.5 + r * Math.cos(angle));
                    int z = (int) Math.floor(0.5 + r * Math.sin(angle));

                    worldSpleef.getBlockAt(x, y, z).setType(Material.WOOL);
                    BlockState state = worldSpleef.getBlockAt(x, y, z).getState();
                    Wool woolData = (Wool) state.getData();
                    woolData.setColor(DyeColor.SILVER);
                    state.setData(woolData);
                    state.update();
                }
            }
        }

        for (int x = -31; x < 32; x++) {
            for (int z = -31; z < 32; z++) {
                for (int y = 81; y < 84; y++) {
                    if (worldSpleef.getBlockAt(x, y, z).getType() == Material.FIRE) {
                        worldSpleef.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                    if (worldSpleef.getBlockAt(x, y - 7, z).getType() == Material.FIRE) {
                        worldSpleef.getBlockAt(x, y - 7, z).setType(Material.AIR);
                    }
                }
            }
        }

        for (Entity ent : worldSpleef.getEntities()) {
            if (ent instanceof Projectile) {
                ent.remove();
            }
        }

    }

    private void start() {
        ItemStack shears = new ItemStack(Material.SHEARS);
        shears.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
        shears.addUnsafeEnchantment(Enchantment.DIG_SPEED, 50);
        ItemMeta meta = shears.getItemMeta();
        meta.spigot().setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        shears.setItemMeta(meta);

        ItemStack bow = new ItemStack(Material.BOW);
        bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
        bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 2);
        bow.getItemMeta().spigot().setUnbreakable(true);

        ItemStack arrow = new ItemStack(Material.ARROW, 8);

        for (PlayerInfo pInfo : players.values()) {
            pInfo.getPlayer().getInventory().setItem(0, shears);
            pInfo.getPlayer().getInventory().setItem(1, bow);
            pInfo.getPlayer().getInventory().setItem(8, arrow);
            pInfo.getPlayer().setGameMode(GameMode.SURVIVAL);
            pInfo.setDead(false);

            if (Math.floor(pInfo.getPlayer().getLocation().getBlockY()) - 1 == 82 || Math.floor(pInfo.getPlayer().getLocation().getBlockY()) - 1 == 75) {
                for (int x = -1; x < 2; x++) {
                    for (int z = -1; z < 2; z++) {
                        Location newLoc = new Location(pInfo.getPlayer().getWorld(), pInfo.getPlayer().getLocation().getBlockX() + x, pInfo.getPlayer().getLocation().getBlockY() - 1, pInfo.getPlayer().getLocation().getBlockZ() + z);
                        decayBlock(newLoc);
                    }
                }
            }
        }

        isRunning = true;
    }

    public void playerJoin(Player player) {
        player.teleport(worldSpleef.getSpawnLocation().add(0.5, 0, 0.5));
        player.getInventory().clear();
        players.put(player.getUniqueId(), new PlayerInfo(player));
        if (time >= 0) {
            player.setGameMode(GameMode.SPECTATOR);
            for (PlayerInfo pInfo : players.values()) {
                pInfo.getPlayer().hidePlayer(player);
            }
        } else {
            player.setGameMode(GameMode.ADVENTURE);
            for (Player p : worldSpleef.getPlayers()) {
                p.showPlayer(player);
                p.sendMessage(Stats.get(player.getUniqueId()).getColor() + player.getName() + " \u00A7ehas joined (\u00A7b" + players.size() + "\u00A7e)");
                sb.updateScoreboard(p);
            }

            if (players.size() == 2) {
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        runTimer();
                    }
                }, 0, 1000);
            }
        }
    }

    private void runTimer() {
        String color = time > -10 ? "c" : "6";
        if (Arrays.asList(-20, -10, -5, -4, -3, -2, -1).contains(time)) {
            for (Player player : worldSpleef.getPlayers()) {
                player.sendMessage("\u00A7eThe game will start in \u00A7" + color + -time + " \u00A7eseconds!");
                player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
            }
        }
        Bukkit.getServer().getScheduler().runTask(Spleef.getPlugin(), () -> {
            if (time == 0) {
                for (Player player : worldSpleef.getPlayers()) {
                    player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1, 1);
                }
                start();
            }
            for (Player player : worldSpleef.getPlayers()) {
                sb.updateScoreboard(player);
            }
            time++;
        });
    }

    public void playerLeave(Player player) {
        if (time >= 0 && isRunning) {
            playerDeath(player);
        } else if (time < 0) {
            players.remove(player.getUniqueId());
            if (players.size() < 2) {
                time = -10;
                timer.cancel();
                for (Player p : worldSpleef.getPlayers()) {
                    sb.updateScoreboard(p);
                    p.sendMessage("\u00A7cNot enough players. Start canceled.");
                    p.playSound(p.getLocation(), Sound.CLICK, 1, 1);
                }
            }
        }
    }

    public void decayBlock(Location loc) {
        if (decayingBlocks.get(loc) == null && time >= 0 && isRunning) {
            decayingBlocks.put(loc, new DecayingBlock(loc));
        }
    }

    public void playerDeath(Player player) {
        if (player.getWorld() == worldSpleef) {
            player.setGameMode(GameMode.SPECTATOR);
            for (PlayerInfo pInfo : players.values()) {
                if (pInfo.isDead()) {
                    player.showPlayer(pInfo.getPlayer());
                } else {
                    pInfo.getPlayer().hidePlayer(player);
                }
            }
            player.getInventory().clear();
        }
        if (isRunning) {
            players.get(player.getUniqueId()).setDead(true);
            Stats playerInfo = Stats.get(player.getUniqueId());
            for (Player p : worldSpleef.getPlayers()) {
                p.sendMessage(playerInfo.getPrefix() + playerInfo.getName() + "\u00A7f has met their untimely death!");
            }
            UUID winner = null;
            int num = 0;
            for (PlayerInfo pInfo : players.values()) {
                if (!pInfo.isDead()) {
                    num++;
                    pInfo.getPlayer().sendMessage("\u00A76+10 coins! (Death)");
                    pInfo.addCoins(10);
                    pInfo.getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 2));
                    if (winner == null) {
                        winner = pInfo.getPlayer().getUniqueId();
                    }
                }
            }
            if (num == 2) {
                final int[] flash_num = {0};
                Timer flash_timer = new Timer();
                flash_timer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        Bukkit.getServer().getScheduler().runTask(Spleef.getPlugin(), () -> {
                            for (int r = 30; r >= 0; r--) {
                                for (double i = 0.0; i < 360.0; i += 0.1) {
                                    double angle = i * Math.PI / 180;
                                    int x = (int) Math.floor(0.5 + r * Math.cos(angle));
                                    int z = (int) Math.floor(0.5 + r * Math.sin(angle));
                                    if (flash_num[0] == 7) {
                                        worldSpleef.getBlockAt(x, 82, z).setType(Material.AIR);

                                    } else if (worldSpleef.getBlockAt(x, 82, z).getType() == Material.WOOL) {
                                        BlockState state = worldSpleef.getBlockAt(x, 82, z).getState();
                                        Wool woolData = (Wool) state.getData();
                                        if (flash_num[0] % 2 == 0) {
                                            woolData.setColor(DyeColor.RED);
                                        } else {
                                            woolData.setColor(DyeColor.YELLOW);
                                        }
                                        state.setData(woolData);
                                        state.update();
                                    }
                                }
                            }
                            if (flash_num[0] == 7) {
                                flash_timer.cancel();
                            }
                            flash_num[0]++;
                            timer.cancel();
                        });
                    }
                }, 0, 700);
            }
            if (winner != null && num == 1) {
                win(Bukkit.getPlayer(winner));
            }
        }
    }

    private void win(Player player) {
        player.sendMessage("\u00A76+20 coins! (Win)");
        players.get(player.getUniqueId()).addCoins(20);
        timer.cancel();
        isRunning = false;

        Stats playerInfo = Stats.get(player.getUniqueId());
        for (PlayerInfo pInfo : players.values()) {
            if (pInfo.getPlayer().getWorld() == worldSpleef) {
                pInfo.getPlayer().sendMessage("\u00A76\u00A7m----------------------------\n" + playerInfo.getPrefix() + playerInfo.getName() + "\u00A7f won!\nCoins Earned: \u00A76" + pInfo.getCoins() + "\n\u00A7m----------------------------");
                sb.updateScoreboard(pInfo.getPlayer());
            }
        }
        for (DecayingBlock block : decayingBlocks.values()) {
            block.cancelTimer();
        }
        for (PlayerInfo pInfo : players.values()) {
            Spleef.getPlugin().updatePlayerFile(pInfo.getPlayer().getUniqueId() + ".coins", pInfo.getCoins());
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(Spleef.getPlugin(), () -> {
            setup();
            for (Player p : worldSpleef.getPlayers()) {
                playerJoin(p);
            }
            for (Player p : worldSpleef.getPlayers()) {
                sb.updateScoreboard(p);
            }
        }, 200);

    }

    public boolean checkWorld(World world) {
        return world == this.worldSpleef || world.getName().equals(worldName);
    }

    public String getVar(String name) {
        if (name.equals("time")) {
            if (time >= 0) {
                return (int) Math.floor((double) time / 60) + "m " + time % 60 + "s";
            } else if (players.size() < 2) {
                return "Waiting";
            } else {
                return -time + "s";
            }
        } else if (name.equals("players")) {
            int alive = 0;
            for (PlayerInfo pInfo : players.values()) {
                if (!pInfo.isDead()) {
                    alive++;
                }
            }
            return String.valueOf(alive);
        }
        return "";
    }

    public void shutdown() {
        isRunning = false;
        for (Player player : worldSpleef.getPlayers()) {
            Hub.sendHub(player);
        }
        Bukkit.getServer().unloadWorld(worldSpleef, false);
        FileUtils.deleteDirectory(new File("./" + worldSpleef.getName()));
        timer.cancel();
    }

}
