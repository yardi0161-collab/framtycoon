package com.farmtycoon.island;

import com.farmtycoon.FarmTycoon;
import com.farmtycoon.storage.Database;
import com.farmtycoon.storage.PlayerData;
import com.farmtycoon.world.VoidGenerator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public final class IslandManager {
    private final FarmTycoon plugin; private final Database db;
    public IslandManager(FarmTycoon plugin, Database db){this.plugin=plugin;this.db=db;}
    public PlayerData data(UUID id) throws SQLException { return db.getOrCreate(id); }
    public double cost(int level){return plugin.getConfig().getDouble("island.upgrade-costs."+level,0);}
    public double borderSize(int level){return plugin.getConfig().getDouble("island.border-sizes."+level,32);}
    public int radius(int level){return plugin.getConfig().getInt("island.platform-radii."+level,8);}

    public void teleportOrCreate(Player player) {
        try {
            PlayerData d=db.getOrCreate(player.getUniqueId());
            boolean firstCreation = d.worldName()==null;
            String worldName = firstCreation ? plugin.getConfig().getString("island.world-prefix","ft_island_")+player.getUniqueId().toString().replace("-","") : d.worldName();
            World world=Bukkit.getWorld(worldName);
            if(world==null) world=new WorldCreator(worldName).generator(new VoidGenerator()).environment(World.Environment.NORMAL).createWorld();
            if(world==null){player.sendMessage("§cGagal membuat atau memuat world pulau.");return;}
            world.setKeepSpawnInMemory(true); world.setGameRule(GameRule.DO_MOB_SPAWNING,false); world.setGameRule(GameRule.DO_WEATHER_CYCLE,false); world.setStorm(false);
            if(firstCreation){
                int x=plugin.getConfig().getInt("island.center-x",0), y=plugin.getConfig().getInt("island.y",100), z=plugin.getConfig().getInt("island.center-z",0);
                buildPlatform(world,x,z,y,radius(d.islandLevel()));
                db.setIsland(player.getUniqueId(),worldName,x,y,z);
                player.sendMessage(plugin.message("island-created"));
                d=db.getOrCreate(player.getUniqueId());
            }
            world.getWorldBorder().setCenter(d.islandX()+0.5,d.islandZ()+0.5);world.getWorldBorder().setSize(borderSize(d.islandLevel()));
            world.setSpawnLocation(d.islandX()+0.5,d.islandY()+1,d.islandZ()+0.5);
            player.teleport(new Location(world,d.islandX()+0.5,d.islandY()+1,d.islandZ()+0.5));
            player.sendMessage(plugin.message("island-teleport"));
        } catch(Exception e){plugin.getLogger().severe("Island error: "+e.getMessage());player.sendMessage("§cTerjadi kesalahan saat membuka pulau.");}
    }
    private void buildPlatform(World world,int cx,int cz,int y,int r){
        for(int x=cx-r;x<=cx+r;x++)for(int z=cz-r;z<=cz+r;z++){
            world.getBlockAt(x,y-1,z).setType(Material.DIRT,false);
            world.getBlockAt(x,y,z).setType(Material.GRASS_BLOCK,false);
        }
        world.getBlockAt(cx,y+1,cz).setType(Material.OAK_LOG,false);
        world.getBlockAt(cx+1,y+1,cz).setType(Material.OAK_LEAVES,false);
        world.getBlockAt(cx,y+1,cz+1).setType(Material.OAK_LEAVES,false);
    }
    public boolean performUpgrade(Player player){
        try{
            PlayerData d=db.getOrCreate(player.getUniqueId());int next=d.islandLevel()+1;
            if(next>5){player.sendMessage(plugin.message("max-level"));return false;}
            double price=cost(next);
            if(d.balance()<price){player.sendMessage(plugin.message("not-enough-money").replace("%cost%",format(price)).replace("%balance%",format(d.balance())));return false;}
            if(!db.upgrade(player.getUniqueId(),next,price)){player.sendMessage("§cUpgrade gagal. Coba lagi.");return false;}
            World world=d.worldName()==null?null:Bukkit.getWorld(d.worldName());
            if(world!=null){int oldR=radius(next-1),newR=radius(next);expandPlatform(world,d.islandX(),d.islandZ(),d.islandY(),oldR,newR);world.getWorldBorder().setSize(borderSize(next));}
            player.sendMessage(plugin.message("island-upgraded").replace("%level%",String.valueOf(next)));
            return true;
        }catch(Exception e){plugin.getLogger().severe("Upgrade error: "+e.getMessage());player.sendMessage("§cUpgrade gagal karena kesalahan data.");return false;}
    }
    private void expandPlatform(World w,int cx,int cz,int y,int oldR,int newR){for(int x=cx-newR;x<=cx+newR;x++)for(int z=cz-newR;z<=cz+newR;z++){if(Math.abs(x-cx)<=oldR&&Math.abs(z-cz)<=oldR)continue;Block b=w.getBlockAt(x,y,z);if(b.getType()==Material.AIR){w.getBlockAt(x,y-1,z).setType(Material.DIRT,false);b.setType(Material.GRASS_BLOCK,false);}}}
    public static String format(double v){if(v>=1_000_000)return String.format(java.util.Locale.US,"%.1fM",v/1_000_000).replace(".0M","M");if(v>=1_000)return String.format(java.util.Locale.US,"%.1fK",v/1_000).replace(".0K","K");return String.format(java.util.Locale.US,"%,.0f",v);}
}
