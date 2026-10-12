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

package fr.moribus.imageonmap.i18n;

import java.util.Locale;
import org.bukkit.entity.Player;

/**
 * Short translation methods.
 *
 * <p>Messages are gettext {@code msgid}s, formatted with {@link java.text.MessageFormat} parameters and
 * user-friendly formatting codes like {@code {red}} or {@code {ce}}.</p>
 */
public final class I {
    private I() {
    }

    /**
     * Translates a message into the primary locale.
     *
     * @param text       The message to translate.
     * @param parameters The parameters of the message.
     * @return The translated message.
     */
    public static String t(String text, Object... parameters) {
        return I18n.translate(null, null, text, null, null, parameters);
    }

    /**
     * Translates a message into the given locale.
     *
     * @param locale     The locale (if {@code null}, the primary locale is used).
     * @param text       The message to translate.
     * @param parameters The parameters of the message.
     * @return The translated message.
     */
    public static String t(Locale locale, String text, Object... parameters) {
        return I18n.translate(locale, null, text, null, null, parameters);
    }

    /**
     * Translates a message with plurals into the primary locale.
     *
     * @param singular   The singular form of the message.
     * @param plural     The plural form of the message.
     * @param count      The count used to select the plural form.
     * @param parameters The parameters of the message. If none, the count is used as {@code {0}}.
     * @return The translated message.
     */
    public static String tn(String singular, String plural, Integer count, Object... parameters) {
        return I18n.translate(null, null, singular, plural, count, parameters);
    }

    /**
     * Translates a message with plurals into the given locale.
     *
     * @param locale     The locale (if {@code null}, the primary locale is used).
     * @param singular   The singular form of the message.
     * @param plural     The plural form of the message.
     * @param count      The count used to select the plural form.
     * @param parameters The parameters of the message. If none, the count is used as {@code {0}}.
     * @return The translated message.
     */
    public static String tn(Locale locale, String singular, String plural, Integer count, Object... parameters) {
        return I18n.translate(locale, null, singular, plural, count, parameters);
    }

    /**
     * Translates a message into the given locale.
     *
     * @param locale     The locale (if {@code null}, the primary locale is used).
     * @param text       The message to translate.
     * @param parameters The parameters of the message.
     * @return The translated message.
     */
    public static String tl(Locale locale, String text, Object... parameters) {
        return I18n.translate(locale, null, text, null, null, parameters);
    }

    /**
     * Translates a message into the locale of the given player, and sends it to them.
     *
     * @param player     The receiver.
     * @param text       The message to translate.
     * @param parameters The parameters of the message.
     */
    public static void sendT(Player player, String text, Object... parameters) {
        player.sendMessage(I18n.translate(I18n.getPlayerLocale(player), null, text, null, null, parameters));
    }
}
