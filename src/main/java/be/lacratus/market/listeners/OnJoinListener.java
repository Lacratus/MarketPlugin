package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnJoinListener implements Listener {

    private Market main;

    public OnJoinListener(Market main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        OfflinePlayer player = e.getPlayer();
        if (main.getEconomyImplementer().createPlayerAccount(player)) {
            System.out.println("Account created");
        } else {
            System.out.println("Account already existed");
        }
        System.out.printf("TEST?????");
    }
}
