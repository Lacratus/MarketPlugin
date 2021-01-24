package be.lacratus.market.listeners;

import be.lacratus.market.Market;
import be.lacratus.market.data.StoredDataHandler;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OnJoinListener implements Listener {

    private Market main;
    private StoredDataHandler storedDataHandler;

    public OnJoinListener(Market main) {
        this.main = main;
        this.storedDataHandler = main.getStoredDataHandler();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (main.getEconomyImplementer().createPlayerAccount(player)) {
            System.out.println("Account created");
        } else {
            System.out.println("Account already existed");
        }

        //Fill onlinePlayers

        //Check if player gets item back
        DDGSpeler speler;
        List<VeilingItem> veilingItems;
        System.out.println("OnJoiNListener: " + main.getPlayersWithBiddings().containsKey(uuid));
        if (main.getPlayersWithItems().containsKey(uuid)) {
            System.out.println("Speler heeft items op de veiling: " + main.getPlayersWithItems().get(uuid).getBiddenItems());
            speler = main.getPlayersWithItems().get(uuid);
            veilingItems = new ArrayList<>(main.getPlayersWithItems().get(uuid).getBiddenItems());
            for (VeilingItem item : veilingItems) {
                System.out.println(item);
                long timeLeft = item.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                if (timeLeft < 0) {
                    player.getInventory().setItem(player.getInventory().firstEmpty(), item.getItemStack());
                    main.getPlayersWithBiddings().get(uuid).getBiddenItems().remove(item);
                    main.getItemsRemoveDatabase().add(item);
                }
            }
            main.getOnlinePlayers().put(uuid, speler);
            main.updateLists(speler);
        } else if (main.getPlayersWithBiddings().containsKey(uuid)) {
            System.out.println("Spelers heeft biedingen op de veilingen");
            speler = main.getPlayersWithBiddings().get(uuid);
            veilingItems = new ArrayList<>(main.getPlayersWithBiddings().get(uuid).getBiddenItems());
            for (VeilingItem item : veilingItems) {
                long timeLeft = item.getTimeOfDeletion() - (System.currentTimeMillis() / 1000);
                if (timeLeft < 0) {
                    player.getInventory().setItem(player.getInventory().firstEmpty(), item.getItemStack());
                    main.getPlayersWithBiddings().get(uuid).getBiddenItems().remove(item);
                    main.getItemsRemoveDatabase().add(item);
                    System.out.println(main.getItemsRemoveDatabase());
                }
            }
            main.getOnlinePlayers().put(uuid, speler);
            main.updateLists(speler);
        } else {
            System.out.println("Speler heeft niks op de veiling staan");
            storedDataHandler.loadData(uuid).thenAccept(DDGspeler ->
                    main.getOnlinePlayers().put(uuid, DDGspeler)).exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
        }
    }
}
