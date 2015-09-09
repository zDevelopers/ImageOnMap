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

import org.bukkit.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.util.*;


/**
 * This class implements an exploration GUI, allowing users to see a set of data
 * in a paginated view, and to manipulate it or get it (if the
 * {@link fr.moribus.imageonmap.guiproko.core.ExplorerGui.Mode#CREATIVE Creative} mode
 * is enabled for this GUI — enabled by default).
 *
 * This GUI supports both one- and two-dimensional contents; two-dimensional content is
 * represented by a one-dimension list and a width, or by {@link #getViewItem(int, int)}
 * if you override it (in this case you need to call {@link #setDataShape(int, int)} in the
 * {@link #onUpdate()} method).
 *
 * @param <T> The type of data this GUI will display.
 *
 * @author ProkopyL (main) and Amaury Carrade
 */
abstract public class ExplorerGui<T> extends ActionGui
{
    /**
     * The explorer GUI's reading mode.
     *
     * In creative mode (the default mode), the players are able to manipulate the content, get it
     * inside their own inventory (without consumption, just like the creative mode), and whatever
     * you want if you override some methods.
     *
     * In read-only mode, they are only able to browse the content.
     */
    protected enum Mode {READONLY, CREATIVE}
    
    private T[] data;
    private boolean isData2D = false;
    
    private int viewSize;
    private int viewHeight;
    private int viewWidth;
    
    private int currentPageX = 0;
    private int currentPageY = 0;
    
    private int dataHeight;
    private int dataWidth;
    
    private int pageCountX;
    private int pageCountY;
    
    private Mode mode = Mode.CREATIVE;

    /**
     * Sets the displayed data.
     *
     * @param data The data.
     * @param dataWidth The data's width, if this data is in two dimensions.
     *                  In this case the data array will be read like a matrix, with
     *                  lines stored in a consecutive way; the height is automatically
     *                  calculated.
     */
    protected void setData(T[] data, int dataWidth)
    {
        this.data = data;
        if(dataWidth > 0)
            setDataShape(dataWidth, (int) Math.ceil((double) data.length / (double) dataWidth));
    }

    /**
     * Sets the data's shape. Use this if you're providing data through
     * {@link #getViewItem(int, int)}, as example.
     *
     * @param dataWidth The data's width.
     * @param dataHeight The data's height.
     */
    protected void setDataShape(int dataWidth, int dataHeight)
    {
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        this.isData2D = dataWidth > 0;
    }

    /**
     * Sets the displayed data, assuming this data is in one dimension.
     *
     * @param data The data.
     */
    protected void setData(T[] data)
    {
        setData(data, 0);
    }
    
    @Override
    protected void populate(Inventory inventory)
    {
        if(pageCountX > 1)
        {
            updateAction("next", getPageItem("next", canGoNext()));
            updateAction("previous", getPageItem("previous", canGoPrevious()));
        }
        
        if(pageCountY > 1)
        {
            updateAction("up", getPageItem("up", canGoUp()));
            updateAction("down", getPageItem("down", canGoDown()));
        }
        
        if(!isData2D)
        {
            int start = currentPageX * viewSize;
            int max = Math.min(viewSize, data.length - start);

            for(int i = 0; i < max; i++)
            {
                inventory.setItem(i, getViewItem(i + start));
            }
        }
        else
        {
            int startX = currentPageX * viewWidth;
            int startY = currentPageY * viewHeight;
            
            int maxX = Math.min(viewWidth, dataWidth - startX);
            int maxY = Math.min(viewHeight, dataHeight - startY);
            
            for(int i = maxY; i --> 0;)
            {
                for(int j = maxX; j --> 0;)
                {
                    inventory.setItem(i * INVENTORY_ROW_SIZE + j, getViewItem(j + startX, i + startY));
                }
            }
        }

        if(hasActions()) super.populate(inventory);
    }
    
