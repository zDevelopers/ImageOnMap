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

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


abstract public class ExplorerGui<T> extends ActionGui
{
    static protected enum Mode {READONLY, CREATIVE};
    
    private T[] data;
    private int viewWidth;
    
    private int currentPageX = 0;
    
    private int pageCountX = 0;
    private int pageCountY = 0;
    private int inventoryViewSize;
    
    private Mode mode = Mode.CREATIVE;
    
    protected void setData(T[] data, int viewWidth)
    {
        this.data = data;
        this.viewWidth = viewWidth;
    }
    
    protected void setData(T[] data)
    {
        setData(data, 0);
    }
    
    @Override
    protected void populate(Inventory inventory)
    {
        int inventorySize = MAX_INVENTORY_SIZE;
        if(getPageCount() > 1)
        {
            inventorySize -= INVENTORY_ROW_SIZE;
            if(canGoNext())
                updateAction("next", Material.ARROW, "Next page");
            else
                updateAction("next", Material.STICK, "No next page");
            
            if(canGoPrevious())
                updateAction("previous", Material.ARROW, "Previous page");
            else
                updateAction("previous", Material.STICK, "No previous page");
        }
        
        if(viewWidth <= 0)
        {
            int start = currentPageX * inventorySize;
            int max = Math.min(inventorySize, data.length - start);
            for(int i = 0; i < max; i++)
            {
                inventory.setItem(i, getViewItem(data[i + start]));
            }
        }
        if(hasActions()) super.populate(inventory);
    }
    
    @Override
    public void update()
    {
        //TODO: Make inventory fit to content
        setSize(MAX_INVENTORY_SIZE);
        
        super.update();
    }
    
    @Override
    protected void onClick(InventoryClickEvent event)
    {
        int slot = event.getRawSlot();
        
        //Clicked in the action bar
        if(slot > MAX_INVENTORY_SIZE - INVENTORY_ROW_SIZE
                && slot < MAX_INVENTORY_SIZE)
        {
            super.onClick(event);
            return;
        }
        
        if(slot < event.getInventory().getSize())//The user clicked in its own inventory
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
    
    private int getDataIndex(int slot)
    {
        int inventorySize = MAX_INVENTORY_SIZE;
        if(getPageCount() > 1) inventorySize -= INVENTORY_ROW_SIZE;
        return currentPageX * inventorySize + slot;
    }
    
    private void onActionPickup(InventoryClickEvent event)
    {
        if(mode.equals(Mode.READONLY))
        {
            event.setCancelled(true);
            return;
        }
        int dataIndex = getDataIndex(event.getSlot());
        if(dataIndex < 0 || dataIndex >= data.length)
        {
            event.setCancelled(true);
            return;
        }
        event.setCurrentItem(getPickedUpItem(data[dataIndex]));
        GuiUtils.setItemLater(this, event.getSlot(), getViewItem(data[dataIndex]));
    }
    
    private void onActionPut(InventoryClickEvent event)
    {
        event.setCancelled(true);
        if(mode.equals(Mode.READONLY)) return;
        if(!onPutItem(event.getCursor())) return;
        event.setCursor(new ItemStack(Material.AIR));
    }
    
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
        if(getPageCount() > 1)
        {
            action("previous", MAX_INVENTORY_SIZE - INVENTORY_ROW_SIZE);
            action("next", MAX_INVENTORY_SIZE - 1);
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
    
    abstract protected ItemStack getViewItem(T data);
    
    protected ItemStack getPickedUpItem(T data){return getViewItem(data);}
    protected boolean onPutItem(ItemStack item){return true;}
    
    public void next()
    {
        if(!canGoNext()) return;
        currentPageX++;
        refresh();
    }
    
    public void previous()
    {
        if(!canGoPrevious()) return;
        currentPageX--;
        refresh();
    }
    
    public boolean canGoNext()
    {
        return currentPageX < getPageCount();
    }
    
    public boolean canGoPrevious()
    {
        return currentPageX > 0;
    }
    
    public int getPageCount()
    {
        if(data.length == 0) return 0;
        
        if(viewWidth > 0)
        {
            if(viewWidth > INVENTORY_ROW_SIZE) return 1;
            return (int)Math.ceil(viewWidth / (INVENTORY_ROW_SIZE - 1));
        }
        
        return (int)Math.ceil(data.length / (MAX_INVENTORY_SIZE - (hasActions() ? INVENTORY_ROW_SIZE : 0)));
    }
    
    public int getVerticalPageCount()
    {
        if(viewWidth <= 0) return 0;
        
        return (int)Math.ceil(data.length / (MAX_INVENTORY_COLUMN_SIZE - (hasActions() ? 0 : 1)));
    }
    
    protected Mode getMode() {return mode;}
    protected void setMode(Mode mode) {this.mode = mode;}
}
