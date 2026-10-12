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
import fr.moribus.imageonmap.tools.RunTask;
import fr.moribus.imageonmap.tools.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Various utilities for GUIs and texts.
 */
public final class GuiUtils {
    private GuiUtils() {
    }

    /**
     * Sets an item in the inventory of a GUI on the next tick.
     *
     * @param gui  The GUI.
     * @param slot The slot.
     * @param item The item.
     */
    public static void setItemLater(InventoryGui gui, int slot, ItemStack item) {
        RunTask.nextTick(() -> {
            final Inventory inventory = gui.getInventory();
            inventory.setItem(slot, item);
            for (HumanEntity player : inventory.getViewers()) {
                if (player instanceof Player) {
                    ((Player) player).updateInventory();
                }
            }
        });
    }

    public static ItemStack makeItem(Material material, String title, String... loreLines) {
        return makeItem(new ItemStack(material), title, Arrays.asList(loreLines));
    }

    /**
     * Sets the title and lore of an item.
     *
     * @param itemStack The item.
     * @param title     The title (legacy formatting codes allowed). If {@code null}, the title is not changed.
     * @param loreLines The lore (legacy formatting codes allowed). If {@code null} or empty, the lore is not changed.
     * @return The item.
     */
    public static ItemStack makeItem(ItemStack itemStack, String title, List<String> loreLines) {
        final ItemMeta meta = Objects.requireNonNull(itemStack.getItemMeta());

        if (title != null) {
            meta.displayName(Text.legacy(title));
        }
        if (loreLines != null && !loreLines.isEmpty()) {
            meta.lore(Text.legacy(loreLines));
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static List<String> generateLore(String text) {
        return generateLore(text, 28);
    }

    /**
     * Splits a text into lines of the given length, keeping the formatting.
     *
     * @param text       The text.
     * @param lineLength The maximal length of a line (formatting codes excluded).
     * @return The lines.
     */
    public static List<String> generateLore(String text, int lineLength) {
        final List<String> lore = new ArrayList<>();
        final String[] segments = text.split("\n");
        String previousSegmentFormatting = "";

        // We divide by segments of blocks without line break to keep these breaks in the final result
        for (String segment : segments) {
            segment = previousSegmentFormatting + segment;

            if (ChatColor.stripColor(segment).length() <= lineLength) {
                lore.add(segment);
                continue;
            }

            final String[] words = segment.split(" ");
            String currentLine = "";

            for (final String word : words) {
                if (ChatColor.stripColor(currentLine + word).length() > lineLength
                        && ChatColor.stripColor(word).length() <= lineLength) {
                    lore.add(currentLine.trim());
                    // It's important to preserve the formatting
                    currentLine = ChatColor.getLastColors(currentLine) + word + " ";
                } else {
                    currentLine += word + " ";
                }
            }

            // Don't forget the last line...
            if (!ChatColor.stripColor(currentLine.trim()).isEmpty()) {
                lore.add(currentLine.trim());
            }

            previousSegmentFormatting = ChatColor.getLastColors(lore.get(lore.size() - 1));
        }

        return lore;
    }

    public static String generatePrefixedFixedLengthString(String prefix, String text) {
        return generatePrefixedFixedLengthString(prefix, text, 55);
    }

    /**
     * Splits a text into lines of the given length, and prefixes each of them.
     *
     * @param prefix     The prefix.
     * @param text       The text.
     * @param lineLength The maximal length of a line (prefix and formatting codes excluded).
     * @return The prefixed lines, joined with line breaks.
     */
    public static String generatePrefixedFixedLengthString(String prefix, String text, int lineLength) {
        final List<String> lines = generateLore(text, lineLength);
        final StringBuilder result = new StringBuilder();

        for (final String line : lines) {
            result.append(prefix).append(line).append('\n');
        }

        return result.toString().trim();
    }
}
