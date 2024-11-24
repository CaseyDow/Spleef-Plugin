package me.solarlego.spleef.spleef;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerInfo {

    private final UUID player;
    private int coins = 0;
    private boolean isDead = true;

    public PlayerInfo(Player p) {
        player = p.getUniqueId();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int coins) {
        this.coins += coins;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public boolean isDead() {
        return isDead;
    }

}
