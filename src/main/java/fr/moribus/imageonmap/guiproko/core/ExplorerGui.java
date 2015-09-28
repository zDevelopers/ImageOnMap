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

    private boolean keepHorizontalScrollingSpace = false;
    private boolean keepVerticalScrollingSpace = false;

    private int currentPageX = 0;
    private int currentPageY = 0;
    
    private int dataHeight = 0;
    private int dataWidth = 0;
    
    private int pageCountX;
    private int pageCountY;
    
    private Mode mode = Mode.CREATIVE;


    public ExplorerGui()
    {
        // Defined early to be able to use getSize() in the onUpdate method.
        setSize(MAX_INVENTORY_SIZE);
    }

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
        int dataLength = data == null ? 0 : data.length;
        if(dataWidth > 0)
            setDataShape(dataWidth, (int) Math.ceil((double) dataLength / (double) dataWidth));
        else
            setDataShape(0, dataLength);
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

    /**
     * Checks if this GUI contains data, either with the data entry or through
     * {@link #getViewItem(int, int)} and {@link #setDataShape(int, int)}.
     *
     * @return {@code true} if this GUI contains some data.
     */
    protected boolean hasData()
    {
        if(isData2D)
            return dataWidth > 0 && dataHeight > 0;
        else
            return data != null && data.length > 0;
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

        if(hasData())
        {
            if (!isData2D)
            {
                int start = currentPageX * viewSize;
                int max = Math.min(viewSize, data.length - start);

                for (int i = 0; i < max; i++)
                {
                    inventory.setItem(i, getViewItem(getData(i + start)));
                }
            }
            else
            {
                int startX = currentPageX * viewWidth;
                int startY = currentPageY * viewHeight;

                int maxX = Math.min(viewWidth, dataWidth - startX);
                int maxY = Math.min(viewHeight, dataHeight - startY);

                for (int i = maxY; i-- > 0; )
                {
                    for (int j = maxX; j-- > 0; )
                    {
                        inventory.setItem(i * INVENTORY_ROW_SIZE + j, getViewItem(j + startX, i + startY));
                    }
                }
            }
        }
        else
        {
            updateAction("__empty__", getEmptyViewItem());
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

        // The user clicked in the GUI
        if(affectsGui(event))
        {
            if(hasData())
            {
                switch (event.getAction())
                {
                    case PICKUP_ALL:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                    case PICKUP_SOME:
                    case HOTBAR_MOVE_AND_READD:
                    case HOTBAR_SWAP:
                    case MOVE_TO_OTHER_INVENTORY:
                        onActionPickup(event);
                        break;

                    case PLACE_ALL:
                    case PLACE_ONE:
                    case PLACE_SOME:
                    case SWAP_WITH_CURSOR:
                        onActionPut(event);
                        break;

                    case DROP_ALL_CURSOR:
                    case DROP_ONE_CURSOR:
                        break;

                    default:
                        event.setCancelled(true);
                }
            }
            else
            {
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
            // Clicked in the action bar
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
        ExplorerGuiEvent eventSlot = new ExplorerGuiEvent(this, event);
        if(event.getClick().equals(ClickType.RIGHT))
        {
            onRightClick(eventSlot);
            event.setCancelled(true);
            return;
        }

        ItemStack pickedUpItem = getPickedUpItem(eventSlot);
        if(pickedUpItem == null || mode.equals(Mode.READONLY))
        {
            event.setCancelled(true);
            return;
        }
        
        event.setCurrentItem(pickedUpItem);
        GuiUtils.setItemLater(this, event.getSlot(), getViewItem(eventSlot));
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
            
            if(viewHeight >= MAX_INVENTORY_COLUMN_SIZE)
                if(hasActions() || dataLength > MAX_INVENTORY_SIZE || keepHorizontalScrollingSpace)
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
            
            if(viewWidth >= INVENTORY_ROW_SIZE)
                if((pageCountY > 1 && viewWidth == INVENTORY_ROW_SIZE) || keepVerticalScrollingSpace)
                    viewWidth--;
            
            if(viewHeight >= MAX_INVENTORY_COLUMN_SIZE)
                if((pageCountX > 1 && viewHeight == MAX_INVENTORY_COLUMN_SIZE) || keepHorizontalScrollingSpace)
                    viewHeight--;
            
            pageCountX = (int)Math.ceil((double)dataWidth / (double)viewWidth);
            pageCountY = (int)Math.ceil((double)dataHeight / (double)viewHeight);
        }
        
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
        
        if(!hasData())
        {
            action("__empty__", 22);
        }
    }
    
    private T getData(int i)
    {
        if(i < 0 || i >= data.length)
            return null;

        return data[i];
    }
    
    private ItemStack getViewItem(ExplorerGuiEvent event)
    {
        if(!event.isValid) return null;
        if(isData2D)
        {
            return getViewItem(event.xData, event.yData);
        }
        else
        {
            return getViewItem(getData(event.posData));
        }
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
        return getViewItem(getData(y * dataWidth + x));
    }

    /**
     * Returns the ItemStack representation of the given piece of data.
     *
     * @param data The piece of data.
     * @return The piece's representation.
     */
    protected ItemStack getViewItem(T data) { return null; }

    /**
     * Returns the item displayed in the center of the GUI if it is empty.
     *
     * @return The item.
     */
    protected ItemStack getEmptyViewItem() 
    { 
        return GuiUtils.makeItem(Material.BARRIER, "Empty", "There's nothing to see here"); 
    }
    
    private ItemStack getPickedUpItem(ExplorerGuiEvent event)
    {
        if(!event.isValid) return null;
        if(isData2D)
        {
            return getPickedUpItem(event.xData, event.yData);
        }
        else
        {
            return getPickedUpItem(event.posData);
        }
    }
    
    protected ItemStack getPickedUpItem(int x, int y)
    {
        return getViewItem(x,y);
    }

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
     * @return The stack to pick-up (or {@code null} to cancel the pick-up).
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
                    ChatColor.GRAY + "Go to page " + ChatColor.WHITE + (newPage + 1) + ChatColor.GRAY + " of " + ChatColor.WHITE + lastPage
            ));
        }

        icon.setItemMeta(meta);
        return icon;
    }
    
    private void onRightClick(ExplorerGuiEvent event)
    {
        if(!event.isValid) return;
        if(isData2D)
        {
            onRightClick(event.xData, event.yData);
        }
        else
        {
            onRightClick(getData(event.posData));
        }
    }
    
    /**
     * Triggered when the player right-clicks an item on the GUI.
     * @param x The X position of the right-clicked data.
     * @param y The Y position of the right-clicked data.
     */
    protected void onRightClick(int x, int y) {}
    
    /**
     * Triggered when the player right-clicks an item on the GUI.
     *
     * @param data The right-clicked piece of data.
     */
    protected void onRightClick(T data) {}

    /**
     * Triggered when the player tries to place an item inside the GUI.
     *
     * This will not place the item in the GUI, it's up to you to update the data and refresh
     * the GUI if you need so.
     *
     * @param item The {@link ItemStack} the player is trying to put.
     * @return {@code false} to cancel the placement; {@code true} to accept it.
     */
    protected boolean onPutItem(ItemStack item) { return true; }

    static private final class ExplorerGuiEvent
    {
        //2D Explorer
        public final int xData;
        public final int yData;
        
        //1D Explorer
        public final int posData;
        
        public final boolean isValid;
        
        public ExplorerGuiEvent(ExplorerGui gui, InventoryClickEvent event)
        {
            if(gui.isData2D)
            {
                xData = gui.currentPageX * gui.viewWidth + event.getSlot() % INVENTORY_ROW_SIZE;
                yData = gui.currentPageY * gui.viewHeight + event.getSlot() / INVENTORY_ROW_SIZE;
                posData = -1;
                isValid = (xData < gui.dataWidth && xData >= 0 && yData < gui.dataHeight && yData >= 0);
            }
            else
            {
                xData = yData = -1;
                posData = gui.currentPageX * gui.viewSize + event.getSlot();
                isValid = (posData >= 0 && posData < gui.dataHeight);
            }
        }
    }
    
    /**
     * Displays the next horizontal page, if possible.
     */
    @GuiAction
    public void next()
    {
        setCurrentPageX(currentPageX - 1);
    }

    /**
     * Displays the previous horizontal page, if possible.
     */
    @GuiAction
    public void previous()
    {
        setCurrentPageX(currentPageX - 1);
    }

    /**
     * Displays the previous vertical page, if possible.
     */
    @GuiAction
    public void up()
    {
        setCurrentPageY(currentPageY - 1);
    }

    /**
     * Displays the next vertical page, if possible.
     */
    @GuiAction
    public void down()
    {
        setCurrentPageY(currentPageY + 1);
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
    
    public int getCurrentPageX()
    {
        return currentPageX;
    }

    public void setCurrentPageX(int currentPageX)
    {
        setCurrentPage(currentPageX, currentPageY);
    }

    public int getCurrentPageY()
    {
        return currentPageY;
    }

    public void setCurrentPageY(int currentPageY)
    {
        setCurrentPage(currentPageX, currentPageY);
    }
    
    public void setCurrentPage(int currentPageX, int currentPageY)
    {
        if(currentPageX < 1 || currentPageX > pageCountX - 1) return;
        if(currentPageY < 1 || currentPageY > pageCountY - 1) return;
        this.currentPageX = currentPageX;
        this.currentPageY = currentPageY;
        refresh();
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

    /**
     * If set to {@code true}, the horizontal scrolling line will remain empty even without
     * scrolls (with one page typically), so you can place buttons or things like that in this
     * area.
     *
     * Else, with one page, the place will be used to display an additional row of data.
     *
     * @param keepHorizontalScrollingSpace {@code true} if enabled.
     */
    public void setKeepHorizontalScrollingSpace(boolean keepHorizontalScrollingSpace)
    {
        this.keepHorizontalScrollingSpace = keepHorizontalScrollingSpace;
    }

    /**
     * If set to {@code true}, the vertical scrolling line will remain empty even without
     * scrolls (with one page typically), so you can place buttons or things like that in this
     * area.
     *
     * Else, with one page, the place will be used to display an additional column of data.
     *
     * @param keepVerticalScrollingSpace {@code true} if enabled.
     */
    public void setKeepVerticalScrollingSpace(boolean keepVerticalScrollingSpace)
    {
        this.keepVerticalScrollingSpace = keepVerticalScrollingSpace;
    }
}
