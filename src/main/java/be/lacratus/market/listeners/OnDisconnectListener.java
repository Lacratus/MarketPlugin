package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.data.StoredDataHandler;
import be.lacratus.market.objects.DDGPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.UUID;

public class OnDisconnectListener implements Listener {

    private Market main;
    private StoredDataHandler storedDataHandler;

    public OnDisconnectListener(Market main) {
        this.main = main;
        this.storedDataHandler = main.getStoredDataHandler();
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        DDGPlayer player = main.getOnlinePlayers().get(uuid);

        storedDataHandler.saveData(player);

        main.getOnlinePlayers().remove(uuid);
    }
}
