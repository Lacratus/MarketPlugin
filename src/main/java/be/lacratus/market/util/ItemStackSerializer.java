package be.lacratus.market.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ItemStackSerializer {
    @SuppressWarnings("deprecation")
    public static String ItemStackToString(ItemStack itemStack) {
        String serialization = null;

        if (itemStack != null) {
            StringBuilder serializedItemStack = new StringBuilder();

            String itemStackType = String.valueOf(itemStack.getType().getId());
            serializedItemStack.append("Item@").append(itemStackType);

            if (itemStack.getDurability() != 0) {
                String itemStackDurability = String.valueOf(itemStack.getDurability());
                serializedItemStack.append(":d@").append(itemStackDurability);
            }

            if (itemStack.getAmount() != 1) {
                String itemStackAmount = String.valueOf(itemStack.getAmount());
                serializedItemStack.append(":a@").append(itemStackAmount);
            }

            if (itemStack.getItemMeta().getDisplayName() != null) {
                String itemStackName = itemStack.getItemMeta().getDisplayName();
                serializedItemStack.append(":n@").append(itemStackName);
            }

            Map<Enchantment, Integer> itemStackEnch = itemStack.getEnchantments();
            if (itemStackEnch.size() > 0) {
                for (Entry<Enchantment, Integer> ench : itemStackEnch.entrySet()) {
                    serializedItemStack.append(":e@").append(ench.getKey().getId()).append("@").append(ench.getValue());
                }
            }

            List<String> lores = itemStack.getItemMeta().getLore();
            if (lores != null && lores.size() > 0) {
                for (String lore : lores) {
                    serializedItemStack.append(":l@").append(lore);
                }
            }


            if (itemStack.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta bookmeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
                Map<Enchantment, Integer> enchantments = bookmeta.getStoredEnchants();
                if (enchantments.size() > 0) {
                    for (Entry<Enchantment, Integer> bookenchants : enchantments.entrySet()) {
                        serializedItemStack.append(":m@").append(bookenchants.getKey().getId()).append("@").append(bookenchants.getValue());
                    }
                }
            }

            serialization = serializedItemStack.toString();
        }
        return serialization;
    }

    @SuppressWarnings("deprecation")
    public static ItemStack StringToItemStack(String serializedItem) {
        ItemStack itemStack = null;
        boolean createdItemStack = false;
        String[] serializedItemStack = serializedItem.split(":");
        for (String itemInfo : serializedItemStack) {
            String[] itemAttribute = itemInfo.split("@");
            if (itemAttribute[0].equals("Item")) {
                itemStack = new ItemStack(Material.getMaterial(Integer.parseInt(itemAttribute[1])));
                createdItemStack = true;
            } else if (itemAttribute[0].equals("d") && createdItemStack) {
                itemStack.setDurability(Short.parseShort(itemAttribute[1]));
            } else if (itemAttribute[0].equals("a") && createdItemStack) {
                itemStack.setAmount(Integer.parseInt(itemAttribute[1]));
            } else if (itemAttribute[0].equals("e") && createdItemStack) {
                itemStack.addEnchantment(Enchantment.getById(Integer.parseInt(itemAttribute[1])), Integer.parseInt(itemAttribute[2]));
            } else if (itemAttribute[0].equals("n") && createdItemStack) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(itemAttribute[1]);
                itemStack.setItemMeta(itemMeta);
            } else if (itemAttribute[0].equals("l") && createdItemStack) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                List<String> lores = new ArrayList<>();
                lores.add(itemAttribute[1]);
                itemMeta.setLore(lores);
                itemStack.setItemMeta(itemMeta);

            } else if (itemAttribute[0].equals("m") && createdItemStack) {
                EnchantmentStorageMeta itemStackMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
                itemStackMeta.addStoredEnchant(Enchantment.getById(Integer.parseInt(itemAttribute[1])), Integer.parseInt(itemAttribute[2]), true);
                itemStack.setItemMeta(itemStackMeta);
            }
        }
        return itemStack;
    }
}