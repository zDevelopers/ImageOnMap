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

import fr.moribus.imageonmap.*;
import org.bukkit.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * This class implements an action-based GUI.
 * Actions are buttons which trigger an event when getting clicked on by the user.
 * They are represented by (customizable) items, which are immutable by the user.
 * 
 * Events handlers are (usually private) methods implemented in the derived 
 * class(es). They are named using the pattern 'action_[action name]', and 
 * are called when the associated action is triggered. They take an optional
 * argument (add it if you need it): the {@link InventoryClickEvent} triggered.
 *
 * @author ProkopyL (main) and Amaury Carrade
 */
abstract public class ActionGui extends Gui
{
    /**
     * The prefix for action handlers.
     */
    static private final String ACTION_HANDLER_NAME = "action_";
    
    /**
     * The class of this GUI.
     * Useful to retrieve methods from the derived classes.
     */
    private final Class<? extends ActionGui> guiClass = this.getClass();
    
    /**
     * A map containing all the actions defined by the derived class, indexed by
     * their position in the inventory.
     */
    private final HashMap<Integer, Action> actions = new HashMap<>();


    /* ===== Protected API ===== */
    
    /**
     * Creates a new action, represented by the given item.
     * The item's metadata is changed to use the given title and lore.
     *
     * @param name The identifier of the action.
     * @param slot The slot the action will be placed on.
     * @param material The material used to represent the action.
     * @param title The title the item will show.
     * @param loreLines The lore the item will show.
     */
    protected void action(String name, int slot, Material material, String title, String ... loreLines)
    {
        action(name, slot, new ItemStack(material), title, Arrays.asList(loreLines));
    }
    
    /**
     * Creates a new action, represented by the given item.
     * The item's metadata is changed to use the given title and lore.
     *
     * @param name The identifier of the action.
     * @param slot The slot the action will be placed on.
     * @param item The item used to represent the action.
     * @param title The title the item will show.
     * @param loreLines The lore the item will show.
     */
    protected void action(String name, int slot, ItemStack item, String title, String ... loreLines)
    {
        action(name, slot, item, title, Arrays.asList(loreLines));
    }
    
    /**
     * Creates a new action, represented by the given item.
     * The item's metadata is changed to use the given title and lore.
     *
     * @param name The identifier of the action.
     * @param slot The slot the action will be placed on.
     * @param item The item used to represent the action.
     * @param title The title the item will show.
     * @param loreLines The lore the item will show.
     */
    protected void action(String name, int slot, ItemStack item, String title, List<String> loreLines)
    {
        action(name, slot, GuiUtils.makeItem(item, title, loreLines));
    }
    
    /**
     * Creates a new action, represented by the given material.
     *
     * @param name The identifier of the action.
     * @param slot The slot the action will be placed on.
     * @param material The material used to represent the action.
     */
    protected void action(String name, int slot, Material material)
    {
        action(name, slot, GuiUtils.makeItem(material));
    }
    
    /**
     * Creates a new action, represented by no item.
     * This action will not be rendered to the user until
     * {@link #updateAction(java.lang.String, org.bukkit.inventory.ItemStack, java.lang.String)}
     * is called.
     *
     * @param name The identifier of the action.
     * @param slot The slot the action will be placed on.
     */
    protected void action(String name, int slot)
    {
        action(name, slot, (ItemStack) null);
    }
    
    /**
     * Creates a new action, and adds it to the GUI.
     *
     * @param name The identifier of the action.
     * @param slot The slot the action will be placed on.
     * @param item The item used to represent the action.
     */
    protected void action(String name, int slot, ItemStack item)
    {
        if(slot > getSize() || slot < 0) 
            throw new IllegalArgumentException("Illegal slot ID");
        
        action(new Action(name, slot, item, getActionHandler(guiClass, name)));
    }
    
    /**
     * Adds an action to the GUI.
     *
     * @param action The {@link fr.moribus.imageonmap.guiproko.core.ActionGui.Action} to register.
     */
    private void action(Action action)
    {
        actions.put(action.slot, action);
    }
    
