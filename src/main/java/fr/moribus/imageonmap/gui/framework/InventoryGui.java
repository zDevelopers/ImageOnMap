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

import fr.moribus.imageonmap.tools.RunTask;
import fr.moribus.imageonmap.tools.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/**
 * Base class of the inventory-based GUIs.
 */
public abstract class InventoryGui extends GuiBase {
    protected static final int INVENTORY_ROW_SIZE = 9;
    protected static final int MAX_INVENTORY_COLUMN_SIZE = 6;
    protected static final int MAX_INVENTORY_SIZE = INVENTORY_ROW_SIZE * MAX_INVENTORY_COLUMN_SIZE;
    protected static final int MAX_TITLE_LENGTH = 32;

    private int size = 0;
    private String title = "Inventory";
    private String inventoryTitle = null;
    private Inventory inventory;

    protected static boolean affectsGui(final InventoryDragEvent event) {
        for (int slot : event.getRawSlots()) {
            if (slot < event.getInventory().getSize()) {
                return true;
            }
        }
        return false;
    }

    protected static boolean affectsGui(final InventoryClickEvent event) {
        return event.getRawSlot() < event.getInventory().getSize();
    }

    /**
     * Refreshes the content of the inventory, without updating the GUI data.
     */
    public void refresh() {
        inventory.clear();
        populate(inventory);
    }

    @Override
    protected void open(final Player player) {
        super.open(player);
        player.openInventory(inventory);
    }

    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }

        // If close() is called manually, not from InventoryCloseEvent.
        // Ran on the next tick because it's unsafe to call Player.closeInventory() from an InventoryEvent.
        RunTask.nextTick(() -> {
            if (isDisplayedToPlayer()) {
                getPlayer().closeInventory();
            }
        });

        super.close();
    }

    protected abstract void populate(final Inventory inventory);

    protected abstract void onClick(final InventoryClickEvent event);

    protected void onDrag(final InventoryDragEvent event) {
        if (affectsGui(event)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void update() {
        super.update();

        final Player player = getPlayer();

        // The inventory is updated in place if the player is currently looking at it and if neither its title nor its
        // size changed; else, it is re-created.
        if (inventory != null && isDisplayedToPlayer() && title.equals(inventoryTitle)
                && inventory.getSize() == size) {
            refresh();
        } else {
            inventory = Bukkit.createInventory(player, size, Text.legacy(title));
            inventoryTitle = title;
            populate(inventory);

            if (isOpen()) {
                RunTask.nextTick(() -> {
                    player.closeInventory();
                    RunTask.nextTick(() -> player.openInventory(inventory));
                });
            }
        }
    }

    private boolean isDisplayedToPlayer() {
        if (inventory == null || getPlayer() == null) {
            return false;
        }
        final InventoryView openInventoryView = getPlayer().getOpenInventory();
        return inventory.equals(openInventoryView.getTopInventory());
    }

    protected final int getSize() {
        return size;
    }

    protected final void setSize(final int size) {
        this.size = Math.min(((int) (Math.ceil((double) size / INVENTORY_ROW_SIZE))) * INVENTORY_ROW_SIZE,
                MAX_INVENTORY_SIZE);
    }

    protected String getTitle() {
        return title;
    }

    protected void setTitle(String title) {
        if (title != null && title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH - 4) + "...";
        }
        this.title = title != null ? title : "";
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    protected Listener getEventListener() {
        return new InventoryGuiListener();
    }

    protected class InventoryGuiListener implements Listener {
        @EventHandler
        public void onInventoryDrag(final InventoryDragEvent event) {
            if (event.getWhoClicked() != getPlayer()) {
                return;
            }
            onDrag(event);
        }

        @EventHandler
        public void onInventoryClick(final InventoryClickEvent event) {
            if (event.getWhoClicked() != getPlayer()) {
                return;
            }
            onClick(event);
        }

        @EventHandler
        public void onInventoryClose(final InventoryCloseEvent event) {
            if (event.getPlayer() != getPlayer()) {
                return;
            }
            if (!event.getInventory().equals(inventory)) {
                return;
            }

            if (isOpen()) {
                if (checkImmune()) {
                    return;
                }
                close();
            }
        }
    }
}
