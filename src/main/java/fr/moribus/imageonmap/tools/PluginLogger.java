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

import fr.moribus.imageonmap.ImageOnMap;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

/**
 * Static access to the plugin logger.
 *
 * <p>Messages accept {@link MessageFormat} parameters ({@code {0}}, {@code {1}}...). Messages logged from a thread
 * other than the server main thread are prefixed with the name of that thread.</p>
 */
public final class PluginLogger {
    private PluginLogger() {
    }

    public static void log(Level level, String message, Throwable ex) {
        getLogger().log(level, prefix(message), ex);
    }

    public static void log(Level level, String message, Object... args) {
        getLogger().log(level, prefix(format(message, args)));
    }

    public static void log(Level level, String message, Throwable ex, Object... args) {
        log(level, message, args);
        log(level, "Exception : ", ex);
    }

    public static void info(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public static void warning(String message, Object... args) {
        log(Level.WARNING, message, args);
    }

    public static void warning(String message, Throwable ex) {
        log(Level.WARNING, message, ex);
    }

    public static void warning(String message, Throwable ex, Object... args) {
        log(Level.WARNING, message, ex, args);
    }

    public static void error(String message) {
        log(Level.SEVERE, message);
    }

    public static void error(String message, Throwable ex) {
        log(Level.SEVERE, message, ex);
    }

    public static void error(String message, Throwable ex, Object... args) {
        log(Level.SEVERE, message, ex, args);
    }

    public static void error(String message, Object... args) {
        log(Level.SEVERE, message, args);
    }

    private static String format(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }
        try {
            return MessageFormat.format(message, args);
        } catch (IllegalArgumentException ex) {
            return message;
        }
    }

    private static String prefix(String message) {
        if (Bukkit.isPrimaryThread()) {
            return message;
        }
        return "[" + Thread.currentThread().getName() + "] " + message;
    }

    private static Logger getLogger() {
        final ImageOnMap plugin = ImageOnMap.getPlugin();
        return plugin != null ? plugin.getLogger() : Logger.getLogger("ImageOnMap");
    }
}