    /**
     * Updates the action represented by the given name.
     *
     * @param name The name of the action to update.
     * @param item The new material to affect to the action.
     * @param title The new title to affect to the action.
     * @throws IllegalArgumentException If no action has the given name.
     */
    protected void updateAction(String name, Material item, String title)
    {
        updateAction(name, new ItemStack(item), title);
    }
    
    /**
     * Updates the action represented by the given name.
     *
     * @param name The name of the action to update.
     * @param item The new item to affect to the action.
     * @param title The new title to affect to the action.
     * @throws IllegalArgumentException If no action has the given name.
     */
    protected void updateAction(String name, ItemStack item, String title)
    {
        updateAction(name, item);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        item.setItemMeta(meta);
    }

    /**
     * Updates the action represented by the given name.
     *
     * @param name The name of the action to update.
     * @param item The new item to affect to the action.
     * @throws IllegalArgumentException If no action has the given name.
     */
    protected void updateAction(String name, ItemStack item)
    {
        getAction(name).item = item;
    }

    /**
     * Retrieves the action represented by the given name.
     *
     * @param name The name of the action to retreive.
     * @return The action represented by the given name.
     * @throws IllegalArgumentException If no action has the given name.
     */
    private Action getAction(String name) throws IllegalArgumentException
    {
        for(Action action : actions.values())
        {
            if(action.name.equals(name)) return action;
        }
        throw new IllegalArgumentException("Unknown action name : " + name);
    }
    
    /**
     * Raised when the Gui needs to be updated.
     * Use this method to create your actions.
     */
    @Override
    protected abstract void onUpdate();
    
    /**
     * Raised when an action without any event handler has been triggered.
     *
     * @param name The name of the triggered action.
     * @param slot The slot of the action.
     * @param item The item of the action.
     * @param event The {@link InventoryClickEvent} raised when this action was triggered.
     */
    protected void unknown_action(String name, int slot, ItemStack item, InventoryClickEvent event)
    {
        unknown_action(name, slot, item);
    }

    /**
     * Raised when an action without any event handler has been triggered.
     *
     * @param name The name of the triggered action.
     * @param slot The slot of the action.
     * @param item The item of the action.
     */
    protected void unknown_action(String name, int slot, ItemStack item) {}


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
        if(event.getRawSlot() >= event.getInventory().getSize()) //The user clicked in its own inventory
        {
            if(!event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
                return;
        }

        event.setCancelled(true);
        
        callAction(actions.get(event.getRawSlot()));
    }
    
    /**
     * Triggers the given action's event handler.
     * @param action The action to trigger.
     */
    private void callAction(Action action)
    {
        if(action == null) return;

        if(action.callback == null)
        {
            unknown_action(action.name, action.slot, action.item);
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
    
    /**
     * Retrieves the event handler matching the given name from a class (or any of its parents).
     *
     * @param klass The class to retrieve the event handler from.
     * @param name The name of the action.
     * @return The event handler matching the action name, or null if none was found.
     */
    private Method getActionHandler(Class<?> klass, String name)
    {
        Method callback;

        do
        {
            try
            {
                try
                {
                    callback = klass.getDeclaredMethod(ACTION_HANDLER_NAME + name, InventoryClickEvent.class);
                }
                catch(NoSuchMethodException e)
                {
                    callback = klass.getDeclaredMethod(ACTION_HANDLER_NAME + name);
                }

                callback.setAccessible(true);
                break;
            }
            catch (Throwable ex)
            {
                callback = null;
                klass = klass.getSuperclass();
            }
        } while (klass != null);
        
        return callback;
    }
    
    /**
     * @return if this GUI has any actions defined.
     */
    protected boolean hasActions()
    {
        return !actions.isEmpty();
    }
    
    /**
     * This structure represents an action.
     */
    static private class Action
    {
        /**
         * The name of the action.
         */
        public String name;
        /**
         * The slot the action will be put in.
         */
        public int slot;
        /**
         * The item this action will be represented by.
         */
        public ItemStack item;
        /**
         * The callback this action will call when triggered.
         */
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