    @Override
    protected void onClick(InventoryClickEvent event)
    {
        int slot = event.getRawSlot();
        
        // Clicked in the action bar
        if(hasActions() && 
            slot >= MAX_INVENTORY_SIZE - INVENTORY_ROW_SIZE
            && slot < MAX_INVENTORY_SIZE)
        {
            super.onClick(event);
            return;
        }
        
        if(isData2D && pageCountY > 1 &&
                slot % INVENTORY_ROW_SIZE == INVENTORY_ROW_SIZE - 1)
        {
            super.onClick(event);
            return;
        }
        
        if(affectsGui(event)) // The user clicked in its own inventory
        {
            switch(event.getAction())
            {
                case PICKUP_ALL: case PICKUP_HALF: case PICKUP_ONE: case PICKUP_SOME:
                case HOTBAR_MOVE_AND_READD: case HOTBAR_SWAP:
                case MOVE_TO_OTHER_INVENTORY:
                    onActionPickup(event); break;

                case PLACE_ALL: case PLACE_ONE: case PLACE_SOME:
                case SWAP_WITH_CURSOR:
                    onActionPut(event); break;

                case DROP_ALL_CURSOR: case DROP_ONE_CURSOR: 
                    break;

                default:
                    event.setCancelled(true);
            }
        }
        else
        {
            if(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))
                onActionMove(event);
        }
    }
    
    @Override
    protected void onDrag(InventoryDragEvent event)
    {
        if(!affectsGui(event)) return;
        
        for(int slot : event.getRawSlots())
        {
            //Clicked in the action bar
            if(hasActions() && 
                slot >= MAX_INVENTORY_SIZE - INVENTORY_ROW_SIZE
                && slot < MAX_INVENTORY_SIZE)
            {
                super.onDrag(event);
                return;
            }

            if(isData2D && pageCountY > 1 &&
                    slot % INVENTORY_ROW_SIZE == INVENTORY_ROW_SIZE - 1)
            {
                super.onDrag(event);
                return;
            }
        }
        
        event.setCancelled(true);

        if(mode.equals(Mode.READONLY)) return;
        if(!onPutItem(event.getOldCursor())) return;

        event.setCursor(new ItemStack(Material.AIR));
    }

    /**
     * Triggered when a player clicks on the GUI to get an item.
     *
     * @param event The triggered event.
     */
    private void onActionPickup(InventoryClickEvent event)
    {
        int dataIndex = getDataIndex(event.getSlot());
        if(event.getClick().equals(ClickType.RIGHT))
        {
            onRightClick(getData(dataIndex));
            event.setCancelled(true);
            return;
        }

        ItemStack pickedUpItem = getPickedUpItem(dataIndex);
        if(pickedUpItem == null || mode.equals(Mode.READONLY))
        {
            event.setCancelled(true);
            return;
        }

        event.setCurrentItem(pickedUpItem);
        GuiUtils.setItemLater(this, event.getSlot(), getViewItem(dataIndex));
    }

    /**
     * Triggered when a player clicks on the GUI to place an item.
     *
     * @param event The triggered event.
     */
    private void onActionPut(InventoryClickEvent event)
    {
        event.setCancelled(true);
        if(mode.equals(Mode.READONLY)) return;
        if(!onPutItem(event.getCursor())) return;
        event.setCursor(new ItemStack(Material.AIR));
    }

    /**
     * Triggered when a player moves an item on the GUI.
     *
     * @param event The triggered event.
     */
    private void onActionMove(InventoryClickEvent event)
    {
        event.setCancelled(true);
        if(mode.equals(Mode.READONLY)) return;
        if(!onPutItem(event.getCurrentItem())) return;
        event.setCurrentItem(new ItemStack(Material.AIR));
    }
    
    @Override
    protected void onAfterUpdate()
    {
        //Calculating page count
        if(data != null && data.length <= 0)
        {
            viewWidth = INVENTORY_ROW_SIZE;
            viewHeight = 1;
            viewSize = viewWidth;
            
            pageCountX = 1;
            pageCountY = 1;
        }
        else if(!isData2D)
        {
            int dataLength = (data == null) ? 0 : data.length;

            viewWidth = INVENTORY_ROW_SIZE;
            viewHeight = Math.min((int)Math.ceil((double)dataLength / (double)viewWidth),
                                   MAX_INVENTORY_COLUMN_SIZE);
            
            if(hasActions() || dataLength > MAX_INVENTORY_SIZE)
                viewHeight--;
            viewSize = viewWidth * viewHeight;
            
            pageCountX = (int)Math.ceil((double)dataLength / (double)viewSize);
            pageCountY = 1;
        }
        else
        {
            viewWidth = Math.min(dataWidth, INVENTORY_ROW_SIZE);
            viewHeight = Math.min(dataHeight, MAX_INVENTORY_COLUMN_SIZE);
            
            pageCountX = (int)Math.ceil((double)dataWidth / (double)viewWidth);
            pageCountY = (int)Math.ceil((double)dataHeight / (double)viewHeight);
            
            if(pageCountY > 1 && viewWidth == INVENTORY_ROW_SIZE) viewWidth--;
            if(pageCountX > 1 && viewHeight == MAX_INVENTORY_COLUMN_SIZE) viewHeight--;
            
            pageCountX = (int)Math.ceil((double)dataWidth / (double)viewWidth);
            pageCountY = (int)Math.ceil((double)dataHeight / (double)viewHeight);
        }
        
        setSize(MAX_INVENTORY_SIZE);
        
        if(pageCountX > 1)
        {
            action("previous", MAX_INVENTORY_SIZE - INVENTORY_ROW_SIZE);
            action("next", MAX_INVENTORY_SIZE - 1 - (pageCountY > 1 ? 1 : 0));
        }
        if(pageCountY > 1)
        {
            action("up", INVENTORY_ROW_SIZE - 1);
            action("down", MAX_INVENTORY_SIZE - 1);
        }
    }
    
    private void action_next()
    {
        next();
    }
    
    private void action_previous()
    {
        previous();
    }
    
    private void action_up()
    {
        up();
    }
    
    private void action_down()
    {
        down();
    }
    
    private int getDataIndex(int inventorySlot)
    {
        if(isData2D)
        {
            int column = currentPageX * viewWidth + inventorySlot % INVENTORY_ROW_SIZE;
            int row = currentPageY * viewHeight + inventorySlot / INVENTORY_ROW_SIZE;
            return row * dataWidth + column;
        }
        else
        {
            return currentPageX * viewSize + inventorySlot;
        }
    }
    
    private T getData(int i)
    {
        if(i < 0 || i >= data.length)
            return null;

        return data[i];
    }

    /**
     * Returns the stack to display at the given index.
     *
     * @param i The index.
     * @return The stack.
     */
    protected ItemStack getViewItem(int i)
    {
        return getViewItem(getData(i));
    }

    /**
     * Returns the stack to display at the given coordinates.
     *
     * @param x The x-coordinate (left to right).
     * @param y The y-coordinate (top to bottom).
     * @return The stack.
     */
    protected ItemStack getViewItem(int x, int y)
    {
        return getViewItem(y * dataWidth + x);
    }

    /**
     * Returns the ItemStack representation of the given piece of data.
     *
     * @param data The piece of data.
     * @return The piece's representation.
     */
    protected ItemStack getViewItem(T data) { return null; }


    private ItemStack getPickedUpItem(int dataIndex)
    {
        if(dataIndex < 0 || dataIndex >= data.length)
            return null;

        return getPickedUpItem(getData(dataIndex));
    }

    /**
     * Returns the stack the players will get when they try to take an item from
     * the GUI, in {@link fr.moribus.imageonmap.guiproko.core.ExplorerGui.Mode#CREATIVE}
     * mode.
     *
     * @param data The picked-up piece of data.
     * @return The stack to pick-up ({@code null} to cancel the pick-up).
     */
    protected ItemStack getPickedUpItem(T data) { return getViewItem(data); }

    /**
     * Returns the item to use to display the pagination buttons.
     *
     * @param paginationButtonType The type of button (either "next", "previous", "up", "down").
     * @param canUse {@code true} if the button is usable (i.e. not in the last or first page of
     *               its kind.
     * @return The item.
     */
    protected ItemStack getPageItem(String paginationButtonType, boolean canUse)
    {
        ItemStack icon = new ItemStack(canUse ? Material.ARROW : Material.STICK);
        ItemMeta meta = icon.getItemMeta();

        String title;
        Integer newPage;
        Integer lastPage;

        switch (paginationButtonType)
        {
            case "next":
                title = canUse ? "Next page" : "No next page";

                newPage = currentPageX + 1;
                lastPage = getPageCount();
                break;

            case "previous":
                title = canUse ? "Previous page" : "No previous page";

                newPage = currentPageX - 1;
                lastPage = getPageCount();
                break;

            case "up":
                title = canUse ? "Go up" : "Top page";

                newPage = currentPageY + 1;
                lastPage = getVerticalPageCount();
                break;

            case "down":
                title = canUse ? "Go down" : "Bottom page";

                newPage = currentPageY - 1;
                lastPage = getVerticalPageCount();
                break;

            default:
                return null; // invalid page type
        }

        meta.setDisplayName((canUse ? ChatColor.WHITE : ChatColor.GRAY) + title);

        if(canUse)
        {
            meta.setLore(Collections.singletonList(
                    ChatColor.GRAY + "Go to page " + ChatColor.WHITE + (newPage) + ChatColor.GRAY + " of " + ChatColor.WHITE + lastPage
            ));
        }

        icon.setItemMeta(meta);
        return icon;
    }

    /**
     * Triggered when the player right-clicks an item on the GUI.
     *
     * @param data The right-clicked piece of data.
     */
    protected void onRightClick(T data) {}

    /**
     * Triggered when the player try to place an item inside the GUI.
     *
     * This will not place the item in the GUI, it's up to you to update the data and refresh
     * the GUI if you need so.
     *
     * @param item The {@link ItemStack} the player is trying to put.
     * @return {@code false} to cancel the placement; {@code true} to accept it.
     */
    protected boolean onPutItem(ItemStack item) { return true; }

    /**
     * Displays the next horizontal page, if possible.
     */
    public void next()
    {
        if(!canGoNext()) return;
        currentPageX++;
        refresh();
    }

    /**
     * Displays the previous horizontal page, if possible.
     */
    public void previous()
    {
        if(!canGoPrevious()) return;
        currentPageX--;
        refresh();
    }

    /**
     * Displays the previous vertical page, if possible.
     */
    public void up()
    {
        if(!canGoUp()) return;
        currentPageY--;
        refresh();
    }

    /**
     * Displays the next vertical page, if possible.
     */
    public void down()
    {
        if(!canGoDown()) return;
        currentPageY++;
        refresh();
    }
    
    public boolean canGoNext()
    {
        return currentPageX < pageCountX - 1;
    }
    
    public boolean canGoPrevious()
    {
        return currentPageX > 0;
    }
    
    public boolean canGoUp()
    {
        return currentPageY > 0;
    }
    
    public boolean canGoDown()
    {
        return currentPageY < pageCountY - 1;
    }

    /**
     * Returns the amount of horizontal pages.
     *
     * @return The pages' amount.
     */
    public int getPageCount()
    {
        return pageCountX;
    }

    /**
     * Returns the amount of vertical pages.
     * This will always be 1 if the GUI is representing a one-dimensional data set.
     *
     * @return The pages' amount.
     */
    public int getVerticalPageCount()
    {
        return pageCountY;
    }

    /** @return The GUI's manipulation mode. */
    protected Mode getMode()
    {
        return mode;
    }

    /**
     * Sets the GUI's manipulation mode.
     * @param mode The mode.
     */
    protected void setMode(Mode mode)
    {
        this.mode = mode;
    }
}
