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

package fr.moribus.imageonmap.tools;

import fr.moribus.imageonmap.gui.framework.GuiUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A builder to create or update item stacks, using legacy-formatted texts for names and lore.
 */
public class ItemStackBuilder {
    private final ItemStack itemStack;
    private final Material material;
    private final List<Component> existingLore = new ArrayList<>();
    private final List<String> loreLines = new ArrayList<>();
    private StringBuilder title = null;
    private boolean hideAllAttributes = false;

    public ItemStackBuilder(Material material) {
        this.material = material;
        this.itemStack = null;
    }

    /**
     * Creates a builder updating an existing item stack. The existing lore is kept.
     *
     * @param itemStack The item stack to update.
     */
    public ItemStackBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.material = null;

        if (itemStack != null) {
            final ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && meta.hasLore()) {
                final List<Component> lore = meta.lore();
                if (lore != null) {
                    existingLore.addAll(lore);
                }
            }
        }
    }

    /**
     * Builds the item stack.
     *
     * @return The item stack.
     */
    public ItemStack item() {
        final ItemStack newItemStack;

        if (itemStack == null) {
            if (material == null) {
                throw new IllegalStateException("Cannot build item without a specified material");
            }
            newItemStack = new ItemStack(material);
        } else {
            newItemStack = itemStack;
        }

        final ItemMeta meta = Objects.requireNonNull(newItemStack.getItemMeta());

        if (title != null) {
            meta.displayName(Text.legacy(ChatColor.RESET + title.toString()));
        }

        if (!loreLines.isEmpty()) {
            final List<Component> lore = new ArrayList<>(existingLore);
            lore.addAll(Text.legacy(loreLines));
            meta.lore(lore);
        }

        if (hideAllAttributes) {
            meta.addItemFlags(ItemFlag.values());
        }

        newItemStack.setItemMeta(meta);
        return newItemStack;
    }

    /**
     * Appends text to the title of the item.
     *
     * @param color The color of the appended text (can be {@code null}).
     * @param texts The texts to append.
     * @return The current builder, for method chaining.
     */
    public ItemStackBuilder title(ChatColor color, String... texts) {
        if (this.title == null) {
            this.title = new StringBuilder();
        }
        if (color != null) {
            this.title.append(color);
        }
        this.title.append(String.join("", texts));
        return this;
    }

    public ItemStackBuilder title(String... texts) {
        return title(null, texts);
    }

    public ItemStackBuilder lore(String... lines) {
        loreLines.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemStackBuilder lore(List<String> lines) {
        loreLines.addAll(lines);
        return this;
    }

    public ItemStackBuilder loreLine(String... text) {
        return loreLine(null, text);
    }

    /**
     * Adds a lore line.
     *
     * @param color The color of the line (can be {@code null}).
     * @param text  The texts composing the line.
     * @return The current builder, for method chaining.
     */
    public ItemStackBuilder loreLine(ChatColor color, String... text) {
        if (color != null) {
            loreLines.add(color + String.join("", text));
        } else {
            loreLines.add(String.join("", text));
        }
        return this;
    }

    public ItemStackBuilder longLore(String text) {
        return lore(GuiUtils.generateLore(text));
    }

    public ItemStackBuilder longLore(String text, int lineLength) {
        return lore(GuiUtils.generateLore(text, lineLength));
    }

    public ItemStackBuilder loreSeparator() {
        loreLines.add("");
        return this;
    }

    public ItemStackBuilder hideAllAttributes() {
        this.hideAllAttributes = true;
        return this;
    }
}
