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
import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.rawtext.RawText;
import fr.zcraft.quartzlib.components.rawtext.RawTextPart;
import fr.zcraft.quartzlib.tools.text.RawMessage;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name = "list", usageParameters = "[player name]")
public class ListCommand extends IoMCommand {
    @Override
    protected void run() throws CommandException {
        ArrayList<String> arguments = getArgs();
        if (arguments.size() > 1) {
            throwInvalidArgument(I.t("Too many parameters!"));
            return;
        }

        String playerName;
        if (arguments.size() == 1) {
            if (!Permissions.LISTOTHER.grantedTo(sender)) {
                throwNotAuthorized();
                return;
            }

            playerName = arguments.get(0);
        } else {
            playerName = playerSender().getName();
        }

        final Player sender = playerSender();

        //TODO passer en static
        ImageOnMap.getPlugin().getCommandWorker().offlineNameFetch(playerName, uuid -> {
            if (!sender.isOnline()) {
                return;
            }
            if (uuid == null) {
                try {
                    throwInvalidArgument(I.t("Player {} not found.", playerName));
                } catch (CommandException e) {
                    e.printStackTrace();
                }
                return;
            }
            List<ImageMap> mapList = MapManager.getMapList(uuid);
            if (mapList.isEmpty()) {
                info(sender, I.t("No map found."));
                return;
            }
            String message = I.tn("{white}{bold}{0} map found.",
                    "{white}{bold}{0} maps found.",
                    mapList.size());

            info(sender, I.tn("{white}{bold}{0} map found.", "{white}{bold}{0} maps found.", mapList.size()));

            RawTextPart rawText = new RawText("");
            rawText = addMap(rawText, mapList.get(0));

            //TODO pagination chat
            for (int i = 1, c = mapList.size(); i < c; i++) {
                rawText = rawText.then(", ").color(ChatColor.GRAY);
                rawText = addMap(rawText, mapList.get(i));
            }
            RawMessage.send(sender, rawText.build());

        });
    }

    private RawTextPart<?> addMap(RawTextPart<?> rawText, ImageMap map) {
        final String size = map.getType() == ImageMap.Type.SINGLE ? "1 × 1" :
                ((PosterMap) map).getColumnCount() + " × " + ((PosterMap) map).getRowCount();

        return rawText
                .then(map.getId())
                .color(ChatColor.WHITE)
                .command(GetCommand.class, map.getId())
                .hover(new RawText()
                        .then(map.getName()).style(ChatColor.BOLD, ChatColor.GREEN).then("\n")
                        .then(map.getId() + ", " + size).color(ChatColor.GRAY).then("\n\n")
                        .then(I.t("{white}Click{gray} to get this map"))
                );
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.LIST.grantedTo(sender);
    }
}
