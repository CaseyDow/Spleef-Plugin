package me.solarlego.spleef;

import me.solarlego.spleef.commands.CommandPlay;
import me.solarlego.spleef.spleef.SpleefGame;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Spleef extends JavaPlugin {

    private static Spleef instance;
    private ArrayList<SpleefGame> games;

    @Override
    public void onEnable() {
        instance = this;
        games = new ArrayList<>();
        Bukkit.getServer().getPluginManager().registerEvents(new CommandPlay(), this);

        saveDefaultConfig();
        File file = new File(getDataFolder(), "players.yml");
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        for (SpleefGame game : games) {
            game.shutdown();
        }
    }

    public static Spleef getPlugin() {
        return instance;
    }

    public void updatePlayerFile(String path, Integer val) {
        FileConfiguration playersFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "players.yml"));

        playersFile.set(path, val + playersFile.getInt(path));
        playersFile.options().copyDefaults(true);
        try {
            playersFile.save(new File(getDataFolder(), "players.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<SpleefGame> getGames() {
        return games;
    }

    public void addGame(SpleefGame game) {
        this.games.add(game);
    }

}
