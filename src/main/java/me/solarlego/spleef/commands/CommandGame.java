package me.solarlego.spleef.commands;

import me.solarlego.spleef.spleef.SpleefGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandGame implements Listener {

    private final SpleefGame game;

    public CommandGame(SpleefGame spleef) {
        game = spleef;
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (game.checkWorld(event.getPlayer().getWorld())) {
            String cmd = event.getMessage().toLowerCase();
            if ("/end".equals(cmd)) {
                if (event.getPlayer().hasPermission("solarlego.command.end")) {
                    game.shutdown();
                    event.setCancelled(true);
                }
            }
        }
    }

}
