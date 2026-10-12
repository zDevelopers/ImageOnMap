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

import java.util.regex.Pattern;

/**
 * Legacy section-sign formatting codes.
 *
 * <p>ImageOnMap's translations embed formatting as legacy codes (e.g. {@code {red}}), so texts are built as legacy
 * strings and converted to Adventure components with {@link Text#legacy(String)} where the API needs them.</p>
 */
public enum ChatColor {
    BLACK('0', false),
    DARK_BLUE('1', false),
    DARK_GREEN('2', false),
    DARK_AQUA('3', false),
    DARK_RED('4', false),
    DARK_PURPLE('5', false),
    GOLD('6', false),
    GRAY('7', false),
    DARK_GRAY('8', false),
    BLUE('9', false),
    GREEN('a', false),
    AQUA('b', false),
    RED('c', false),
    LIGHT_PURPLE('d', false),
    YELLOW('e', false),
    WHITE('f', false),
    MAGIC('k', true),
    BOLD('l', true),
    STRIKETHROUGH('m', true),
    UNDERLINE('n', true),
    ITALIC('o', true),
    RESET('r', false);

    public static final char COLOR_CHAR = '§';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-ORX]");

    private final char code;
    private final boolean format;
    private final String asString;

    ChatColor(char code, boolean format) {
        this.code = code;
        this.format = format;
        this.asString = new String(new char[] {COLOR_CHAR, code});
    }

    /**
     * Gets the formatting code matching the given character.
     *
     * @param code The code character (e.g. {@code 'c'} for red).
     * @return The matching formatting code, or {@code null}.
     */
    public static ChatColor getByChar(char code) {
        final char lower = Character.toLowerCase(code);
        for (ChatColor color : values()) {
            if (color.code == lower) {
                return color;
            }
        }
        return null;
    }

    /**
     * Strips all formatting codes from the given text.
     *
     * @param input The text.
     * @return The text without formatting codes.
     */
    public static String stripColor(String input) {
        if (input == null) {
            return null;
        }
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Gets the formatting codes active at the end of the given text.
     *
     * @param input The text.
     * @return The active formatting codes.
     */
    public static String getLastColors(String input) {
        String result = "";
        final int length = input.length();

        for (int index = length - 1; index > -1; index--) {
            final char section = input.charAt(index);
            if (section == COLOR_CHAR && index < length - 1) {
                final ChatColor color = getByChar(input.charAt(index + 1));
                if (color != null) {
                    result = color + result;
                    if (color.isColor() || color == RESET) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    public char getChar() {
        return code;
    }

    public boolean isFormat() {
        return format;
    }

    public boolean isColor() {
        return !format && this != RESET;
    }

    @Override
    public String toString() {
        return asString;
    }
}
