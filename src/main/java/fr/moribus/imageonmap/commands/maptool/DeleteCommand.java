/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
 *
 * This software is a computer program whose purpose is to allow insertion of
 * custom images in a Minecraft world.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
 */

package fr.moribus.imageonmap.commands.maptool;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.MapManagerException;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.commands.WithFlags;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.quartzlib.tools.PluginLogger.DebugLevel;
import fr.zcraft.quartzlib.tools.text.RawMessage;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name = "delete", usageParameters = "[player name]:<map name> [--confirm]")
@WithFlags({"confirm"})
public class DeleteCommand extends IoMCommand {

    private static RawText deleteMsg(Class klass, String playerName, ImageMap map) {
        return new RawText(I.t("You are going to delete") + " ")
                .then(map.getId())
                .color(ChatColor.GOLD)
                .then(". " + I.t("Are you sure ? "))
                .color(ChatColor.WHITE)
                .then(I.t("[Confirm]"))
                .color(ChatColor.GREEN)
                .hover(new RawText(I.t("{red}This map will be deleted {bold}forever{red}!")))
                .command(klass, playerName + ":" + "\"" + map.getId() + "\"", "--confirm")
                .build();
    }

    public void invalidArgument(String msg) throws CommandException {
        PluginLogger.debug(ImageOnMap.getPlugin(), DebugLevel.DEVELOPER_LOG, msg);
        throwInvalidArgument(I.t(msg));
    }


    @Override
    protected void run() throws CommandException {
        PluginLogger.debug(ImageOnMap.getPlugin(), DebugLevel.DEVELOPER_LOG, "DeleteCommand");

        ArrayList<String> arguments = getArgs();
        PluginLogger.debug(ImageOnMap.getPlugin(), DebugLevel.DEVELOPER_LOG, arguments);
        final boolean confirm = hasFlag("confirm");

        if (arguments.size() > 3 || (arguments.size() > 2 && !confirm)) {
            invalidArgument(I.t("Too many parameters!"));
            return;
        }
        if (arguments.size() < 1) {
            invalidArgument(I.t("Too few parameters!"));
            return;
        }

        final String playerName;
        final String mapName;
        final Player sender = playerSender();

        if (arguments.size() == 2 || arguments.size() == 3) {
            if (!Permissions.DELETEOTHER.grantedTo(sender)) {
                throwNotAuthorized();
                return;
            }

            playerName = arguments.get(0);
            mapName = arguments.get(1);
        } else {
            playerName = sender.getName();
            mapName = arguments.get(0);
        }
        PluginLogger
                .debug(ImageOnMap.getPlugin(), DebugLevel.DEVELOPER_LOG, "player {0} map name {1} command sender {2}",
                        playerName, mapName,
                        sender);
        //TODO passer en static
        ImageOnMap.getPlugin().getCommandWorker().offlineNameFetch(playerName, uuid -> {
            PluginLogger.debug(ImageOnMap.getPlugin(), DebugLevel.DEVELOPER_LOG,
                    "offlineNameFetch uuid " + uuid.toString());
            if (uuid == null) {
                warning(sender, I.t("The player {0} does not exist.", playerName));
                return;
            }

            PluginLogger.debug(ImageOnMap.getPlugin(), DebugLevel.DEVELOPER_LOG, "offlineNameFetch getMap");
            ImageMap map = MapManager.getMap(uuid, mapName);
            PluginLogger.debug(ImageOnMap.getPlugin(), DebugLevel.DEVELOPER_LOG,
                    "offlineNameFetch map" + map);
            if (map == null) {
                warning(sender, I.t("This map does not exist."));
                return;
            }
            PluginLogger.debug(ImageOnMap.getPlugin(), DebugLevel.DEVELOPER_LOG,
                    "offlineNameFetch map id " + map.getId() + " count " + map.getMapCount());
            if (!confirm) {
                RawText msg = deleteMsg(getClass(), playerName, map);
                RawMessage.send(sender, msg);
            } else {
                if (sender != null && sender.isOnline() && sender.getInventory() != null) {
                    MapManager.clear(sender.getInventory(), map);
                }

                try {
                    MapManager.deleteMap(map);
                    success(sender, I.t("Map successfully deleted."));
                } catch (MapManagerException ex) {
                    PluginLogger.warning(I.t("A non-existent map was requested to be deleted", ex));
                    warning(sender, I.t("This map does not exist."));
                }
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
        return Permissions.DELETE.grantedTo(sender);
    }
}
