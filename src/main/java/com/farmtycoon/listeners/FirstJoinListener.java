package com.farmtycoon.listeners;

import com.farmtycoon.FarmTycoon;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class FirstJoinListener implements Listener {
    private final FarmTycoon plugin;
    public FirstJoinListener(FarmTycoon plugin){this.plugin=plugin;}
    @EventHandler public void onJoin(PlayerJoinEvent event){
        Player player=event.getPlayer();
        try { plugin.database().getOrCreate(player.getUniqueId()); }
        catch(Exception ex){plugin.getLogger().warning("Tidak dapat membuat data pemain: "+ex.getMessage());}
        if(player.hasPlayedBefore()) return;
        Bukkit.getScheduler().runTask(plugin,()->{
            String worldName=plugin.getConfig().getString("spawn.world","world");World world=Bukkit.getWorld(worldName);
            if(world==null){plugin.getLogger().warning("World spawn '"+worldName+"' tidak ditemukan; pemain memakai spawn server.");return;}
            int x=plugin.getConfig().getInt("spawn.x",0),y=plugin.getConfig().getInt("spawn.y",80),z=plugin.getConfig().getInt("spawn.z",0);
            for(int bx=x-8;bx<=x+8;bx++)for(int bz=z-8;bz<=z+8;bz++){
                Block floor=world.getBlockAt(bx,y-1,bz);if(floor.getType()==Material.AIR)floor.setType(Material.GRASS_BLOCK,false);
                world.getBlockAt(bx,y,bz).setType(Material.AIR,false);
            }
            Location spawn=new Location(world,x+0.5,y,z+0.5, (float)plugin.getConfig().getDouble("spawn.yaw",0),(float)plugin.getConfig().getDouble("spawn.pitch",0));
            player.teleport(spawn);
        });
    }
}
