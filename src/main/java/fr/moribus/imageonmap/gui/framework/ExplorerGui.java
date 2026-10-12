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

import fr.moribus.imageonmap.tools.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * A GUI to explore a list (1D) or a grid (2D) of data, with pagination.
 *
 * @param <T> The type of the explored data.
 */
public abstract class ExplorerGui<T> extends ActionGui {
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

    protected ExplorerGui() {
        // Defined early to be able to use getSize() in the onUpdate method.
        setSize(MAX_INVENTORY_SIZE);
    }

    /**
     * Sets the explored data, as a list.
     *
     * @param data The data.
     */
    protected void setData(T[] data) {
        this.data = data;
        final int dataLength = data == null ? 0 : data.length;
        setDataShape(0, dataLength);
    }

    /**
     * Sets the shape of the explored data. If the width is positive, the data is a grid.
     *
     * @param dataWidth  The width of the grid.
     * @param dataHeight The height of the grid.
     */
    protected void setDataShape(int dataWidth, int dataHeight) {
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        this.isData2D = dataWidth > 0;
    }

    protected boolean hasData() {
        if (isData2D) {
            return dataWidth > 0 && dataHeight > 0;
        } else {
            return data != null && data.length > 0;
        }
    }

    @Override
    protected void populate(Inventory inventory) {
        if (pageCountX > 1) {
            updateAction("next", getPageItem("next", canGoNext()));
            updateAction("previous", getPageItem("previous", canGoPrevious()));
        }

        if (pageCountY > 1) {
            updateAction("up", getPageItem("up", canGoUp()));
            updateAction("down", getPageItem("down", canGoDown()));
        }

        if (hasData()) {
            if (!isData2D) {
                final int start = currentPageX * viewSize;
                final int max = Math.min(viewSize, data.length - start);

                for (int i = 0; i < max; i++) {
                    inventory.setItem(i, getViewItem(getData(i + start)));
                }
            } else {
                final int startX = currentPageX * viewWidth;
                final int startY = currentPageY * viewHeight;

                final int maxX = Math.min(viewWidth, dataWidth - startX);
                final int maxY = Math.min(viewHeight, dataHeight - startY);

                for (int i = maxY; i-- > 0; ) {
                    for (int j = maxX; j-- > 0; ) {
                        inventory.setItem(i * INVENTORY_ROW_SIZE + j, getViewItem(j + startX, i + startY));
                    }
                }
            }
        } else {
            updateAction("__empty__", getEmptyViewItem());
        }

        if (hasActions()) {
            super.populate(inventory);
        }
    }

    @Override
    protected void onClick(InventoryClickEvent event) {
        final int slot = event.getRawSlot();

        // Clicked in the action bar
        if (hasActions() && slot >= MAX_INVENTORY_SIZE - INVENTORY_ROW_SIZE && slot < MAX_INVENTORY_SIZE) {
            super.onClick(event);
            return;
        }

        if (isData2D && pageCountY > 1 && slot % INVENTORY_ROW_SIZE == INVENTORY_ROW_SIZE - 1) {
            super.onClick(event);
            return;
        }

        // The user clicked in the GUI
        if (affectsGui(event)) {
            if (hasData()) {
                switch (event.getAction()) {
                    case PICKUP_ALL:
                    case PICKUP_HALF:
                    case PICKUP_ONE:
                    case PICKUP_SOME:
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
            } else {
                event.setCancelled(true);
            }
        } else {
            if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                onActionMove(event);
            }
        }
    }

    @Override
    protected void onDrag(InventoryDragEvent event) {
        if (!affectsGui(event)) {
            return;
        }

        for (int slot : event.getRawSlots()) {
            // Clicked in the action bar
            if (hasActions() && slot >= MAX_INVENTORY_SIZE - INVENTORY_ROW_SIZE && slot < MAX_INVENTORY_SIZE) {
                super.onDrag(event);
                return;
            }

            if (isData2D && pageCountY > 1 && slot % INVENTORY_ROW_SIZE == INVENTORY_ROW_SIZE - 1) {
                super.onDrag(event);
                return;
            }
        }

        event.setCancelled(true);

        if (!onPutItem(event.getOldCursor())) {
            return;
        }
        event.setCursor(new ItemStack(Material.AIR));
    }

    private void onActionPickup(InventoryClickEvent event) {
        final ExplorerGuiEvent eventSlot = new ExplorerGuiEvent(this, event);

        if (event.getClick().equals(ClickType.RIGHT)) {
            onRightClick(eventSlot);
            event.setCancelled(true);
            return;
        }

        final ItemStack pickedUpItem = getPickedUpItem(eventSlot);
        if (pickedUpItem == null) {
            event.setCancelled(true);
            return;
        }

        event.setCurrentItem(pickedUpItem);
        GuiUtils.setItemLater(this, event.getSlot(), getViewItem(eventSlot));
    }

