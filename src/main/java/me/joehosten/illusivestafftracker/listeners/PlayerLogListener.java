package me.joehosten.illusivestafftracker.listeners;

import me.joehosten.illusivestafftracker.IllusiveStaffTracker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.UUID;

public class PlayerLogListener implements Listener {

    private final IllusiveStaffTracker plugin;
    private final HashMap<UUID, Long> map;

    public PlayerLogListener(IllusiveStaffTracker plugin) {
        this.plugin = plugin;
        this.map = plugin.getMap();
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("illusive.staff")) return;
        clockIn(p.getUniqueId());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("illusive.staff")) return;
        clockOut(p);
    }

    private Long getTimeFromConfig(UUID p) {
        FileConfiguration config = IllusiveStaffTracker.getInstance().getConfig();
        return config.getLong(String.valueOf(p));
    }

    private void clockIn(UUID p) {
        map.put(p, System.currentTimeMillis());
        System.out.print("clocked in " + p);
    }

    private void clockOut(Player p) {
        long logoutTime = System.currentTimeMillis();
        long loginTime = map.get(p.getUniqueId());
        map.remove(p.getUniqueId());

        FileConfiguration config = IllusiveStaffTracker.getInstance().getConfig();
        Long timeToday = logoutTime - loginTime;
        Long timeFromConfig = getTimeFromConfig(p.getUniqueId());
        Long toSet = timeToday + timeFromConfig;
        config.set(String.valueOf(p.getUniqueId()), toSet);
        IllusiveStaffTracker.getInstance().saveConfig();

        IllusiveStaffTracker.getInstance().sendEmbed(p, p.getName() + " logged off with " + plugin.convertTime(toSet) + " played this week.", false, Color.GRAY);
    }

    public void saveAllPlayers() {
        for (UUID p : map.keySet()) {
            long logoutTime = System.currentTimeMillis();
            long loginTime = map.get(p);
            map.remove(p);

            FileConfiguration config = IllusiveStaffTracker.getInstance().getConfig();
            config.set(String.valueOf(p), (logoutTime - loginTime) + getTimeFromConfig(p));
            IllusiveStaffTracker.getInstance().saveConfig();
            if (Bukkit.getPlayer(p) != null) { // check if player is online
                clockIn(p);
            }
        }
        System.out.println("saved playrs");
    }
}
