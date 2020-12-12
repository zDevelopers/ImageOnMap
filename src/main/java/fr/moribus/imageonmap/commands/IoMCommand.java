/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2020)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2020)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package fr.moribus.imageonmap.commands;

import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.quartzlib.components.commands.Command;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.i18n.I;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;


public abstract class IoMCommand extends Command {
    protected ImageMap getMapFromArgs() throws CommandException {
        return getMapFromArgs(playerSender(), 0, true);
    }

    protected ArrayList<String> getArgs() {
        ArrayList<String> arguments = new ArrayList<>();

        //State of the automaton, can read word like:
        //name_here; "name here"
        int state = 0;
        StringBuilder s = new StringBuilder();
        for (String arg : args) {
            switch (state) {
                case 0:
                    if (arg.startsWith("\"")) {
                        state = 1;
                        arg = arg.substring(1);

                        s = s.append(arg);
                    } else {
                        arguments.add(arg.toString());
                    }
                    break;
                case 1:
                    if (arg.endsWith("\"")) {
                        arg = arg.substring(0, arg.length() - 1);
                        s = s.append(" " +   arg);
                        arguments.add(s.toString());
                        s = new StringBuilder();
                        state = 0;
                    } else {
                        s = s.append(" " + arg);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + state);
            }
        }
        return arguments;
    }

    //TODO:Add the quote system to zlib and refactor this
    protected ImageMap getMapFromArgs(Player player, int index) throws CommandException {
        if (args.length <= index) {
            throwInvalidArgument(I.t("You need to give a map name."));
        }


        StringBuilder mapName = new StringBuilder(args[index]);
        for (int i = index + 1, c = args.length; i < c; i++) {
            mapName.append(" ").append(args[i]);
        }
        String regex = "((\"([^\\\"]*(\\\\\\\")*)*([^\\\\\\\"]\"))|([^\\\"\\s\\\\]*(\\\\\\s)*[\\\\]*)*\"?)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mapName.toString());

        StringBuilder result = new StringBuilder();

        //matcher.find();
        result.append(matcher.group(0));
        if (result != null) {
            if (result.charAt(0) == '\"') {
                if (result.length() == 1) {
                    result.deleteCharAt(0);
                } else if (result.charAt(result.length() - 1) == '\"') {
                    result = result.deleteCharAt(result.length() - 1);
                    if (result != null && !result.equals("") && result.charAt(0) == '\"') {
                        mapName = new StringBuilder(result.deleteCharAt(0).toString());
                    }

                }
            }
        }


        mapName = new StringBuilder(mapName.toString().trim());
        ImageMap map;
        map = MapManager.getMap(player.getUniqueId(), mapName.toString());

        if (map == null) {
            error(I.t("This map does not exist."));
        }
        return map;
    }

    protected ImageMap getMapFromArgs(Player player, int index, boolean expand) throws CommandException {
        if (args.length <= index) {
            throwInvalidArgument(I.t("You need to give a map name."));
        }

        ImageMap map;
        String mapName = args[index];

        if (expand) {
            for (int i = index + 1, c = args.length; i < c; i++) {
                mapName += " " + args[i];
            }
        }

        mapName = mapName.trim();
        map = MapManager.getMap(player.getUniqueId(), mapName);

        if (map == null) {
            error(I.t("This map does not exist."));
        }

        return map;
    }

    protected List<String> getMatchingMapNames(Player player, String prefix) {
        return getMatchingMapNames(MapManager.getMapList(player.getUniqueId()), prefix);
    }

    protected List<String> getMatchingMapNames(Iterable<? extends ImageMap> maps, String prefix) {
        List<String> matches = new ArrayList<>();

        for (ImageMap map : maps) {
            if (map.getId().startsWith(prefix)) {
                matches.add(map.getId());
            }
        }

        return matches;
    }
}