    private void onActionPut(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!onPutItem(event.getCursor())) {
            return;
        }
        event.getView().setCursor(new ItemStack(Material.AIR));
    }

    private void onActionMove(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!onPutItem(event.getCurrentItem())) {
            return;
        }
        event.setCurrentItem(new ItemStack(Material.AIR));
    }

    @Override
    protected void onAfterUpdate() {
        // Calculating page count
        if (data != null && data.length <= 0) {
            viewWidth = INVENTORY_ROW_SIZE;
            viewHeight = 1;
            viewSize = viewWidth;
            pageCountX = 1;
            pageCountY = 1;
        } else if (!isData2D) {
            final int dataLength = (data == null) ? 0 : data.length;
            viewWidth = INVENTORY_ROW_SIZE;
            viewHeight = Math.min((int) Math.ceil((double) dataLength / (double) viewWidth),
                    MAX_INVENTORY_COLUMN_SIZE);

            if (viewHeight >= MAX_INVENTORY_COLUMN_SIZE
                    && (hasActions() || dataLength > MAX_INVENTORY_SIZE || keepHorizontalScrollingSpace)) {
                viewHeight--;
            }

            viewSize = viewWidth * viewHeight;
            pageCountX = (int) Math.ceil((double) dataLength / (double) viewSize);
            pageCountY = 1;
        } else {
            viewWidth = Math.min(dataWidth, INVENTORY_ROW_SIZE);
            viewHeight = Math.min(dataHeight, MAX_INVENTORY_COLUMN_SIZE);

            pageCountX = (int) Math.ceil((double) dataWidth / (double) viewWidth);
            pageCountY = (int) Math.ceil((double) dataHeight / (double) viewHeight);

            if (viewWidth >= INVENTORY_ROW_SIZE
                    && ((pageCountY > 1 && viewWidth == INVENTORY_ROW_SIZE) || keepVerticalScrollingSpace)) {
                viewWidth--;
            }

            if (viewHeight >= MAX_INVENTORY_COLUMN_SIZE
                    && ((pageCountX > 1 && viewHeight == MAX_INVENTORY_COLUMN_SIZE) || keepHorizontalScrollingSpace)) {
                viewHeight--;
            }

            pageCountX = (int) Math.ceil((double) dataWidth / (double) viewWidth);
            pageCountY = (int) Math.ceil((double) dataHeight / (double) viewHeight);
        }

        if (pageCountX > 1) {
            action("previous", MAX_INVENTORY_SIZE - INVENTORY_ROW_SIZE);
            action("next", MAX_INVENTORY_SIZE - 1 - (pageCountY > 1 ? 1 : 0));
        }

        if (pageCountY > 1) {
            action("up", INVENTORY_ROW_SIZE - 1);
            action("down", MAX_INVENTORY_SIZE - 1);
        }

        if (!hasData()) {
            action("__empty__", 22);
        }
    }

    private T getData(int i) {
        if (i < 0 || i >= data.length) {
            return null;
        }
        return data[i];
    }

    private ItemStack getViewItem(ExplorerGuiEvent event) {
        if (!event.isValid) {
            return null;
        }

        if (isData2D) {
            return getViewItem(event.dataX, event.dataY);
        } else {
            return getViewItem(getData(event.posData));
        }
    }

    /**
     * Returns the item displayed for the given grid position (2D data).
     *
     * @param x The column.
     * @param y The row.
     * @return The item.
     */
    protected ItemStack getViewItem(int x, int y) {
        return getViewItem(getData(y * dataWidth + x));
    }

    /**
     * Returns the item displayed for the given data (1D data).
     *
     * @param data The data.
     * @return The item.
     */
    protected ItemStack getViewItem(T data) {
        return null;
    }

    protected ItemStack getEmptyViewItem() {
        return GuiUtils.makeItem(Material.BARRIER, ChatColor.RED + "Empty",
                ChatColor.GRAY + "There's nothing to see here");
    }

    private ItemStack getPickedUpItem(ExplorerGuiEvent event) {
        if (!event.isValid) {
            return null;
        }

        if (isData2D) {
            return getPickedUpItem(event.dataX, event.dataY);
        } else {
            return getPickedUpItemAt(event.posData);
        }
    }

    /**
     * Returns the item given to the player when they pick up the given grid position (2D data).
     *
     * @param x The column.
     * @param y The row.
     * @return The item, or {@code null} to cancel the pickup.
     */
    protected ItemStack getPickedUpItem(int x, int y) {
        return getViewItem(x, y);
    }

    /**
     * Returns the item given to the player when they pick up the given data (1D data).
     *
     * @param data The data.
     * @return The item, or {@code null} to cancel the pickup.
     */
    protected ItemStack getPickedUpItem(T data) {
        return getViewItem(data);
    }

    private ItemStack getPickedUpItemAt(int dataIndex) {
        if (dataIndex < 0 || dataIndex >= data.length) {
            return null;
        }
        return getPickedUpItem(getData(dataIndex));
    }

    protected ItemStack getPageItem(String paginationButtonType, boolean canUse) {
        final String title;
        final int newPage;
        final int lastPage;

        switch (paginationButtonType) {
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
                newPage = currentPageY - 1;
                lastPage = getVerticalPageCount();
                break;
            case "down":
                title = canUse ? "Go down" : "Bottom page";
                newPage = currentPageY + 1;
                lastPage = getVerticalPageCount();
                break;
            default:
                return null; // invalid page type
        }

        final Material material = canUse ? Material.ARROW : Material.STICK;
        final String displayName = (canUse ? ChatColor.WHITE : ChatColor.GRAY) + title;

        if (canUse) {
            return GuiUtils.makeItem(material, displayName, ChatColor.GRAY + "Go to page " + ChatColor.WHITE
                    + (newPage + 1) + ChatColor.GRAY + " of " + ChatColor.WHITE + lastPage);
        }
        return GuiUtils.makeItem(material, displayName);
    }

    private void onRightClick(ExplorerGuiEvent event) {
        if (!event.isValid) {
            return;
        }

        if (isData2D) {
            onRightClick(event.dataX, event.dataY);
        } else {
            onRightClick(getData(event.posData));
        }
    }

    protected void onRightClick(int x, int y) {
    }

    protected void onRightClick(T data) {
    }

    protected boolean onPutItem(ItemStack item) {
        return true;
    }

    @GuiAction
    public void next() {
        setCurrentPageX(currentPageX + 1);
    }

    @GuiAction
    public void previous() {
        setCurrentPageX(currentPageX - 1);
    }

    @GuiAction
    public void up() {
        setCurrentPageY(currentPageY - 1);
    }

    @GuiAction
    public void down() {
        setCurrentPageY(currentPageY + 1);
    }

    public boolean canGoNext() {
        return currentPageX < pageCountX - 1;
    }

    public boolean canGoPrevious() {
        return currentPageX > 0;
    }

    public boolean canGoUp() {
        return currentPageY > 0;
    }

    public boolean canGoDown() {
        return currentPageY < pageCountY - 1;
    }

    public void setCurrentPageX(int currentPageX) {
        setCurrentPage(currentPageX, currentPageY);
    }

    public void setCurrentPageY(int currentPageY) {
        setCurrentPage(currentPageX, currentPageY);
    }

    /**
     * Changes the displayed page.
     *
     * @param currentPageX The horizontal page.
     * @param currentPageY The vertical page.
     */
    public void setCurrentPage(int currentPageX, int currentPageY) {
        if (currentPageX < 0 || currentPageX > pageCountX - 1) {
            return;
        }

        if (isData2D) {
            if (currentPageY < 0 || currentPageY > pageCountY - 1) {
                return;
            }
            this.currentPageY = currentPageY;
        }

        this.currentPageX = currentPageX;
        refresh();
    }

    public int getPageCount() {
        return pageCountX;
    }

    public int getVerticalPageCount() {
        return pageCountY;
    }

    public void setKeepHorizontalScrollingSpace(boolean keepHorizontalScrollingSpace) {
        this.keepHorizontalScrollingSpace = keepHorizontalScrollingSpace;
    }

    public void setKeepVerticalScrollingSpace(boolean keepVerticalScrollingSpace) {
        this.keepVerticalScrollingSpace = keepVerticalScrollingSpace;
    }

    private static final class ExplorerGuiEvent {
        private final int dataX;
        private final int dataY;
        private final int posData;
        private final boolean isValid;

        private ExplorerGuiEvent(ExplorerGui<?> gui, InventoryClickEvent event) {
            if (gui.isData2D) {
                dataX = gui.currentPageX * gui.viewWidth + event.getSlot() % INVENTORY_ROW_SIZE;
                dataY = gui.currentPageY * gui.viewHeight + event.getSlot() / INVENTORY_ROW_SIZE;
                posData = -1;
                isValid = dataX < gui.dataWidth && dataX >= 0 && dataY < gui.dataHeight && dataY >= 0;
            } else {
                dataX = -1;
                dataY = -1;
                posData = gui.currentPageX * gui.viewSize + event.getSlot();
                isValid = posData >= 0 && posData < gui.dataHeight;
            }
        }
    }
}
