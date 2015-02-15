/*
 * Copyright (C) 2014 ProkopyL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
