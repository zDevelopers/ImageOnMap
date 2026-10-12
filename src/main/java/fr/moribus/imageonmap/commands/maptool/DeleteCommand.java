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

import fr.moribus.imageonmap.Permissions;
import fr.moribus.imageonmap.commands.Command;
import fr.moribus.imageonmap.commands.CommandException;
import fr.moribus.imageonmap.commands.CommandInfo;
import fr.moribus.imageonmap.commands.Commands;
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.commands.WithFlags;
import fr.moribus.imageonmap.i18n.I;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.MapManagerException;
import fr.moribus.imageonmap.tools.PluginLogger;
import fr.moribus.imageonmap.tools.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name = "delete", usageParameters = "[player name]:<map name> [--confirm]")
@WithFlags({"confirm"})
public class DeleteCommand extends IoMCommand {

    private static Component deleteMsg(Class<? extends Command> klass, String playerName, ImageMap map) {
        final String confirmCommand = Commands.getCommandInfo(klass)
                .build(playerName + ":" + "\"" + map.getId() + "\"", "--confirm");

        return Component.text()
                .append(Text.legacy(I.t("You are going to delete") + " "))
                .append(Component.text(map.getId(), NamedTextColor.GOLD))
                .append(Text.legacy(". " + I.t("Are you sure ? ")).color(NamedTextColor.WHITE))
                .append(Text.legacy(I.t("[Confirm]")).color(NamedTextColor.GREEN)
                        .hoverEvent(HoverEvent.showText(Text.legacy(
                                I.t("{red}This map will be deleted {bold}forever{red}!"))))
                        .clickEvent(ClickEvent.runCommand(confirmCommand)))
                .build();
    }

    @Override
    protected void run() throws CommandException {
        ArrayList<String> arguments = getArgs();
        final boolean confirm = hasFlag("confirm");

        if (arguments.size() > 3 || (arguments.size() > 2 && !confirm)) {
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

        retrieveUUID(playerName, uuid -> {
            ImageMap map = MapManager.getMap(uuid, mapName);

            if (map == null) {
                warning(sender, I.t("This map does not exist."));
                return;
            }

            if (!confirm) {
                sender.sendMessage(deleteMsg(getClass(), playerName, map));
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
