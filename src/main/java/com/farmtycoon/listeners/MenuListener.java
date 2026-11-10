package com.farmtycoon.listeners;

import com.farmtycoon.FarmTycoon;
import com.farmtycoon.gui.IslandMenu;
import com.farmtycoon.gui.IslandMenuHolder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public final class MenuListener implements Listener {
    private final FarmTycoon plugin;private final IslandMenu menu;
    public MenuListener(FarmTycoon p,IslandMenu m){plugin=p;menu=m;}
    @EventHandler public void onClick(InventoryClickEvent e){
        if(!(e.getInventory().getHolder() instanceof IslandMenuHolder))return;
        e.setCancelled(true);if(!(e.getWhoClicked() instanceof Player p))return;
        if(e.getRawSlot()<0 || e.getRawSlot()>=e.getView().getTopInventory().getSize())return;
        ItemStack item=e.getCurrentItem();if(item==null||!item.hasItemMeta())return;
        Material type=item.getType();
        if(type==Material.BARRIER){p.closeInventory();return;}
        if(type==Material.LIME_CONCRETE){p.closeInventory();plugin.islands().performUpgrade(p);menu.open(p);}
    }
}
