/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
 *
 * Parts of this file are adapted from QuartzLib (https://github.com/zDevelopers/QuartzLib),
 * Copyright or © or Copr. ZDevelopers (2015 – 2021), governed by the CeCILL-B license.
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.moribus.imageonmap.gui.framework;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.tools.RunTask;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Manages the GUIs opened by the players.
 */
public final class Gui {
    private static final Map<Player, GuiBase> openGuis = new HashMap<>();
    private static Listener quitListener = null;

    private Gui() {
    }

    /**
     * Initializes the GUI system.
     *
     * @param plugin The plugin.
     */
    public static void init(ImageOnMap plugin) {
        openGuis.clear();
        quitListener = new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                final GuiBase openGui = openGuis.get(event.getPlayer());
                if (openGui != null) {
                    openGui.registerClose();
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(quitListener, plugin);
    }

    /**
     * Closes the GUI system.
     */
    public static void exit() {
        for (GuiBase openGui : openGuis.values().toArray(new GuiBase[0])) {
            openGui.registerClose();
        }
        openGuis.clear();
        if (quitListener != null) {
            HandlerList.unregisterAll(quitListener);
            quitListener = null;
        }
    }

    /**
     * Opens a GUI for a player.
     *
     * @param owner  The player the GUI will be shown to.
     * @param gui    The GUI.
     * @param parent The parent of the newly created GUI. Can be null.
     * @param <T>    A GUI type.
     * @return The opened GUI.
     */
    public static <T extends GuiBase> T open(final Player owner, final T gui, final GuiBase parent) {
        final GuiBase openGui = openGuis.get(owner);
        if (openGui != null) {
            openGui.registerClose();
        }
        if (parent != null) {
            gui.setParent(parent);
        }

        RunTask.later(() -> gui.open(owner), 0);
        return gui;
    }

    public static <T extends GuiBase> T open(Player owner, T gui) {
        return open(owner, gui, null);
    }

    static void registerEvents(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, ImageOnMap.getPlugin());
    }

    static void unregisterEvents(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    static void registerGuiOpen(Player player, GuiBase gui) {
        openGuis.put(player, gui);
    }

    static void registerGuiClose(GuiBase gui) {
        openGuis.remove(gui.getPlayer(), gui);
    }
}
