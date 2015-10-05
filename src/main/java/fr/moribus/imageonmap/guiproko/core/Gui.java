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
import java.lang.reflect.Constructor;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

abstract public class Gui 
{
    /**
     * The player this Gui instance is associated to.
     */
    private Player player;
    
    /**
     * The parent of this GUI (if any).
     */
    private Gui parent;
    
    /**
     * If the inventory is currently open.
     */
    private boolean open = false;
    
    /**
     * If the GUI is immune to close events.
     * Useful to filter out excessive InventoryCloseEvents Bukkit sends ...
     */
    private boolean immune = false;
    
    /* ===== Public API ===== */
    
    /**
     * Asks the GUI to update its data, and refresh its view accordingly.
     */
    public void update()
    {
        onUpdate();
        onAfterUpdate();
    }
    
    /**
     * Closes this inventory.
     */
    public void close()
    {
        close(false);
    }
    
    /**
     * Closes this inventory.
     * (Dirty hack edition)
     * @param immune if true, the parent of this GUI will be set immune when opened
     */
    protected void close(boolean immune)
    {
        onClose();
        
        if(parent != null)
           Gui.open(player, parent).setImmune(immune);
    }
    
    /* ===== Protected API ===== */
    
    /**
     * Raised when the {@link Gui#update()} method is called.
     * Use this method to update your internal data.
     */
    protected void onUpdate() {}
    
    /**
     * Raised when the {@link Gui#update()} method is called, but before the inventory is populated.
     * Use this method in a Gui subclass to analyze given data and set other parameters accordingly.
     */
    protected void onAfterUpdate() {}
    
    
    protected void onClose()
    {
        if(open == false) return;
        open = false;
        openGuis.remove(player);
    }
    
    protected void open(Player player)
    {
        this.player = player;
        openGuis.put(player, this);
        update();
        open = true;
    }
    
    /* ===== Getters & Setters ===== */
    
    /** @return If the GUI is currently open or not. */
    public final boolean isOpen() { return open; }
    
    /** @return The parent of this GUI. */
    public final Gui getParent() { return parent; }
    
    private void setParent(Gui parent)
    {
        if(parent == this)
            throw new IllegalArgumentException("A GUI cannot be its own parent.");
        this.parent = parent;
    }
    
    /** @return The player this Gui instance is associated to. */
    protected final Player getPlayer() { return player; }
    
    protected boolean checkImmune()
    {
        if(!immune) return false;
        immune = false;
        return true;
    }
    
    private void setImmune(boolean immune)
    {
        this.immune = immune;
    }
    
    /* ===== Static API ===== */
    
    /**
     * A map of all the currently open GUIs, associated to the HumanEntity
     * that requested it.
     */
    static private HashMap<Player, Gui> openGuis = null;
    
    /**
     * A map of all the currently registered GUIs listeners.
     */
    static private HashMap<Class<? extends Listener>, Listener> guiListeners = null;
    
    /**
     * The plugin that uses the GUI API.
     */
    static private Plugin plugin = null;
    
    /**
     * Initializes the GUI listeners.
     * This method must be called on plugin enabling.
     * @param plugin The plugin the GUI listeners will be registered on
     */
    static public final void init(Plugin plugin)
    {
        openGuis = new HashMap<>();
        guiListeners = new HashMap<>();
        Gui.plugin = plugin;
        GuiUtils.init();
    }
    
    /**
     * Cleans up the GUI states.
     * This method must be called on plugin disabling.
     */
    static public final void exit()
    {
        openGuis.clear();
        guiListeners.clear();
        openGuis = null;
        guiListeners = null;
    }
    
    static protected final void registerListener(Class<? extends Listener> listenerClass)
    {
        if(guiListeners == null || guiListeners.containsKey(listenerClass))
            return;
        
        try
        {
            Constructor<? extends Listener> constructor = listenerClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Listener listener = constructor.newInstance();
            guiListeners.put(listenerClass, listener);
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
        catch(Throwable ex)
        {
            PluginLogger.error("Could not register listener for GUI", ex);
        }
    }
    
    /**
     * Opens a GUI for a player.
     * @param <T> A GUI type.
     * @param owner The player the GUI will be shown to.
     * @param gui The GUI.
     * @param parent The parent of the newly created GUI. Can be null.
     * @return The opened GUI.
     */
    static public final <T extends Gui> T open(final Player owner, final T gui, final Gui parent)
    {
        Gui openGui = openGuis.get(owner);
        if(openGui != null) openGui.onClose();
        if(parent != null) ((Gui)gui).setParent(parent);
        
         Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run()
                {
                    ((Gui)gui).open(owner);/* JAVA GENERICS Y U NO WORK */
                }
            }, 0);
        
        return gui;
    }
    
    /**
     * Opens a GUI for a player.
     * @param <T> A GUI type.
     * @param owner The player the GUI will be shown to.
     * @param gui The GUI.
     * @return The opened GUI.
     */
    static public final <T extends Gui> T open(Player owner, T gui)
    {
        return open(owner, gui, null);
    }
    
    /**
     * Closes any open GUI for a given player.
     * @param owner The player.
     */
    static public final void close(Player owner)
    {
        Gui openGui = openGuis.get(owner);
        if(openGui != null) openGui.close();
    }
    
    static public final Gui getOpenGui(HumanEntity entity)
    {
        if(!(entity instanceof Player)) return null;
        return openGuis.get((Player) entity);
    }
    
    static public final <T extends Gui> T getOpenGui(HumanEntity entity, Class<T> guiClass)
    {
        Gui openGui = getOpenGui(entity);
        if(openGui == null) return null;
        if(!guiClass.isAssignableFrom(openGui.getClass())) return null;
        return (T) openGui;
    }
}
