package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.data.StoredDataHandler;
import be.lacratus.market.objects.DDGSpeler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OnDisconnectListener implements Listener {

    private Market main;
    private StoredDataHandler storedDataHandler;

    public OnDisconnectListener(Market main) {
        this.main = main;
        this.storedDataHandler = main.getStoredDataHandler();
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) throws SQLException {
        UUID uuid = event.getPlayer().getUniqueId();
        DDGSpeler speler = main.getOnlinePlayers().get(uuid);

        storedDataHandler.saveData(speler);

        main.getOnlinePlayers().remove(uuid);
    }
}
