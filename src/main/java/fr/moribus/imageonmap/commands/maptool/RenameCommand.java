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
import fr.zcraft.quartzlib.tools.PluginLogger;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;

@CommandInfo(name = "rename", usageParameters = "<original map name> <new map name>")
public class RenameCommand extends IoMCommand {

    private ArrayList<String> getArgs() {
        ArrayList<String> arguments = new ArrayList<>();

        //State of the automaton, can read word like:
        //name_here; "name here"
        int state = 0;
        StringBuilder s = new StringBuilder();
        for (String arg : args) {

            PluginLogger.info("arg  " + arg);
            switch (state) {
                case 0:
                    if (arg.startsWith("\"")) {
                        PluginLogger.info("start with ");
                        state = 1;
                        arg = arg.substring(1);

                        s = s.append(arg);
                    } else {
                        PluginLogger.info("not start with ");
                        arguments.add(arg.toString());
                    }
                    break;
                case 1:
                    if (arg.endsWith("\"")) {
                        PluginLogger.info("end with ");
                        arg = arg.substring(0, arg.length() - 1);
                        s = s.append(" " +   arg);
                        arguments.add(s.toString());
                        s = new StringBuilder();
                        state = 0;
                    } else {
                        PluginLogger.info("not end with ");
                        s = s.append(" " + arg);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + state);
            }
            //arguments.add(arg.toString());


        }
        return arguments;
    }

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
