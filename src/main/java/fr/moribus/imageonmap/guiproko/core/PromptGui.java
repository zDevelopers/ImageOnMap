/*
 * Copyright (C) 2013 Moribus
 * Copyright (C) 2015 ProkopyL <prokopylmc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.moribus.imageonmap.guiproko.core;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.PluginLogger;
import fr.moribus.imageonmap.ReflectionUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class PromptGui extends Gui
{
    static private final int SIGN_LINES_COUNT = 4;
    static private final int SIGN_COLUMNS_COUNT = 15;
    
    static private boolean isInitialized = false;
    
    /* ===== Reflection to Sign API ===== */
    static private Field fieldSign = null;//CraftSign.sign
    static private Method methodGetHandle = null;//CraftPlayer.getHandle()
    static private Method methodOpenSign = null;//EntityHuman.openSign()
    static private Class classTileEntitySign = null;//CraftBlock.class
    
    static public boolean isAvailable()
    {
        if(!isInitialized) init();
        return fieldSign != null;
    }
    
    static public void prompt(Player owner, Callback<String> callback)
    {
        prompt(owner, callback, "", null);
    }
    
    static public void prompt(Player owner, Callback<String> callback, String contents, Gui parent)
    {
        Gui.open(owner, new PromptGui(callback, contents), parent);
    }
    
    static private void init()
    {
        isInitialized = true;
        
        try
        {
            Class CraftSign = ReflectionUtils.getBukkitClassByName("block.CraftSign");
            classTileEntitySign = ReflectionUtils.getMinecraftClassByName("TileEntitySign");
            Class CraftPlayer = ReflectionUtils.getBukkitClassByName("entity.CraftPlayer");
            Class EntityHuman = ReflectionUtils.getMinecraftClassByName("EntityHuman");
            
            
            fieldSign = ReflectionUtils.getField(CraftSign, "sign");
            methodGetHandle = CraftPlayer.getDeclaredMethod("getHandle");
            methodOpenSign = EntityHuman.getDeclaredMethod("openSign", classTileEntitySign);
        }
        catch (Exception ex)
        {
            PluginLogger.error("Unable to initialize Sign Prompt API", ex);
            fieldSign = null;
        }
    }
    
    private final Callback<String> callback;
    private Location signLocation;
    private String contents;
    
    public PromptGui(Callback<String> callback, String contents)
    {
        this(callback);
        this.contents = contents;
    }
    
    public PromptGui(Callback<String> callback)
    {
        super();
        registerListener(PromptGuiListener.class);
        if(!isAvailable()) throw new IllegalStateException("Sign-based prompt GUI are not available");
        this.callback = callback;
    }
    
    @Override
    protected void open(final Player player)
    {
        super.open(player);
        
        signLocation = findAvailableLocation(player);
        Block block = player.getWorld().getBlockAt(signLocation);
        block.setType(Material.SIGN_POST, false);
        final Sign sign = (Sign) block.getState();
        setSignContents(sign, contents);
        sign.update();
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(ImageOnMap.getPlugin(), new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Object signTE = fieldSign.get(sign);
                    Object playerEntity = methodGetHandle.invoke(player);
                    methodOpenSign.invoke(playerEntity, signTE);
                }
                catch(Throwable ex)
                {
                    PluginLogger.error("Error while opening Sign prompt", ex);
                }
            }  
        }, 3);
        
    }
    
    @Override
    protected void onClose()
    {
        Block block = getPlayer().getWorld().getBlockAt(signLocation);
        block.setType(Material.AIR);
        super.onClose();
    }
    
    private void validate(String[] lines)
    {
        callback.call(getSignContents(lines));
        this.close(true);//Bukkit sends extra InventoryCloseEvents when closing a sign GUI...
    }
    
    static private String getSignContents(String[] lines)
    {
        String content = lines[0].trim();
        
        for(int i = 1; i < lines.length; i++)
        {
            if(lines[i] == null || lines[i].isEmpty()) continue;
            content += " " + lines[i].trim();
        }
        return content.trim();
    }
    
    static private void setSignContents(Sign sign, String content)
    {
        String[] lines = new String[SIGN_LINES_COUNT + 1];
        String curLine;
        int curLineIndex = 0, spacePos;
        
        if(content != null)
        {
            lines[0] = content;
            while(curLineIndex < SIGN_LINES_COUNT)
            {
                curLine = lines[curLineIndex];
                if(curLine.length() <= SIGN_COLUMNS_COUNT)
                    break;

                spacePos = curLine.lastIndexOf(' ', SIGN_COLUMNS_COUNT);
                if(spacePos < 0) break;
                lines[curLineIndex + 1] = curLine.substring(spacePos + 1);
                lines[curLineIndex] = curLine.substring(0, spacePos);
                curLineIndex++;
            }
        }
        
        for(int i = SIGN_LINES_COUNT; i --> 0;)
        {
            sign.setLine(i, lines[i]);
        }
    }
    
    static private Location findAvailableLocation(Player player)
    {
        World world = player.getWorld();
        Chunk playerChunk = player.getLocation().getChunk();
        Chunk firstChunk = world.getChunkAt(playerChunk.getX() - 1, playerChunk.getZ() - 1);
        Location firstLoc = firstChunk.getBlock(0, 255, 0).getLocation();
        Location loc;
        
        for(int i = 48; i --> 0;)
        {
            for(int j = 0; j --> -10;)
            {
                for(int k = 48; k --> 0;)
                {
                    loc = firstLoc.add(i, j, k);
                    if(hasSpace(world, loc))
                        return loc;
                }
            }
        }
        
        return null;
    }
    
    static private boolean hasSpace(World world, Location loc)
    {
        if(!Material.AIR.equals(world.getBlockAt(loc).getType()))
            return false;
        
        for(int i = 1; i --> -1;)
        {
            for(int j = 1; j --> -1;)
            {
                for(int k = 1; k --> -1;)
                {
                    if(!Material.AIR.equals(world.getBlockAt(
                            loc.getBlockX() + i, loc.getBlockY() + j, loc.getBlockZ() + k)
                            .getType()))
                        return false;
                }
            }
        }
            
        return true;
    }
    
    static private final class PromptGuiListener implements Listener
    {
        @EventHandler
        public void onSignChange(SignChangeEvent event)
        {
            PromptGui gui = Gui.getOpenGui(event.getPlayer(), PromptGui.class);
            if(gui == null) return;
            gui.validate(event.getLines());
        }
    }
}
