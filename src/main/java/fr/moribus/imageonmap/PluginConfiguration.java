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
import org.bukkit.plugin.Plugin;

public enum PluginConfiguration
{
    //Configuration field Names, with default values
    COLLECT_DATA("collect-data", true),
    MAP_GLOBAL_LIMIT("map-global-limit", 0, "Limit-map-by-server"),
    MAP_PLAYER_LIMIT("map-player-limit", 0, "Limit-map-by-player");
    
    private final String fieldName;
    private final Object defaultValue;
    private final String[] deprecatedNames;
    
    private PluginConfiguration(String fieldName, Object defaultValue, String ... deprecatedNames)
    {
        this.fieldName = fieldName;
        this.defaultValue = defaultValue;
        this.deprecatedNames = deprecatedNames;
    }
    
    public Object get()
    {
        return getConfig().get(fieldName, defaultValue);
    }
    
    public Object getDefaultValue()
    {
        return defaultValue;
    }
    
    public boolean isDefined()
    {
        return getConfig().contains(fieldName);
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
    
    private boolean init()
    {
        boolean affected = false;
        
        if(!isDefined())
        {
            getConfig().set(fieldName, defaultValue);
            affected = true;
        }
        
        for(String deprecatedName : deprecatedNames)
        {
            if(getConfig().contains(deprecatedName))
            {
                getConfig().set(fieldName, getConfig().get(deprecatedName));
                getConfig().set(deprecatedName, null);
                affected = true;
            }
        }
        return affected;
    }
    
    /* ===== Static API ===== */
    
    static private Plugin plugin;
    static public FileConfiguration getConfig()
    {
        return plugin.getConfig();
    }
    
    static public void init(Plugin plugin)
    {
        PluginConfiguration.plugin = plugin;
        loadDefaultValues();
    }
    
    static private void loadDefaultValues()
    {
        boolean affected = false;
        
        for(PluginConfiguration configField : PluginConfiguration.values())
        {
            if(configField.init()) affected = true;
        }
        
        if(affected) plugin.saveConfig();
    }
}
