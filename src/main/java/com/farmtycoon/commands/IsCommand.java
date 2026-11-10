package com.farmtycoon.commands;

import com.farmtycoon.FarmTycoon;
import com.farmtycoon.island.IslandManager;
import com.farmtycoon.gui.IslandMenu;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public final class IsCommand implements CommandExecutor {
    private final FarmTycoon plugin;private final IslandManager islands;private final IslandMenu menu;
    public IsCommand(FarmTycoon p,IslandManager i,IslandMenu m){plugin=p;islands=i;menu=m;}
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args){
        if(!(sender instanceof Player p)){plugin.send(sender,"player-only");return true;}
        if(!p.hasPermission("farmtycoon.is")){plugin.send(p,"no-permission");return true;}
        if(args.length>0&&args[0].equalsIgnoreCase("upgrade")){menu.open(p);return true;}
        if(args.length>0){p.sendMessage("§eGunakan: /is atau /is upgrade");return true;}
        islands.teleportOrCreate(p);return true;
    }
}
