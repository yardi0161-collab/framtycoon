package com.farmtycoon.gui;

import com.farmtycoon.FarmTycoon;
import com.farmtycoon.storage.Database;
import com.farmtycoon.storage.PlayerData;
import com.farmtycoon.island.IslandManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import java.sql.SQLException;
import java.util.List;

public final class IslandMenu {
    private final FarmTycoon plugin;private final IslandManager islands;private final Database db;
    public IslandMenu(FarmTycoon p,IslandManager i,Database d){plugin=p;islands=i;db=d;}
    public void open(Player p){
        try{
            PlayerData data=db.getOrCreate(p.getUniqueId());Inventory inv=Bukkit.createInventory(new IslandMenuHolder(),27,"§2§lISLAND UPGRADE");
            fill(inv);inv.setItem(11,item(Material.GRASS_BLOCK,"§aCurrent Island",List.of("§7Level: §e"+data.islandLevel(),"§7Border: §f"+(int)islands.borderSize(data.islandLevel())+" x "+(int)islands.borderSize(data.islandLevel()),"§7Balance: §6$"+IslandManager.format(data.balance()))));
            if(data.islandLevel()<5){int n=data.islandLevel()+1;inv.setItem(13,item(Material.GOLDEN_HOE,"§eNext: Island Level "+n,List.of("§7Upgrade cost: §6$"+IslandManager.format(islands.cost(n)),"§7New border: §f"+(int)islands.borderSize(n)+" x "+(int)islands.borderSize(n),"","§aKlik untuk konfirmasi upgrade")));inv.setItem(15,item(Material.LIME_CONCRETE,"§aCONFIRM UPGRADE",List.of("§7Level berikutnya: §e"+n,"§7Biaya: §6$"+IslandManager.format(islands.cost(n)),"","§eKlik untuk membeli upgrade")));}else inv.setItem(13,item(Material.NETHER_STAR,"§6MAX LEVEL",List.of("§aPulau kamu sudah maksimal!")));
            inv.setItem(22,item(Material.BARRIER,"§cCLOSE",List.of("§7Tutup menu")));p.openInventory(inv);
        }catch(SQLException e){p.sendMessage("§cTidak bisa memuat data pulau.");}
    }
    private void fill(Inventory inv){ItemStack pane=item(Material.GRAY_STAINED_GLASS_PANE," ",List.of());for(int i=0;i<27;i++)inv.setItem(i,pane);}
    private ItemStack item(Material m,String name,List<String> lore){ItemStack s=new ItemStack(m);ItemMeta meta=s.getItemMeta();meta.setDisplayName(name);meta.setLore(lore);s.setItemMeta(meta);return s;}
}
