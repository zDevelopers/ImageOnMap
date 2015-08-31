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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


abstract public class ExplorerGui<T> extends ActionGui
{
    static protected enum Mode {READONLY, CREATIVE};
    
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
    
    protected void setData(T[] data, int dataWidth)
    {
        this.data = data;
        if(dataWidth > 0)
            setData(dataWidth, (int) Math.ceil((double)data.length / (double)dataWidth));
    }
    
    protected void setData(int dataWidth, int dataHeight)
    {
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        this.isData2D = dataWidth > 0;
    }
    
    protected void setData(T[] data)
    {
        setData(data, 0);
    }
    
    @Override
    protected void populate(Inventory inventory)
    {
        if(pageCountX > 1)
        {
            if(canGoNext())
                updateAction("next", Material.ARROW, "Next page");
            else
                updateAction("next", Material.STICK, "No next page");
            
            if(canGoPrevious())
                updateAction("previous", Material.ARROW, "Previous page");
            else
                updateAction("previous", Material.STICK, "No previous page");
        }
        
        if(pageCountY > 1)
        {
            if(canGoUp())
                updateAction("up", Material.ARROW, "Go Up");
            else
                updateAction("up", Material.STICK, "Top page");
            
            if(canGoDown())
                updateAction("down", Material.ARROW, "Go Down");
            else
                updateAction("down", Material.STICK, "Bottom page");
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
                    inventory.setItem(i*INVENTORY_ROW_SIZE + j, getViewItem(j + startX, i + startY));
                }
            }
        }
        if(hasActions()) super.populate(inventory);
    }
    
    @Override
    protected void onClick(InventoryClickEvent event)
    {
        int slot = event.getRawSlot();
        
        //Clicked in the action bar
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
        
        if(affectsGui(event))//The user clicked in its own inventory
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
            viewWidth = INVENTORY_ROW_SIZE;
            viewHeight = Math.min((int)Math.ceil((double)data.length / (double)viewWidth),
                                   MAX_INVENTORY_COLUMN_SIZE);
            
            if(hasActions() || data.length > MAX_INVENTORY_SIZE)
                viewHeight--;
            viewSize = viewWidth * viewHeight;
            
            pageCountX = (int)Math.ceil((double)data.length / (double)viewSize);
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
    
    protected ItemStack getViewItem(int i)
    {
        return getViewItem(getData(i));
    }
    
    protected ItemStack getViewItem(int x, int y)
    {
        return getViewItem(y * dataWidth + x);
    }
    
    protected ItemStack getViewItem(T data){return null;};
    
    private ItemStack getPickedUpItem(int dataIndex)
    {
        if(dataIndex < 0 || dataIndex >= data.length)
            return null;
        return getPickedUpItem(getData(dataIndex));
    }
    
    protected ItemStack getPickedUpItem(T data){return getViewItem(data);}
    protected void onRightClick(T data){}
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
    
    public void up()
    {
        if(!canGoUp()) return;
        currentPageY--;
        refresh();
    }
    
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
    
    public int getPageCount()
    {
        return pageCountX;
    }
    
    public int getVerticalPageCount()
    {
        return pageCountY;
    }
    
    protected Mode getMode() {return mode;}
    protected void setMode(Mode mode) {this.mode = mode;}
}
