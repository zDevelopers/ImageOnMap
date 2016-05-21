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

package fr.moribus.imageonmap;

import fr.zcraft.zlib.components.configuration.Configuration;
import fr.zcraft.zlib.components.configuration.ConfigurationItem;

import java.util.Locale;

import static fr.zcraft.zlib.components.configuration.ConfigurationItem.item;


public final class PluginConfiguration extends Configuration
{
    static public ConfigurationItem<Locale> LANG = item("lang", Locale.class);

    static public ConfigurationItem<Boolean> COLLECT_DATA = item("collect-data", true);

    static public ConfigurationItem<Integer> MAP_GLOBAL_LIMIT = item("map-global-limit", 0, "Limit-map-by-server");
    static public ConfigurationItem<Integer> MAP_PLAYER_LIMIT = item("map-player-limit", 0, "Limit-map-by-player");
}
