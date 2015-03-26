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

import fr.moribus.imageonmap.PluginLogger;
import fr.moribus.imageonmap.commands.Command;
import fr.moribus.imageonmap.commands.CommandException;
import fr.moribus.imageonmap.commands.CommandInfo;
import fr.moribus.imageonmap.commands.Commands;
import fr.moribus.imageonmap.image.ImageRendererExecutor;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.worker.WorkerCallback;
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
        final Player player = playerSender();
        boolean scaling = false;
        URL url;
        
        if(args.length < 1) throwInvalidArgument("You must give an URL to take the image from.");
        
        try
        {
            url = new URL(args[0]);
        }
        catch(MalformedURLException ex)
        {
            throwInvalidArgument("Invalid URL.");
            return;
        }
        
        if(args.length >= 2)
        {
            if(args[1].equals("resize")) scaling = true;
        }
        
        info("Rendering ...");
        ImageRendererExecutor.Render(url, scaling, player.getUniqueId(), new WorkerCallback<ImageMap>()
        {
            @Override
            public void finished(ImageMap result)
            {
                player.sendMessage("§7Rendering finished !");
                result.give(player.getInventory());
            }

            @Override
            public void errored(Throwable exception)
            {
                player.sendMessage("§cMap rendering failed : " + exception.getMessage());
                PluginLogger.LogWarning("Rendering from '" + player.getName() + "' failed", exception);
            }
        });
    }

}
