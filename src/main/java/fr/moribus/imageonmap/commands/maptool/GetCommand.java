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

package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.ImageOnMap;
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
import org.bukkit.entity.Player;

@CommandInfo(name = "get")
public class GetCommand extends IoMCommand {
    @Override
    protected void run() throws CommandException {
        ArrayList<String> arguments = getArgs();

        if (arguments.size() > 2) {
            throwInvalidArgument(I.t("Too many parameters!"));
            return;
        }
        if (arguments.size() < 1) {
            throwInvalidArgument(I.t("Too few parameters!"));
            return;
        }
        final String playerName;
        final String mapName;
        final Player sender = playerSender();

        if (arguments.size() == 1) {
            playerName = sender.getName();
            mapName = arguments.get(0);
        } else {
            if (!Permissions.GETOTHER.grantedTo(sender)) {
                throwNotAuthorized();
                return;
            }
            playerName = arguments.get(0);
            mapName = arguments.get(1);
        }




        //TODO passer en static
        ImageOnMap.getPlugin().getCommandWorker().offlineNameFetch(playerName, uuid -> {
            if (uuid == null) {
                warning(sender, I.t("The player {0} does not exist.", playerName));
                return;
            }
            ImageMap map = MapManager.getMap(uuid, mapName);

            if (map == null) {
                warning(sender, I.t("This map does not exist."));
                return;
            }

            if (map.give(sender)) {
                info(I.t("The requested map was too big to fit in your inventory."));
                info(I.t("Use '/maptool getremaining' to get the remaining maps."));
            }
        });
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
        return Permissions.GET.grantedTo(sender);
    }
}
