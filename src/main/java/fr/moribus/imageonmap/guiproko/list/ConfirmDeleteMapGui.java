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

package fr.moribus.imageonmap.guiproko.list;

import fr.moribus.imageonmap.*;
import fr.moribus.imageonmap.guiproko.core.*;
import fr.moribus.imageonmap.map.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.material.*;

import java.util.*;


public class ConfirmDeleteMapGui extends ActionGui
{
    static private final int BUTTONS_WIDTH  = 4;

    static private final int FIRST_SLOT_DELETE_BUTTON = 27;
    static private final int SHIFT_CANCEL_BUTTON = 5;

    /**
     * The messages randomly displayed in the lore of the “delete” buttons.
     */
    static private final String[] DELETE_MESSAGES = new String[]{
        "Please", "I'm still alive", "Don't do that", "I'm still loving you", "I want to live",
        "Please please", "Please please please", "What are you doing?!", "Nooooo!",
        "Click and I'll be dead", "Why?", "Please don't do that", "Think about my family",
        "Click, I don't like you anymore.", "I don't hate you.", "Click, I'm ready.",
        "I'm a green button.", "I'm different.", "Thanks anyway.", "Excuse me.", "Get mad.",
        "Sorry!", "My fault!", "I don't blame you.", "No hard feelings.",
        "But I need to protect the humans!", "Noooo!", "I'm scared!", "What are you doing?",
        "It burns.", "This is not good.", "Can't breathe.", "Thanks anyway.", "These things happen.",
        "That was nobody's fault.", "I probably deserved it.", "I blame myself."
    };

    /**
     * The messages randomly displayed in the lore of the “cancel” buttons.
     */
    static private final String[] CANCEL_MESSAGES = new String[] {
        "Yay!", "Still aliiiive!", "Click click click", "Yes do that", "I'm a red button.",
        "Please click here", "The other button is ugly", "Save me", "This is the good choice",
        "Click, I want to live!", "I'll be dead another day anyway", "Are you sure?",
        "So you're still loving me?", "Please save me", "Take me with you.",
        "Excuse me.", "Don't make lemonade.", "Sleep mode activated.", "Hybernating.",
        "Your business is appreciated.", "Hey! It's me! Don't shoot!", "Wheee!", "Hurray!",
        "You have excellent aim!", "Please please please"
    };
    
    /**
     * The map being deleted.
     */
    private final ImageMap mapToDelete;

    /**
     * The previously-viewed page of the list GUI.
     * Used to be able to bring the user back to the same page.
     */
    private final int currentPage;

    /**
     * A source of randomness.
     *
     * Yes, this javadoc comment is REALLY useful. Trust me.
     */
    private final Random random = new Random();


    /**
     *
     * @param mapToDelete The map being deleted.
     * @param currentPage The previously-viewed page of the list GUI.
     */
    public ConfirmDeleteMapGui(ImageMap mapToDelete, int currentPage)
    {
        this.mapToDelete = mapToDelete;
        this.currentPage = currentPage;
    }

    @Override
    protected void onUpdate()
    {
        setTitle(mapToDelete.getName() + " » " + ChatColor.BLACK + "Confirm deletion");
        setSize(6 * 9);
        
        /* ** Item representation of the image being deleted ** */

        ItemStack beingDeleted = new ItemStack(Material.EMPTY_MAP);
        ItemMeta meta = beingDeleted.getItemMeta();

        meta.setDisplayName(ChatColor.RED + "You're about to destroy this map...");
        meta.setLore(Arrays.asList(
                ChatColor.RED + "..." + ChatColor.ITALIC + "forever" + ChatColor.RED + ".",
                "",
                ChatColor.GRAY + "Name: " + ChatColor.WHITE + mapToDelete.getName(),
                ChatColor.GRAY + "Map ID: " + ChatColor.WHITE + mapToDelete.getId(),
                ChatColor.GRAY + "Maps inside: " + ChatColor.WHITE + mapToDelete.getMapsIDs().length
        ));

        beingDeleted.setItemMeta(meta);

        action("", 13, beingDeleted);


        /* ** Buttons ** */

        int slot = FIRST_SLOT_DELETE_BUTTON;
        for(; slot < getSize() - (9 - BUTTONS_WIDTH); slot++)
        {
            action("delete", slot, createDeleteSubButton());
            action("cancel", slot + SHIFT_CANCEL_BUTTON, createCancelSubButton());

            if((slot + 1) % 9 == (9 - BUTTONS_WIDTH - 1))
                slot += 5;
        }
    }

    private ItemStack createDeleteSubButton()
    {
        // Orange? Nooo. In the real world this is red. True story.
        return createSubButton(DyeColor.ORANGE, ChatColor.RED + "Delete the map", DELETE_MESSAGES);
    }

    private ItemStack createCancelSubButton()
    {
        // YES. Purple = lime. BECAUSE. Just accept it.
        return createSubButton(DyeColor.PURPLE, ChatColor.GREEN + "Cancel", CANCEL_MESSAGES);
    }

    private ItemStack createSubButton(DyeColor color, String title, String[] messages)
    {
        Dye pane = new Dye(Material.STAINED_GLASS_PANE);
        pane.setColor(color);

        ItemStack subButton = pane.toItemStack(1);
        ItemMeta meta = subButton.getItemMeta();

        meta.setDisplayName(title);
        meta.setLore(Arrays.asList(
                "",
                ChatColor.GRAY + messages[random.nextInt(messages.length)]
        ));

        subButton.setItemMeta(meta);
        return subButton;
    }
    
    @GuiAction
    protected void action_cancel()
    {
        Gui.open(getPlayer(), new MapDetailGui(mapToDelete)).setCurrentPageX(currentPage);
    }
    
    @GuiAction
    protected void action_delete()
    {
        MapManager.clear(getPlayer().getInventory(), mapToDelete);

        try
        {
            MapManager.deleteMap(mapToDelete);
            getPlayer().sendMessage(ChatColor.GRAY + "Map successfully deleted.");
        }
        catch (MapManagerException ex)
        {
            PluginLogger.warning("Error while deleting map", ex);
            getPlayer().sendMessage(ChatColor.RED + ex.getMessage());
        }

        Gui.open(getPlayer(), new MapListGui(/* currentPage */));
    }
}
