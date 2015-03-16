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

import org.bukkit.configuration.file.FileConfiguration;

public enum PluginConfiguration
{
    //Configuration field Names, with default values
    COLLECT_DATA("collect-data", true),
    MAP_GLOBAL_LIMIT("map-global-limit", 0),
    MAP_PLAYER_LIMIT("map-player-limit", 0);
    
    private final String fieldName;
    private final Object defaultValue;
    
    private PluginConfiguration(String fieldName, Object defaultValue)
    {
        this.fieldName = fieldName;
        this.defaultValue = defaultValue;
    }
    
    public Object get()
    {
        return getConfig().get(fieldName, defaultValue);
    }
    
    public Object getDefaultValue()
    {
        return defaultValue;
    }
    
    public boolean isDefaultValue()
    {
        return get().equals(defaultValue);
    }
    
    @Override
    public String toString()
    {
        return get().toString();
    }
    
    public String getString()
    {
        return getConfig().getString(fieldName, (String)defaultValue);
    }
    
    public int getInteger()
    {
        return getConfig().getInt(fieldName, (Integer)defaultValue);
    }
    
    public boolean getBoolean()
    {
        return getConfig().getBoolean(fieldName, (Boolean)defaultValue);
    }
    
    static public FileConfiguration getConfig()
    {
        return ImageOnMap.getPlugin().getConfig();
    }
}
