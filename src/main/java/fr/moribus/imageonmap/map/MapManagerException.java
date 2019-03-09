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

package fr.moribus.imageonmap.map;

import fr.zcraft.zlib.components.i18n.I;

import java.text.MessageFormat;

public class MapManagerException extends Exception
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 361594945031622687L;

	public enum Reason
    {
        MAXIMUM_PLAYER_MAPS_EXCEEDED(I.t("You have too many maps (maximum : {0}).")),
        MAXIMUM_SERVER_MAPS_EXCEEDED(I.t("The server ImageOnMap limit has been reached.")),
        IMAGEMAP_DOES_NOT_EXIST(I.t("The given map does not exist."));
        
        private final String reasonString;

        Reason(String reasonString)
        {
            this.reasonString = reasonString;
        }
        
        public String getReasonString(Object ...arguments)
        {
            return MessageFormat.format(reasonString, arguments);
        }
    }


    private final Reason reason;
    
    public MapManagerException(Reason reason, Object ...arguments)
    {
        super(reason.getReasonString(arguments));
        this.reason = reason;
    }
    
    public Reason getReason() { return reason; }
}
