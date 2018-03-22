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

package fr.moribus.imageonmap.gui;

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.MapManagerException;
import fr.zcraft.zlib.components.gui.ActionGui;
import fr.zcraft.zlib.components.gui.Gui;
import fr.zcraft.zlib.components.gui.GuiAction;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.items.ItemStackBuilder;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import java.util.Arrays;
import java.util.Random;


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
     * A source of randomness.
     *
     * Yes, this javadoc comment is REALLY useful. Trust me.
     */
    private final Random random = new Random();


    /**
     *
     * @param mapToDelete The map being deleted.
     */
    public ConfirmDeleteMapGui(ImageMap mapToDelete)
    {
        this.mapToDelete = mapToDelete;
    }

    @Override
    protected void onUpdate()
    {
        /// The title of the map deletion GUI. {0}: map name.
        setTitle(I.t(getPlayerLocale(), "{0} » {black}Confirm deletion", mapToDelete.getName()));
        setSize(6 * 9);


        /* ** Item representation of the image being deleted ** */

        action("", 13, new ItemStackBuilder(Material.EMPTY_MAP)
                 /// The title of the map deletion item
                .title(I.t(getPlayerLocale(), "{red}You're about to destroy this map..."))
                 /// The end, in the lore, of a title starting with “You're about to destroy this map...”.
                .lore(I.t(getPlayerLocale(), "{red}...{italic}forever{red}."))
                .loreLine()
                .lore(I.t(getPlayerLocale(), "{gray}Name: {white}{0}",mapToDelete.getName()))
                .lore(I.t(getPlayerLocale(), "{gray}Map ID: {white}{0}", mapToDelete.getId()))
                .lore(I.t(getPlayerLocale(), "{gray}Maps inside: {white}{0}", mapToDelete.getMapsIDs().length))
                .hideAttributes()
        );


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

    @GuiAction ("cancel")
    protected void cancel()
    {
        close();
    }

    @GuiAction ("delete")
    protected void delete()
    {
        // Does the player still have the permission to delete a map?
        if (!Permissions.DELETE.grantedTo(getPlayer()))
        {
            I.sendT(getPlayer(), "{ce}You are no longer allowed to do that.");
            close();
            return;
        }

        MapManager.clear(getPlayer().getInventory(), mapToDelete);

        try
        {
            MapManager.deleteMap(mapToDelete);
            getPlayer().sendMessage(I.t("{gray}Map successfully deleted."));
        }
        catch (MapManagerException ex)
        {
            PluginLogger.warning("Error while deleting map", ex);
            getPlayer().sendMessage(ChatColor.RED + ex.getMessage());
        }


        // We try to open the map list GUI, if the map was deleted, before the details GUI
        // (so the grandparent GUI).
        if (getParent() != null && getParent().getParent() != null)
            Gui.open(getPlayer(), getParent().getParent());
        else
            close();
    }
}
