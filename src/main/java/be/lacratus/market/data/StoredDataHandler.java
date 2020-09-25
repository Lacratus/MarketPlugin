package be.lacratus.market.data;

import be.lacratus.market.Market;
import be.lacratus.market.objects.DDGSpeler;
import be.lacratus.market.objects.VeilingItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StoredDataHandler {

    private Market main;
    private DDGSpeler target;

    public StoredDataHandler(Market main) {
        this.main = main;


    }

    public Runnable saveData(DDGSpeler data) throws SQLException {
        //Data wordt geupdate naar de database
        Runnable runnable = () -> {
            try (Connection connection = main.openConnection()) {
                for (VeilingItem item : data.getRemoveItemsDatabase()) {
                    if (item.getId() != 0) {
                        try (PreparedStatement ps1 = connection.prepareStatement("DELETE * FROM veilingitem WHERE itemindex = '" + item.getId() + "';")) {
                            ps1.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
                for (VeilingItem item : data.getPersoonlijkeItems()) {
                    if (item.getId() != 0) {
                        try (PreparedStatement ps3 = connection.prepareStatement("UPDATE veilingitem SET highestoffer =  " + item.getHighestOffer() + ", uuidbidder = '" + item.getUuidBidder()
                                + "',uuidowner = '" + item.getUuidOwner() + "',timeofdeletion = " + item.getTimeOfDeletion() + ", material = '" + item.getItemStack().getType()
                                + "',quantity = " + item.getItemStack().getAmount() + ";")) {
                            ps3.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement ps2 = connection.prepareStatement("INSERT INTO veilingitem(highestoffer,uuidbidder,uuidowner,timeofdeletion,material,quantity) VALUES (" + item.getHighestOffer() +
                                ", '" + item.getUuidBidder() +
                                "', '" + item.getUuidOwner() +
                                "', " + item.getTimeOfDeletion() +
                                ", '" + item.getItemStack().getType().toString() +
                                "', " + item.getItemStack().getAmount() + ")")) {

                            ps2.executeUpdate();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }

                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };
        return runnable;
    }


    public CompletableFuture<DDGSpeler> loadData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            //Eerste keer inloggen maakt een rij aan in database
            try (Connection connection = main.openConnection();
                 ResultSet rs = connection.prepareStatement("SELECT COUNT(uuid) FROM ddgspeler WHERE uuid = '" + uuid + "';").executeQuery()) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO ddgspeler(uuid,balance) VALUES ('"
                            + uuid + "',DEFAULT)");
                    ps.executeUpdate();
                    ps.close();
                    target = new DDGSpeler(uuid);
                    //Alle persoonlijke veilingitems worden toegevoegd aan de speler
                }/* else {
                    try (ResultSet rs2 = connection.prepareStatement("SELECT * FROM veilingitem WHERE owneruuid = '" + uuid + "';").executeQuery()) {
                        DDGSpeler ddgSpeler = new DDGSpeler(uuid);
                        while (rs2.next()) {
                            String materialString = rs2.getString("material");
                            int quantity = rs2.getInt("quantity");
                            Material material = Material.matchMaterial(materialString);
                            ItemStack itemStack = new ItemStack(material, quantity);

                            UUID uuidbidder = UUID.fromString(rs2.getString("uuidbidder"));
                            long timeOfDeletion = (System.currentTimeMillis() / 1000) + rs2.getLong("timeofdeletion");
                            int highestOffer = rs2.getInt("highestoffer");


                            //creation of veilingitems
                            VeilingItem veilingItem = new VeilingItem(itemStack, uuid, timeOfDeletion);
                            veilingItem.setHighestOffer(highestOffer);
                            veilingItem.setUuidBidder(uuidbidder);
                            ddgSpeler.getPersoonlijkeItems().add(veilingItem);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return null;
                    }
                }*/
                return target;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public Runnable fillAuctionHouseAndBank() {
        return () -> {
            try (Connection connection = main.openConnection();
                 ResultSet rs = connection.prepareStatement("SELECT * FROM veilingitem").executeQuery();
                 ResultSet rs2 = connection.prepareStatement("SELECT * FROM ddgspeler").executeQuery()) {
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

                    //Creation of DDGspeler who have items on auctionhouse
                    DDGSpeler eigenaar;
                    if (main.getPlayersWithItems().containsKey(uuidOwner)) {
                        eigenaar = main.getPlayersWithItems().get(uuidOwner);
                    } else {
                        eigenaar = new DDGSpeler(uuidOwner);
                    }
                    eigenaar.getPersoonlijkeItems().add(veilingItem);

                    //Creation of DDGspeler who have bidden items
                    DDGSpeler bieder;
                    if (main.getPlayersWithItems().containsKey(uuidBidder)) {
                        bieder = main.getPlayersWithItems().get(uuidBidder);
                    } else if (main.getPlayersWithBiddings().containsKey(uuidBidder)) {
                        bieder = main.getPlayersWithBiddings().get(uuidBidder);
                    } else {
                        bieder = new DDGSpeler(uuidBidder);
                    }
                    bieder.getBiddenItems().add(veilingItem);
                    main.runTaskGiveItem(veilingItem, bieder, timeOfDeletion - (System.currentTimeMillis() / 1000));
                    main.getVeilingItems().add(veilingItem);
                }

                while (rs2.next()) {
                    UUID uuid = UUID.fromString(rs2.getString("uuid"));
                    double balance = rs2.getInt("balance");

                    main.getPlayerBank().put(uuid, balance);
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
    }
}
