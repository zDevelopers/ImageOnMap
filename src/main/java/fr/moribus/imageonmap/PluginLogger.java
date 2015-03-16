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

import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class PluginLogger 
{
    static private Logger getLogger()
    {
        return ImageOnMap.getPlugin().getLogger();
    }
    
    static public void LogInfo(String message)
    {
        getLogger().log(Level.INFO, message);
    }
    
    static public void LogWarning(String message)
    {
        getLogger().log(Level.WARNING, message);
    }
    
    static public void LogWarning(String message, Throwable ex)
    {
        getLogger().log(Level.WARNING, message, ex);
    }
    
    static public void LogError(String message, Throwable ex)
    {
        getLogger().log(Level.SEVERE, message, ex);
    }
}
