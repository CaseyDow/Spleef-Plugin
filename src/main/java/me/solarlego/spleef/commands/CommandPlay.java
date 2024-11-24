package me.solarlego.spleef.commands;

import me.solarlego.spleef.gui.PlayGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandPlay implements Listener {

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage().toLowerCase();
        if (cmd.equals("/play spleef")) {
            new PlayGUI(event.getPlayer());
            event.setCancelled(true);
        }
    }

}
