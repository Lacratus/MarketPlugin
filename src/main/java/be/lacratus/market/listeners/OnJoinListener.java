package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.data.StoredDataHandler;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class OnJoinListener implements Listener {

    private Market main;
    private StoredDataHandler storedDataHandler;

    public OnJoinListener(Market main) {
        this.main = main;
        this.storedDataHandler = main.getStoredDataHandler();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        if (main.getEconomyImplementer().createPlayerAccount(player)) {
            System.out.println("Account created");
        } else {
            System.out.println("Account already existed");
        }
        System.out.print("TEST?????");

        //Fill onlinePlayers

        //Check if player gets item back
        DDGSpeler speler;
        if (main.getPlayersWithItems().containsKey(uuid)) {
            System.out.println("Hier 1");
            speler = main.getPlayersWithItems().get(uuid);
            main.getOnlinePlayers().put(uuid, speler);
        } else if (main.getPlayersWithBiddings().containsKey(uuid)) {
            System.out.println("hier 2");
            speler = main.getPlayersWithBiddings().get(uuid);
            for (VeilingItem item : main.getPlayersWithBiddings().get(uuid).getBiddenItems()) {
                long timeLeft = item.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                if (timeLeft < 0) {
                    player.getInventory().setItem(player.getInventory().firstEmpty(), item.getItemStack());
                    main.getPlayersWithBiddings().get(uuid).removebiddedItem(item);
                    main.getItemsRemoveDatabase().add(item);
                }
            }
            main.getOnlinePlayers().put(uuid, speler);
        } else {
            System.out.println("HIer 3");
            storedDataHandler.loadData(uuid).thenAccept(DDGspeler -> {
                main.getOnlinePlayers().put(uuid, DDGspeler);
            }).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
        }
    }
}
