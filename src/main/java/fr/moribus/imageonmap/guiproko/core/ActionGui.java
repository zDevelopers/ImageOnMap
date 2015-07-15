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

import fr.moribus.imageonmap.PluginLogger;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

abstract public class ActionGui extends Gui
{
    static private final String ACTION_HANDLER_NAME = "action_";
    private final Class<? extends ActionGui> guiClass = this.getClass();
    private final HashMap<Integer, Action> actions = new HashMap<>();
    private int maxSlot;
    
    /* ===== Protected API ===== */
    
    protected void action(String name, int slot, Material material, String title, String ... loreLines)
    {
        action(name, slot, new ItemStack(material), title, Arrays.asList(loreLines));
    }
    
    protected void action(String name, int slot, ItemStack item, String title, String ... loreLines)
    {
        action(name, slot, item, title, Arrays.asList(loreLines));
    }
    
    protected void action(String name, int slot, ItemStack item, String title, List<String> loreLines)
    {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        meta.setLore(loreLines);
        item.setItemMeta(meta);
        
        Method callback;
        try
        {
            callback = guiClass.getDeclaredMethod(ACTION_HANDLER_NAME + name);
            callback.setAccessible(true);
        }
        catch (Throwable ex)
        {
            callback = null;
        }
        
        if(slot > getSize() || slot < 0) 
            throw new IllegalArgumentException("Illegal slot ID");
        
        action(new Action(name, slot, item, callback));
    }
    
    private void action(Action action)
    {
        actions.put(action.slot, action);
        if(maxSlot < action.slot) maxSlot = action.slot;
    }
    
    @Override
    protected abstract void onUpdate();
    
    @Override
    public void update()
    {
        actions.clear();
        super.update();
    }
    
    @Override
    protected void populate(Inventory inventory)
    {
        for(Action action : actions.values())
        {
            inventory.setItem(action.slot, action.item);
        }
    }

    @Override
    protected void onClick(InventoryClickEvent event)
    {
        event.setCancelled(true);
        
        Action action = actions.get(event.getRawSlot());
        if(action == null || action.callback == null) return;
        try
        {
            action.callback.invoke(this);
        }
        catch (IllegalAccessException | IllegalArgumentException ex)
        {
            PluginLogger.error("Could not invoke GUI action handler", ex);
        }
        catch (InvocationTargetException ex)
        {
            PluginLogger.error("Error while invoking action handler {0} of GUI {1}", ex, action.name, guiClass.getName());
        }
    }
    
    
    static private class Action
    {
        public String name;
        public int slot;
        public ItemStack item;
        public Method callback;
        
        public Action(String name, int slot, ItemStack item, Method callback)
        {
            this.name = name;
            this.slot = slot;
            this.item = item;
            this.callback = callback;
        }
    }
}
