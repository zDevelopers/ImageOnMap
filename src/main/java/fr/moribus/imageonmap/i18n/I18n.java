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

import fr.moribus.imageonmap.tools.ChatColor;
import fr.moribus.imageonmap.tools.PluginLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Internationalization: loads gettext {@code .po} translation files and translates messages.
 *
 * <p>Translations are loaded from the {@code i18n} directory of the plugin JAR, and then from the {@code i18n}
 * directory of the plugin data folder, whose translations take precedence.</p>
 */
public final class I18n {
    private static final String I18N_DIRECTORY = "i18n";
    private static final int JAR_PRIORITY = 0;
    private static final int DATA_FOLDER_PRIORITY = 100;

    private static final Map<Locale, List<Translator>> translators = new ConcurrentHashMap<>();

    private static final String ERROR_COLOR = ChatColor.RED.toString();
    private static final String NOTICE_COLOR = ChatColor.WHITE.toString();
    private static final String SUCCESS_COLOR = ChatColor.GREEN.toString();
    private static final String STATUS_COLOR = ChatColor.GRAY.toString();
    private static final String COMMAND_COLOR = ChatColor.GOLD.toString();

    private static Locale primaryLocale = null;
    private static Locale fallbackLocale = null;

    private I18n() {
    }

    /**
     * Loads the translations of the given plugin.
     *
     * @param plugin  The plugin.
     * @param jarFile The plugin JAR file.
     */
    public static void init(JavaPlugin plugin, File jarFile) {
        translators.clear();
        setPrimaryLocale(Locale.getDefault());
        fallbackLocale = Locale.US;

        if (jarFile != null) {
            loadFromJar(plugin, jarFile);
        }
        load(new File(plugin.getDataFolder(), I18N_DIRECTORY));
    }

    /**
     * Unloads all translations.
     */
    public static void exit() {
        translators.clear();
    }

