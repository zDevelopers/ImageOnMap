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

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.MapManagerException;
import fr.zcraft.zlib.components.commands.CommandException;
import fr.zcraft.zlib.components.commands.CommandInfo;
import fr.zcraft.zlib.components.commands.WithFlags;
import fr.zcraft.zlib.components.i18n.I;
import fr.zcraft.zlib.components.rawtext.RawText;
import fr.zcraft.zlib.tools.PluginLogger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@CommandInfo (name =  "delete", usageParameters = "<map name> [--confirm]")
@WithFlags ({"confirm"})
public class DeleteCommand extends IoMCommand
{
    @Override
    protected void run() throws CommandException
    {
        ImageMap map = getMapFromArgs();
        Player player = playerSender();

        if (!hasFlag("confirm"))
        {
            RawText msg = new RawText(I.t("You are going to delete") + " ")
                .then(map.getId())
                    .color(ChatColor.GOLD)
                .then(". " + I.t("Are you sure ? "))
                    .color(ChatColor.WHITE)
                .then(I.t("[Confirm]"))
                    .color(ChatColor.GREEN)
                    .hover(new RawText(I.t("{red}This map will be deleted {bold}forever{red}!")))
                    .command(getClass(), map.getId(), "--confirm")
                .build();

            send(msg);
        }
        else
        {
            MapManager.clear(player.getInventory(), map);

            try
            {
                MapManager.deleteMap(map);
                info(I.t("Map successfully deleted."));
            }
            catch (MapManagerException ex)
            {
                PluginLogger.warning("A non-existent map was requested to be deleted", ex);
                warning(I.t("This map does not exist."));
            }
        }
    }
    
    @Override
    protected List<String> complete() throws CommandException
    {
        if(args.length == 1) 
            return getMatchingMapNames(playerSender(), args[0]);

        return null;
    }

    @Override
    public boolean canExecute(CommandSender sender)
    {
        return Permissions.DELETE.grantedTo(sender);
    }
}
