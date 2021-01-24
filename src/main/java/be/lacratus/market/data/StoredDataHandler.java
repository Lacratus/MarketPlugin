package be.lacratus.market.data;

import be.lacratus.market.Market;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
import be.lacratus.market.util.SortByIndexAscending;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
    private DDGSpeler target;

    public StoredDataHandler(Market main) {
        this.main = main;
    }

    public CompletableFuture<Integer> getMaxIndex(Connection connection) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                try (PreparedStatement ps = connection.prepareStatement("SELECT MAX(itemindex) as maxindex FROM veilingitem");
                     ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int maxIndex = rs.getInt("maxindex");
                    return maxIndex;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }


    public void saveData(DDGSpeler data) {
        //Data wordt geupdate naar de database
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            try (Connection connection = main.openConnection();
                 PreparedStatement ps4 = connection.prepareStatement("UPDATE ddgspeler SET balance = " + main.getPlayerBank().get(data.getUuid()) + " WHERE uuid = '" + data.getUuid() + "';")) {
                ps4.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }


    public CompletableFuture<DDGSpeler> loadData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            //Eerste keer inloggen maakt een rij aan in database
            try (Connection connection = main.openConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT COUNT(uuid) FROM ddgspeler WHERE uuid = '" + uuid + "';");
                ResultSet rs = statement.executeQuery();
                rs.next();
                if (rs.getInt(1) == 0) {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO ddgspeler(uuid,balance) VALUES ('"
                            + uuid + "',DEFAULT)");
                    ps.executeUpdate();
                    ps.close();
                }
                return new DDGSpeler(uuid);
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
                        //Creation of veilingItem
                        int id = rs.getInt("itemindex");
                        UUID uuidOwner = UUID.fromString(rs.getString("uuidowner"));
                        Material material = Material.matchMaterial(rs.getString("material"));
                        int quantity = rs.getInt("quantity");
                        ItemStack itemStack = new ItemStack(material, quantity);
                        int timeOfDeletion = rs.getInt("timeofdeletion");


                        VeilingItem veilingItem = new VeilingItem(id, itemStack, uuidOwner, timeOfDeletion);
                        UUID uuidBidder = UUID.fromString(rs.getString("uuidbidder"));
                        veilingItem.setUuidBidder(uuidBidder);
                        veilingItem.setHighestOffer(rs.getInt("highestoffer"));
                        veilingItem.setBumped(rs.getBoolean("Bumped"));
                        //Creation of  DDGspeler who have items on auctionhouse
                        DDGSpeler bieder;
                        if (uuidBidder.toString().equals(uuidOwner.toString())) {
                            if (main.getOnlinePlayers().containsKey(uuidBidder)) {
                                bieder = main.getOnlinePlayers().get(uuidBidder);
                            } else if (main.getPlayersWithItems().containsKey(uuidBidder)) {
                                bieder = main.getPlayersWithItems().get(uuidBidder);
                            } else if (main.getPlayersWithBiddings().containsKey(uuidBidder)) {
                                bieder = main.getPlayersWithBiddings().get(uuidBidder);
                            } else {
                                bieder = new DDGSpeler(uuidBidder);
                            }
                            bieder.getPersoonlijkeItems().add(veilingItem);
                            bieder.getBiddenItems().add(veilingItem);
                            main.updateLists(bieder);
                            //main.getPlayersWithItems().put(uuidOwner, bieder);
                            //main.getPlayersWithBiddings().put(uuidBidder, bieder);
                        } else {
                            DDGSpeler eigenaar;
                            if (main.getPlayersWithItems().containsKey(uuidOwner)) {
                                eigenaar = main.getPlayersWithItems().get(uuidOwner);
                            } else {
                                eigenaar = new DDGSpeler(uuidOwner);
                                main.getPlayersWithItems().put(uuidOwner, eigenaar);
                            }
                            eigenaar.getPersoonlijkeItems().add(veilingItem);

                            //Creation of DDGspeler who have bidden items
                            if (main.getPlayersWithItems().containsKey(uuidBidder)) {
                                bieder = main.getPlayersWithItems().get(uuidBidder);
                            } else if (main.getPlayersWithBiddings().containsKey(uuidBidder)) {
                                bieder = main.getPlayersWithBiddings().get(uuidBidder);
                            } else {
                                bieder = new DDGSpeler(uuidBidder);
                                main.getPlayersWithBiddings().put(uuidBidder, bieder);
                            }
                            bieder.getBiddenItems().add(veilingItem);
                        }
                        main.getVeilingItems().add(veilingItem);
                        main.runTaskGiveItem(veilingItem, bieder, timeOfDeletion);
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

    public Runnable saveAuctionHouseAndBalance() {
        return () -> {
            System.out.println("SAVED");

            try (Connection connection = main.openConnection()) {
                //Veilingitems opslaan
                if (!main.getVeilingItems().isEmpty()) {
                    List<VeilingItem> veilingItems = new ArrayList<>(main.getVeilingItems());
                    veilingItems.sort(new SortByIndexAscending());
                    for (VeilingItem item : veilingItems) {
                        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(itemindex) FROM veilingitem WHERE itemindex = '" + item.getId() + "';");
                             ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            System.out.println("SAVED: " + rs.getInt(1));
                            if (rs.getInt(1) == 0) {
                                System.out.println("Added new item " + item.getId());
                                try (PreparedStatement ps2 = connection.prepareStatement("INSERT INTO veilingitem(highestoffer,uuidbidder,uuidowner,timeofdeletion,material,quantity,bumped) VALUES (" + item.getHighestOffer() +
                                        ", '" + item.getUuidBidder() +
                                        "', '" + item.getUuidOwner() +
                                        "', " + (item.getTimeOfDeletion() - (System.currentTimeMillis() / 1000)) +
                                        ", '" + item.getItemStack().getType().toString() +
                                        "', " + item.getItemStack().getAmount() +
                                        ", '" + item.isBumped() + "') ")) {

                                    ps2.executeUpdate();
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                System.out.println("Updated item " + item.getId());
                                try (PreparedStatement ps3 = connection.prepareStatement("UPDATE veilingitem SET highestoffer =  " + item.getHighestOffer() + ", uuidbidder = '" + item.getUuidBidder()
                                        + "',uuidowner = '" + item.getUuidOwner() + "',timeofdeletion = " + (item.getTimeOfDeletion() - System.currentTimeMillis() / 1000) + ", material = '" + item.getItemStack().getType()
                                        + "',quantity = " + item.getItemStack().getAmount() + "',bumped = " + item.isBumped() + "' WHERE itemindex = '" + item.getId() + "';")) {
                                    ps3.executeUpdate();
                                }
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //Veilingitems verwijderen
                if (!main.getItemsRemoveDatabase().isEmpty()) {
                    List<VeilingItem> removeVeilingItems = new ArrayList<>(main.getItemsRemoveDatabase());
                    //removeVeilingItems.sort(new SortByIndex());
                    List<VeilingItem> toRemove = new ArrayList<>();
                    for (VeilingItem item : removeVeilingItems) {
                        if (item.getId() != 0) {
                            System.out.println("Er wordt een item verwijderd: " + item.getId());
                            try (PreparedStatement ps1 = connection.prepareStatement("DELETE FROM veilingitem WHERE itemindex = " + item.getId() + ";")) {
                                ps1.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        toRemove.add(item);
                    }
                    main.getItemsRemoveDatabase().removeAll(toRemove);
                }

                //Balance opslaan
                for (Map.Entry pl : main.getPlayerBank().entrySet())
                    try (PreparedStatement ps4 = connection.prepareStatement("UPDATE ddgspeler SET balance = " + pl.getValue() + " WHERE uuid = '" + pl.getKey() + "';")) {
                        ps4.executeUpdate();
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
