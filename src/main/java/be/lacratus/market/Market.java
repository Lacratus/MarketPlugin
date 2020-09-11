package be.lacratus.market;

import be.lacratus.market.API.EconomyImplementer;
import be.lacratus.market.commands.MarketCommand;
import be.lacratus.market.commands.MoneyCommand;
import be.lacratus.market.data.StoredDataHandler;
import be.lacratus.market.listeners.AuctionListener;
import be.lacratus.market.listeners.OnDisconnectListener;
import be.lacratus.market.listeners.OnJoinListener;
import be.lacratus.market.objects.AuctionHouse;
import be.lacratus.market.objects.VeilingItem;
import net.milkbowl.vault.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import javax.swing.undo.AbstractUndoableEdit;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class Market extends JavaPlugin {

    private final HashMap<UUID, Double> playerBank = new HashMap<>();

    //Database
    private static Connection connection;
    private String host;
    private String database;
    private String username;
    private String password;
    private int port;

    //Handlers
    private StoredDataHandler storedDataHandler;

    private Economy econ;
    private EconomyImplementer economyImplementer;

    //
    private List<VeilingItem> veilingItems;


    @Override
    public void onEnable() {
        //Commands
        getCommand("market").setExecutor(new MarketCommand(this));
        getCommand("money").setExecutor(new MoneyCommand(this));

        //Listeners
        Bukkit.getPluginManager().registerEvents(new OnJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new OnDisconnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AuctionListener(this), this);


        //Databank creation

        veilingItems = new ArrayList<>();
        //VeilingItems opvullen

        /*for(int i = 0; i < 223 ;i++){
            ItemStack itemStack = new ItemStack(Material.ENDER_PEARL,1);
            veilingItems.add(new VeilingItem(itemStack));
        }*/


        //Register VaultEconomy
        economyImplementer = new EconomyImplementer(this);
        Bukkit.getServicesManager().register(Economy.class, economyImplementer, this, ServicePriority.Normal);


        if (!setupEconomy()) {
            System.out.println("No Vault Dependency Found");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onDisable() {
        //Unregister VaultEconomy
        Bukkit.getServicesManager().unregister(Economy.class, economyImplementer);
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            System.out.println("1");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            System.out.println("2");
            return false;
        }
        Economy econ = rsp.getProvider();
        return econ != null;
    }

    public static Connection getConnection() {
        return connection;
    }

    public StoredDataHandler getStoredDataHandler() {
        return storedDataHandler;
    }

    public HashMap<UUID, Double> getPlayerBank() {
        return playerBank;
    }

    public List<VeilingItem> getVeilingItems() {
        return veilingItems;
    }

    public Economy getEcon() {
        return econ;
    }

    public EconomyImplementer getEconomyImplementer() {
        return economyImplementer;
    }
}
