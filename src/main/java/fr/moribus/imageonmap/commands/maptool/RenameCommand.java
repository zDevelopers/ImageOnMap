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
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

@CommandInfo(name = "rename", usageParameters = "<original map name> <new map name>")
public class RenameCommand extends IoMCommand {

    @Override
    protected void run() throws CommandException {

        ArrayList<String> argList = getArgs();

        if (argList.size() != 2) {
            warning(I.t("Not enough or too many arguments! Usage: /maptool rename <map name> <new map name>"));
            return;
        }


        ImageMap map = MapManager.getMap(playerSender().getUniqueId(), argList.get(0));
        if (map == null) {
            error(I.t("This map does not exist."));
            return;
        }
        map.rename(argList.get(1));

    }

    @Override
    protected List<String> complete() throws CommandException {

        if (args.length == 1) {
            return getMatchingMapNames(playerSender(), args[0]);
        }
        return null;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.RENAME.grantedTo(sender);
    }
}
