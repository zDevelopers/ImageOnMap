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

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Conversion of legacy formatted strings to Adventure components.
 */
public final class Text {
    /**
     * The style applied by a reset code, or before a color code: every decoration explicitly disabled. This mirrors
     * how the server always converted legacy item names and lore (a color code resets formatting, so a colored item
     * name or lore line is not italic).
     */
    private static final Style RESET = Style.style()
            .decoration(TextDecoration.BOLD, false)
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.UNDERLINED, false)
            .decoration(TextDecoration.STRIKETHROUGH, false)
            .decoration(TextDecoration.OBFUSCATED, false)
            .build();

    private Text() {
    }

    /**
     * Converts a legacy string (using section-sign formatting codes) to a component.
     *
     * @param text The legacy text.
     * @return The component, or {@code null} if the text is {@code null}.
     */
    public static Component legacy(String text) {
        if (text == null) {
            return null;
        }

        final TextComponent.Builder root = Component.text();
        final StringBuilder buffer = new StringBuilder();
        Style style = Style.empty();

        for (int i = 0; i < text.length(); i++) {
            final char character = text.charAt(i);
            if (character == ChatColor.COLOR_CHAR && i + 1 < text.length()) {
                final ChatColor code = ChatColor.getByChar(text.charAt(i + 1));
                if (code != null) {
                    flush(root, buffer, style);
                    style = apply(style, code);
                    i++;
                    continue;
                }
            }
            buffer.append(character);
        }
        flush(root, buffer, style);

        return root.build();
    }

    /**
     * Converts legacy strings to components.
     *
     * @param lines The legacy texts.
     * @return The components.
     */
    public static List<Component> legacy(List<String> lines) {
        final List<Component> components = new ArrayList<>(lines.size());
        for (String line : lines) {
            components.add(legacy(line));
        }
        return components;
    }

    private static void flush(TextComponent.Builder root, StringBuilder buffer, Style style) {
        if (buffer.length() == 0) {
            return;
        }
        root.append(Component.text(buffer.toString(), style));
        buffer.setLength(0);
    }

    private static Style apply(Style style, ChatColor code) {
        switch (code) {
            case RESET:
                return RESET;
            case BOLD:
                return style.decoration(TextDecoration.BOLD, true);
            case ITALIC:
                return style.decoration(TextDecoration.ITALIC, true);
            case UNDERLINE:
                return style.decoration(TextDecoration.UNDERLINED, true);
            case STRIKETHROUGH:
                return style.decoration(TextDecoration.STRIKETHROUGH, true);
            case MAGIC:
                return style.decoration(TextDecoration.OBFUSCATED, true);
            default:
                return RESET.color(NamedTextColor.NAMES.value(code.name().toLowerCase()));
        }
    }
}
