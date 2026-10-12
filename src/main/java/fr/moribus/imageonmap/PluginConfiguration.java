/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
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

package fr.moribus.imageonmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import org.bukkit.configuration.file.FileConfiguration;


public final class PluginConfiguration {
    public static final ConfigurationItem<Locale> LANG = item("lang", null, PluginConfiguration::toLocale);

    public static final ConfigurationItem<Boolean> COLLECT_DATA = item("collect-data", true);

    public static final ConfigurationItem<Boolean> CHECK_FOR_UPDATES = item("check-for-updates", true);

    public static final ConfigurationItem<Integer> MAP_GLOBAL_LIMIT =
            item("map-global-limit", 0, "Limit-map-by-server");
    public static final ConfigurationItem<Integer> MAP_PLAYER_LIMIT =
            item("map-player-limit", 0, "Limit-map-by-player");

    public static final ConfigurationItem<Boolean> SAVE_FULL_IMAGE = item("save-full-image", true);


    public static final ConfigurationItem<Integer> LIMIT_SIZE_X = item("limit-map-size-x", 0);
    public static final ConfigurationItem<Integer> LIMIT_SIZE_Y = item("limit-map-size-y", 0);

    public static final ConfigurationItem<List<String>> IMAGES_HOSTNAMES_WHITELIST =
            item("images-hostnames-whitelist", Collections.emptyList(), PluginConfiguration::toStringList);

    private PluginConfiguration() {
    }

    private static ConfigurationItem<Boolean> item(String path, boolean defaultValue) {
        return item(path, defaultValue, PluginConfiguration::toBoolean);
    }

    private static ConfigurationItem<Integer> item(String path, int defaultValue, String... deprecatedPaths) {
        return item(path, defaultValue, PluginConfiguration::toInteger, deprecatedPaths);
    }

    private static <T> ConfigurationItem<T> item(String path, T defaultValue, Function<Object, T> parser,
                                                 String... deprecatedPaths) {
        return new ConfigurationItem<>(path, defaultValue, parser, deprecatedPaths);
    }

    private static Locale toLocale(Object value) {
        if (value.toString().isEmpty()) {
            return null;
        }
        try {
            return new Locale.Builder().setLanguageTag(value.toString()).build();
        } catch (IllformedLocaleException ex) {
            return null;
        }
    }

    private static Boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        switch (value.toString().trim().toLowerCase()) {
            case "true":
            case "yes":
            case "on":
            case "1":
                return true;
            case "false":
            case "no":
            case "off":
            case "0":
                return false;
            default:
                return null;
        }
    }

    private static Integer toInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static List<String> toStringList(Object value) {
        if (!(value instanceof List)) {
            return null;
        }
        final List<String> list = new ArrayList<>();
        for (Object element : (List<?>) value) {
            if (element != null) {
                list.add(element.toString());
            }
        }
        return list;
    }

    /**
     * A value of the configuration file. The value is read from the configuration every time it is requested.
     *
     * @param <T> The type of the value.
     */
    public static final class ConfigurationItem<T> {
        private final String path;
        private final T defaultValue;
        private final Function<Object, T> parser;
        private final String[] deprecatedPaths;

        private ConfigurationItem(String path, T defaultValue, Function<Object, T> parser, String[] deprecatedPaths) {
            this.path = path;
            this.defaultValue = defaultValue;
            this.parser = parser;
            this.deprecatedPaths = deprecatedPaths;
        }

        /**
         * Returns the value of this item.
         *
         * @return The configured value, or the default value if the configured one is missing or invalid.
         */
        public T get() {
            final FileConfiguration config = ImageOnMap.getPlugin().getConfig();

            Object rawValue = config.get(path);
            for (int i = 0; rawValue == null && i < deprecatedPaths.length; i++) {
                rawValue = config.get(deprecatedPaths[i]);
            }

            if (rawValue == null) {
                return defaultValue;
            }

            final T value = parser.apply(rawValue);
            return value != null ? value : defaultValue;
        }
    }
}
