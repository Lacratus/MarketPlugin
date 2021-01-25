package be.lacratus.market.data;

import be.lacratus.market.Market;
import be.lacratus.market.objects.DDGPlayer;
import be.lacratus.market.objects.AuctionItem;
import be.lacratus.market.util.ItemStackSerializer;
import be.lacratus.market.util.SortByIndexAscending;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StoredDataHandler {

    private Market main;

    public StoredDataHandler(Market main) {
        this.main = main;
    }

    // Return of MaxIndex in database
    public CompletableFuture<Integer> getMaxIndex(Connection connection) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                try (PreparedStatement ps = connection.prepareStatement("SELECT MAX(itemindex) as maxindex FROM veilingitem");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt("maxindex");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }


    public void saveData(DDGPlayer data) {
        // Data gets updated to database
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            try (Connection connection = main.openConnection();
                 PreparedStatement ps = connection.prepareStatement("UPDATE ddgspeler SET balance = " + main.getPlayerBank().get(data.getUuid()) + " WHERE uuid = '" + data.getUuid() + "';")) {
                ps.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }


    public CompletableFuture<DDGPlayer> loadData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            // First login makes row in table
            try (Connection connection = main.openConnection()) {
                PreparedStatement ps = connection.prepareStatement("SELECT COUNT(uuid) FROM ddgspeler WHERE uuid = '" + uuid + "';");
                ResultSet rs = ps.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    PreparedStatement ps2 = connection.prepareStatement("INSERT INTO ddgspeler(uuid,balance) VALUES ('"
                            + uuid + "',DEFAULT)");
                    ps2.executeUpdate();
                    ps2.close();
                }
                return new DDGPlayer(uuid);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public Runnable fillAuctionHouseAndBank() {
        return () -> {
            try (Connection connection = main.openConnection()) {
                resetItemIndex(connection);
                try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM veilingitem");
                     PreparedStatement ps2 = connection.prepareStatement("SELECT * FROM ddgspeler");
                     ResultSet rs = ps.executeQuery();
                     ResultSet rs2 = ps2.executeQuery()) {

                    while (rs.next()) {
                        // Creation of AuctionItem
                        int id = rs.getInt("itemindex");
                        UUID uuidOwner = UUID.fromString(rs.getString("uuidowner"));
                        ItemStack itemStack = ItemStackSerializer.StringToItemStack(rs.getString("itemstack"));
                        int timeOfDeletion = rs.getInt("timeofdeletion");

                        AuctionItem auctionItem = new AuctionItem(id, itemStack, uuidOwner, timeOfDeletion);
                        UUID uuidBidder = UUID.fromString(rs.getString("uuidbidder"));
                        auctionItem.setUuidBidder(uuidBidder);
                        auctionItem.setHighestOffer(rs.getInt("highestoffer"));

                        boolean bumped;
                        bumped = rs.getInt("Bumped") != 0;
                        auctionItem.setBumped(bumped);
                        // Creation of DDGPlayer who have items on auctionhouse
                        DDGPlayer Bidder;
                        if (uuidBidder.toString().equals(uuidOwner.toString())) {
                            if (main.getOnlinePlayers().containsKey(uuidBidder)) {
                                Bidder = main.getOnlinePlayers().get(uuidBidder);
                            } else if (main.getPlayersWithItems().containsKey(uuidBidder)) {
                                Bidder = main.getPlayersWithItems().get(uuidBidder);
                            } else if (main.getPlayersWithBiddings().containsKey(uuidBidder)) {
                                Bidder = main.getPlayersWithBiddings().get(uuidBidder);
                            } else {
                                Bidder = new DDGPlayer(uuidBidder);
                            }
                            Bidder.getPersonalItems().add(auctionItem);
                            Bidder.getBiddenItems().add(auctionItem);
                            main.updateLists(Bidder);
                        } else {
                            DDGPlayer owner;
                            if (main.getPlayersWithItems().containsKey(uuidOwner)) {
                                owner = main.getPlayersWithItems().get(uuidOwner);
                            } else {
                                owner = new DDGPlayer(uuidOwner);
                                main.getPlayersWithItems().put(uuidOwner, owner);
                            }
                            owner.getPersonalItems().add(auctionItem);

                            //Creation of DDGPlayer who have bidden items
                            if (main.getPlayersWithItems().containsKey(uuidBidder)) {
                                Bidder = main.getPlayersWithItems().get(uuidBidder);
                            } else if (main.getPlayersWithBiddings().containsKey(uuidBidder)) {
                                Bidder = main.getPlayersWithBiddings().get(uuidBidder);
                            } else {
                                Bidder = new DDGPlayer(uuidBidder);
                                main.getPlayersWithBiddings().put(uuidBidder, Bidder);
                            }
                            Bidder.getBiddenItems().add(auctionItem);
                        }
                        main.getAuctionItems().add(auctionItem);
                        main.runTaskGiveItem(auctionItem, Bidder, timeOfDeletion);
                    }

                    while (rs2.next()) {
                        UUID uuid = UUID.fromString(rs2.getString("uuid"));
                        double balance = rs2.getInt("balance");

                        main.getPlayerBank().put(uuid, balance);
                    }


                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
    }

    // Saving AuctionHouse and Balance of Players after a certain time
    public Runnable saveAuctionHouseAndBalance() {
        return () -> {
            System.out.println("SAVED");

            try (Connection connection = main.openConnection()) {
                // Save AuctionItems
                if (!main.getAuctionItems().isEmpty()) {
                    List<AuctionItem> auctionItems = new ArrayList<>(main.getAuctionItems());
                    auctionItems.sort(new SortByIndexAscending());
                    for (AuctionItem item : auctionItems) {
                        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(itemindex) FROM veilingitem WHERE itemindex = '" + item.getId() + "';");
                             ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            int bumped;
                            if (item.isBumped()) {
                                bumped = 1;
                            } else {
                                bumped = 0;
                            }
                            if (rs.getInt(1) == 0) {

                                try (PreparedStatement ps2 = connection.prepareStatement("INSERT INTO veilingitem(highestoffer,uuidbidder,uuidowner,timeofdeletion,itemstack,bumped) VALUES (" + item.getHighestOffer() +
                                        ", '" + item.getUuidBidder() +
                                        "', '" + item.getUuidOwner() +
                                        "', " + (item.getTimeOfDeletion() - (System.currentTimeMillis() / 1000)) +
                                        ", '" + ItemStackSerializer.ItemStackToString(item.getItemStack()) +
                                        "', " + bumped + ") ")) {

                                    ps2.executeUpdate();
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            } else {

                                try (PreparedStatement ps2 = connection.prepareStatement("UPDATE veilingitem SET highestoffer =  " + item.getHighestOffer() + ", uuidbidder = '" + item.getUuidBidder()
                                        + "',uuidowner = '" + item.getUuidOwner() + "',timeofdeletion = " + (item.getTimeOfDeletion() - System.currentTimeMillis() / 1000) + ", itemstack = '" + ItemStackSerializer.ItemStackToString(item.getItemStack())
                                        + "', bumped = " + bumped + " WHERE itemindex = " + item.getId() + ";")) {

                                    ps2.executeUpdate();
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Removing AuctionItems
                if (!main.getItemsRemoveDatabase().isEmpty()) {
                    List<AuctionItem> removeAuctionItems = new ArrayList<>(main.getItemsRemoveDatabase());
                    List<AuctionItem> toRemove = new ArrayList<>();
                    for (AuctionItem item : removeAuctionItems) {
                        if (item.getId() != 0) {
                            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM veilingitem WHERE itemindex = " + item.getId() + ";")) {
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        toRemove.add(item);
                    }
                    main.getItemsRemoveDatabase().removeAll(toRemove);
                }

                // Save Balance
                for (Map.Entry<UUID, Double> pl : main.getPlayerBank().entrySet())
                    try (PreparedStatement ps = connection.prepareStatement("UPDATE ddgspeler SET balance = " + pl.getValue() + " WHERE uuid = '" + pl.getKey() + "';")) {
                        ps.executeUpdate();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
    }

    public void resetItemIndex(Connection connection) throws SQLException {
        try (
                PreparedStatement ps4 = connection.prepareStatement("ALTER TABLE veilingitem DROP COLUMN itemindex;");
                PreparedStatement ps5 = connection.prepareStatement("ALTER TABLE veilingitem ADD itemindex INT( 100 ) NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST")) {
            ps4.executeUpdate();
            ps5.executeUpdate();
            main.getStoredDataHandler().getMaxIndex(connection).thenAccept(main::setMaxindex);
        }
    }
}
