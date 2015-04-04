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

package fr.moribus.imageonmap.commands;

import fr.moribus.imageonmap.PluginLogger;

public class CommandException extends Exception
{
    public enum Reason
    {
        COMMANDSENDER_EXPECTED_PLAYER,
        INVALID_PARAMETERS,
        COMMAND_ERROR,
        SENDER_NOT_AUTHORIZED
    }
    
    private final Reason reason;
    private final Command command;
    private final String extra;
    
    public CommandException(Command command, Reason reason, String extra)
    {
        this.command = command;
        this.reason = reason;
        this.extra = extra;
    }
    
    public CommandException(Command command, Reason reason)
    {
        this(command, reason, "");
    }
    
    public Reason getReason() { return reason; }
    
    public String getReasonString()
    {
        switch(reason)
        {
            case COMMANDSENDER_EXPECTED_PLAYER:
                return "You must be a player to use this command.";
            case INVALID_PARAMETERS:
                return "Invalid arguments : " + extra +"\n§r" +
                        "Usage : " + command.getUsageString() + "\n" +
                        "For more information, use /" + 
                        command.getCommandGroup().getUsualName() + " help " +
                        command.getName();
            case COMMAND_ERROR:
                return extra.isEmpty() ? "An unknown error suddenly happened." : extra;
            case SENDER_NOT_AUTHORIZED:
                return "You do not have the permission to use this command.";
            default:
                PluginLogger.LogWarning("Unknown CommandException caught", this);
                return "An unknown error suddenly happened.";
        }
    }
}
