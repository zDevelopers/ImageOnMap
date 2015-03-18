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

package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.commands.Command;
import fr.moribus.imageonmap.commands.CommandException;
import fr.moribus.imageonmap.commands.CommandInfo;
import fr.moribus.imageonmap.commands.Commands;
import java.net.MalformedURLException;
import java.net.URL;
import org.bukkit.entity.Player;

@CommandInfo(name = "new", usageParameters = "<URL> [resize]")
public class NewCommand  extends Command
{
    public NewCommand(Commands commandGroup) {
        super(commandGroup);
    }
    
    @Override
    protected void run() throws CommandException
    {
        Player player = playerSender();
        URL url;
        
        if(args.length < 1) throwInvalidArgument("You must give an URL to take the image from.");
        
        try
        {
            url = new URL(args[0]);
        }
        catch(MalformedURLException ex)
        {
            throwInvalidArgument("Invalid URL.");
        }
        
        if(args.length < 2)
        {
            
        }
        
        info("Not implemented.");
    }

}
