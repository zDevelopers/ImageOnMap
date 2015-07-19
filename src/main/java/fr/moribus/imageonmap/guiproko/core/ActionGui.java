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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

abstract public class ActionGui extends Gui
{
    static private final String ACTION_HANDLER_NAME = "action_";
    private final Class<? extends ActionGui> guiClass = this.getClass();
    private final HashMap<Integer, Action> actions = new HashMap<>();
    
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
        
        action(name, slot, item);
    }
    
    protected void action(String name, int slot, Material material)
    {
        action(name, slot, new ItemStack(material));
    }
    
    protected void action(String name, int slot)
    {
        action(name, slot, (ItemStack)null);
    }
    
    protected void action(String name, int slot, ItemStack item)
    {
        if(slot > getSize() || slot < 0) 
            throw new IllegalArgumentException("Illegal slot ID");
        
        action(new Action(name, slot, item, getActionHandler(guiClass, name)));
    }
    
    
    private void action(Action action)
    {
        actions.put(action.slot, action);
    }
    
    protected void updateAction(String name, Material item, String title)
    {
        updateAction(name, new ItemStack(item), title);
    }
    
    protected void updateAction(String name, ItemStack item, String title)
    {
        Action action = getAction(name);
        action.item = item;
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        item.setItemMeta(meta);
    }
    
    private Action getAction(String name)
    {
        for(Action action : actions.values())
        {
            if(action.name.equals(name)) return action;
        }
        throw new IllegalArgumentException("Unknown action name : " + name);
    }
    
    @Override
    protected abstract void onUpdate();
    
    protected void onAction_unknown(String name, int slot, ItemStack item){}
    
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
        if(event.getRawSlot() >= event.getInventory().getSize())//The user clicked in its own inventory
        {
            if(!event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
                return;
        }
        event.setCancelled(true);
        
        callAction(actions.get(event.getRawSlot()));
    }
    
    private void callAction(Action action)
    {
        if(action == null) return;
        if(action.callback == null)
        {
            onAction_unknown(action.name, action.slot, action.item);
            return;
        }
        
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
            PluginLogger.error("Error while invoking action handler {0} of GUI {1}", 
                    ex.getCause(), action.name, guiClass.getName());
        }
    }
    
    private Method getActionHandler(Class klass, String name)
    {
        Method callback = null;
        do
        {
            try
            {
                callback = klass.getDeclaredMethod(ACTION_HANDLER_NAME + name);
                callback.setAccessible(true);
                break;
            }
            catch (Throwable ex)
            {
                callback = null;
                klass = klass.getSuperclass();
            }
            
        }while(klass != null);
        
        return callback;
    }
    
    protected boolean hasActions()
    {
        return !actions.isEmpty();
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