    private static void loadFromJar(JavaPlugin plugin, File file) {
        final String directory = I18N_DIRECTORY + "/";

        try (JarFile jarFile = new JarFile(file)) {
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (!name.startsWith(directory) || name.endsWith("/") || !name.endsWith(".po")) {
                    continue;
                }

                final String[] nameParts = name.split("/");
                final String basename = nameParts[nameParts.length - 1];
                final ReaderSupplier reader = () -> {
                    final InputStream stream = plugin.getResource(name);
                    return stream != null ? new InputStreamReader(stream, StandardCharsets.UTF_8) : null;
                };
                registerTranslator(new Translator(localeFromString(basename.split("\\.")[0]), "jar:" + name,
                        JAR_PRIORITY, reader));
            }
        } catch (IOException e) {
            PluginLogger.error("Unable to load JAR file {0}", e, file.getAbsolutePath());
        }
    }

    private static void load(final File file) {
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children == null) {
                return;
            }
            for (final File child : children) {
                load(child);
            }
        } else if (file.isFile() && file.getName().toLowerCase().endsWith(".po")) {
            registerTranslator(new Translator(localeFromString(file.getName().split("\\.")[0]),
                    file.getAbsolutePath(), DATA_FOLDER_PRIORITY,
                    () -> new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)));
        }
    }

    private static void registerTranslator(final Translator translator) {
        final List<Translator> chain = translators.computeIfAbsent(translator.locale,
                locale -> new CopyOnWriteArrayList<>());
        chain.add(translator);
        chain.sort((t1, t2) -> t1.priority == t2.priority
                ? t1.path.compareTo(t2.path)
                : Integer.compare(t2.priority, t1.priority));
    }

    private static String translateFromChain(Locale locale, String context, String messageId,
                                             String messageIdPlural, Integer count) {
        final List<Translator> chain = translators.get(locale);
        if (chain == null) {
            return null;
        }

        for (Translator translator : chain) {
            final String translated = translator.translate(context, messageId, messageIdPlural, count);
            if (translated != null) {
                return translated;
            }
        }

        return null;
    }

    /**
     * Translates a message.
     *
     * @param locale          The locale to translate into. If {@code null} or not available, the primary locale
     *                        is used, then the fallback locale (English).
     * @param context         The context of the message (can be {@code null}).
     * @param messageId       The message.
     * @param messageIdPlural The plural form of the message (can be {@code null}).
     * @param count           The count used to select a plural form (can be {@code null}).
     * @param parameters      The parameters of the message.
     * @return The translated and formatted message.
     */
    public static String translate(Locale locale, String context, String messageId, String messageIdPlural,
                                   Integer count, Object... parameters) {
        String translated = null;
        Locale usedLocale = Locale.getDefault();

        // The count is likely to be used in the string, so if only a count is given, it is also used as the first
        // and only parameter ({0}).
        if (count != null && (parameters == null || parameters.length == 0)) {
            parameters = new Object[] {count};
        }

        if (locale != null) {
            translated = translateFromChain(locale, context, messageId, messageIdPlural, count);
            if (translated != null) {
                usedLocale = locale;
            } else {
                // Then, we look up for a close locale (same language and country, then same language)
                Locale perfect = null;
                final Set<Locale> close = new HashSet<>();

                for (Locale curLocale : translators.keySet()) {
                    if (curLocale.getLanguage().equals(locale.getLanguage())) {
                        if (curLocale.getCountry().equals(locale.getCountry())) {
                            perfect = curLocale;
                        } else {
                            close.add(curLocale);
                        }
                    }
                }

                if (perfect != null) {
                    translated = translateFromChain(perfect, context, messageId, messageIdPlural, count);
                }

                if (translated != null) {
                    usedLocale = perfect;
                } else {
                    for (Locale closeLocale : close) {
                        translated = translateFromChain(closeLocale, context, messageId, messageIdPlural, count);
                        if (translated != null) {
                            usedLocale = closeLocale;
                            break;
                        }
                    }
                }
            }
        }

        if (translated == null && primaryLocale != null) {
            translated = translateFromChain(primaryLocale, context, messageId, messageIdPlural, count);
            if (translated != null) {
                usedLocale = primaryLocale;
            }
        }

        if (translated == null && fallbackLocale != null) {
            translated = translateFromChain(fallbackLocale, context, messageId, messageIdPlural, count);
            if (translated != null) {
                usedLocale = fallbackLocale;
            }
        }

        // If it's still null, we use the original message, using English plural rules.
        if (translated == null) {
            if (count != null && count != 1 && messageIdPlural != null) {
                translated = messageIdPlural;
            } else {
                translated = messageId;
            }
            usedLocale = primaryLocale != null ? primaryLocale
                    : (fallbackLocale != null ? fallbackLocale : Locale.getDefault());
        }

        translated = replaceFormattingCodes(translated);

        // Single quotes are escaped so the formatter keeps them.
        final MessageFormat formatter = new MessageFormat(translated.replace("'", "''"), usedLocale);

        // Non-breaking spaces are removed, as Minecraft does not handle them well.
        return formatter.format(parameters)
                .replace(" ", " ").replace(" ", " ").replace(" ", " ")
                .replace(" ", " ")
                .replace("⁠", "");
    }

    private static String replaceFormattingCodes(String text) {
        return text.replace("{black}", ChatColor.BLACK.toString())
                .replace("{darkblue}", ChatColor.DARK_BLUE.toString())
                .replace("{darkgreen}", ChatColor.DARK_GREEN.toString())
                .replace("{darkaqua}", ChatColor.DARK_AQUA.toString())
                .replace("{darkred}", ChatColor.DARK_RED.toString())
                .replace("{darkpurple}", ChatColor.DARK_PURPLE.toString())
                .replace("{gold}", ChatColor.GOLD.toString())
                .replace("{gray}", ChatColor.GRAY.toString())
                .replace("{darkgray}", ChatColor.DARK_GRAY.toString())
                .replace("{blue}", ChatColor.BLUE.toString())
                .replace("{green}", ChatColor.GREEN.toString())
                .replace("{aqua}", ChatColor.AQUA.toString())
                .replace("{red}", ChatColor.RED.toString())
                .replace("{lightpurple}", ChatColor.LIGHT_PURPLE.toString())
                .replace("{yellow}", ChatColor.YELLOW.toString())
                .replace("{white}", ChatColor.WHITE.toString())
                .replace("{bold}", ChatColor.BOLD.toString())
                .replace("{strikethrough}", ChatColor.STRIKETHROUGH.toString())
                .replace("{underline}", ChatColor.UNDERLINE.toString())
                .replace("{italic}", ChatColor.ITALIC.toString())
                .replace("{obfuscated}", ChatColor.MAGIC.toString())
                .replace("{reset}", ChatColor.RESET.toString())
                .replace("{ce}", ERROR_COLOR)
                .replace("{cc}", COMMAND_COLOR)
                .replace("{ci}", NOTICE_COLOR)
                .replace("{cs}", SUCCESS_COLOR)
                .replace("{cst}", STATUS_COLOR);
    }

    /**
     * Returns the locale used by the client of the given player.
     *
     * @param player The player.
     * @return The player's locale, or {@code null} if the player is {@code null}.
     */
    public static Locale getPlayerLocale(Player player) {
        if (player == null) {
            return null;
        }
        return player.locale();
    }

    public static Locale getPrimaryLocale() {
        return primaryLocale;
    }

    /**
     * Sets the primary locale, used when no locale is specified or when a translation is missing from the requested
     * locale.
     *
     * @param locale The locale. If {@code null}, the system's default locale is used.
     */
    public static void setPrimaryLocale(final Locale locale) {
        primaryLocale = locale != null ? locale : Locale.getDefault();
    }

    /**
     * Converts a locale name like {@code fr_FR} or {@code fr-FR} into a locale.
     *
     * @param localeName The locale name.
     * @return The locale.
     */
    public static Locale localeFromString(final String localeName) {
        final String[] splitLocale = localeName.split("[_\\-]", 2);
        if (splitLocale.length >= 2) {
            return Locale.of(splitLocale[0], splitLocale[1]);
        } else {
            return Locale.of(localeName);
        }
    }

    @FunctionalInterface
    private interface ReaderSupplier {
        Reader open() throws IOException;
    }

    /**
     * A translation file, loaded lazily on first use.
     */
    private static final class Translator {
        private final Locale locale;
        private final String path;
        private final int priority;
        private final ReaderSupplier readerSupplier;
        private volatile POFile source = null;
        private volatile boolean loaded = false;

        private Translator(Locale locale, String path, int priority, ReaderSupplier readerSupplier) {
            this.locale = locale;
            this.path = path;
            this.priority = priority;
            this.readerSupplier = readerSupplier;
        }

        private synchronized void load() {
            if (loaded) {
                return;
            }
            loaded = true;

            try (Reader reader = readerSupplier.open()) {
                if (reader == null) {
                    PluginLogger.error("Unable to load file {0}", path);
                    return;
                }
                final POFile file = new POFile(reader);
                file.parse();
                source = file;
            } catch (IOException | POFile.CannotParsePOException e) {
                PluginLogger.error("Cannot parse the {0} translations file.", e, path);
                source = null;
            }
        }

        private String translate(String context, String messageId, String messageIdPlural, Integer count) {
            if (!loaded) {
                load();
            }
            if (source == null) {
                return null;
            }

            final POFile.Translation translation = source.getTranslation(context, messageId);
            if (translation == null || translation.getTranslations().isEmpty()) {
                return null;
            }

            if (count != null && translation.getTranslations().size() != 1) {
                final int pluralIndex = source.computePluralForm(count);
                if (translation.getTranslations().size() <= pluralIndex) {
                    return null;
                }
                return translation.getTranslations().get(pluralIndex);
            } else {
                return translation.getTranslations().get(0);
            }
        }
    }
}
