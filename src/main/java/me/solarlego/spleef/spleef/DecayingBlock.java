package me.solarlego.spleef.spleef;

import me.solarlego.spleef.Spleef;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.Wool;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class DecayingBlock {

    private int time;
    private final int firstDecay;
    private final int secondDecay;
    private final int thirdDecay;
    private final Timer timer;

    public DecayingBlock(Location loc) {
        firstDecay = new Random().nextInt(50) + 10;
        secondDecay = new Random().nextInt(50) + 10;
        thirdDecay = new Random().nextInt(50) + 10;
        time = 0;
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                time++;
                if (time == firstDecay) {
                    Bukkit.getServer().getScheduler().runTask(Spleef.getPlugin(), () -> {
                        if (loc.getBlock().getType() == Material.WOOL) {
                            BlockState state = loc.getBlock().getState();
                            Wool woolData = (Wool) state.getData();
                            woolData.setColor(DyeColor.ORANGE);
                            state.setData(woolData);
                            state.update();
                        }
                    });
                } else if (time == firstDecay + secondDecay) {
                    Bukkit.getServer().getScheduler().runTask(Spleef.getPlugin(), () -> {
                        if (loc.getBlock().getType() == Material.WOOL) {
                            BlockState state = loc.getBlock().getState();
                            Wool woolData = (Wool) state.getData();
                            woolData.setColor(DyeColor.RED);
                            state.setData(woolData);
                            state.update();
                        }
                    });
                } else if (time == firstDecay + secondDecay + thirdDecay) {
                    Bukkit.getServer().getScheduler().runTask(Spleef.getPlugin(), () -> loc.getBlock().setType(Material.AIR));
                    timer.cancel();
                }
            }
        }, 0, 100);

    }

    public void cancelTimer() {
        timer.cancel();
    }
}
