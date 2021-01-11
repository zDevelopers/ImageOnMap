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
import fr.moribus.imageonmap.image.ImageRendererExecutor;
import fr.moribus.imageonmap.image.ImageUtils;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.worker.WorkerCallback;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.quartzlib.tools.text.ActionBar;
import fr.zcraft.quartzlib.tools.text.MessageSender;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name = "update", usageParameters = "[playername] <new url> [stretched|covered] <map name to update>")
public class UpdateCommand extends IoMCommand {
    @Override
    protected void run() throws CommandException {

        ArrayList<String> arguments = getArgs();
        String warningMsg;
        if (arguments.size() > 4) {
            warningMsg = "Too many parameters!"
                    + " Usage: /maptool update [playername] <new url> [stretched|covered] <mapname>";
            warning(I.t(warningMsg));
            return;
        }
        if (arguments.size() < 2) {
            warningMsg =
                    "Too few parameters! Usage: /maptool update [playername] <new url> [stretched|covered] <mapname>";
            warning(I.t(warningMsg));
            return;
        }
        final String playerName;
        final String mapName;
        final String url;
        final String resize;
        final Player sender = playerSender();


        if (arguments.size() == 2) {
            resize = "";
            playerName = sender.getName();
            mapName = arguments.get(1);
            url = arguments.get(0);
        } else {
            if (arguments.size() == 4) {
                if (!Permissions.UPDATEOTHER.grantedTo(sender)) {
                    info(sender, I.t("You can't use this command"));
                    return;
                }

                playerName = arguments.get(0);
                url = arguments.get(1);
                resize = arguments.get(2);
                mapName = arguments.get(3);
            } else {
                if (arguments.size() == 3) {
                    if (arguments.get(1).equals("covered") || arguments.get(1).equals("stretched")) {

                        playerName = sender.getName();
                        url = arguments.get(0);
                        resize = arguments.get(1);
                        mapName = arguments.get(2);

                    } else {
                        if (!Permissions.UPDATEOTHER.grantedTo(sender)) {
                            info(sender, I.t("You can't use this command"));
                            return;
                        }
                        playerName = arguments.get(0);
                        url = arguments.get(1);
                        resize = "";
                        mapName = arguments.get(2);
                    }
                } else {
                    resize = "";
                    playerName = "";
                    url = "";
                    mapName = "";
                }
            }
        }


        final ImageUtils.ScalingType scaling;


        switch (resize) {

            case "stretched":
                scaling = ImageUtils.ScalingType.STRETCHED;
                break;
            case "covered":
                scaling = ImageUtils.ScalingType.COVERED;
                break;
            default:
                scaling = ImageUtils.ScalingType.CONTAINED;
        }


        //TODO passer en static
        ImageOnMap.getPlugin().getCommandWorker().offlineNameFetch(playerName, uuid -> {
            if (uuid == null) {
                info(sender, I.t("The player {0} does not exist.", playerName));
                return;
            }
            ImageMap map = MapManager.getMap(uuid, mapName);

            if (map == null) {
                info(sender, I.t("This map does not exist."));
                return;
            }

            URL url1;
            try {
                url1 = new URL(url);
                MapManager.load();

                Integer[] size = {1, 1};
                if (map.getType() == ImageMap.Type.POSTER) {
                    size = map.getSize(new HashMap<String, Object>(), map.getUserUUID(), map.getId());
                }
                //assert size != null;
                int width = size[0];
                int height = size[1];
                try {
                    ActionBar.sendPermanentMessage(sender, ChatColor.DARK_GREEN + I.t("Updating..."));
                    ImageRendererExecutor
                            .update(url1, scaling, uuid, map, width, height, new WorkerCallback<ImageMap>() {
                                @Override
                                public void finished(ImageMap result) {
                                    ActionBar.removeMessage(sender);
                                    MessageSender.sendActionBarMessage(sender,
                                            ChatColor.DARK_GREEN + I.t("The map was updated using the new image!"));
                                }

                                @Override
                                public void errored(Throwable exception) {
                                    sender.sendMessage(I.t("{ce}Map rendering failed: {0}", exception.getMessage()));

                                    PluginLogger.warning("Rendering from {0} failed: {1}: {2}",
                                            sender.getName(),
                                            exception.getClass().getCanonicalName(),
                                            exception.getMessage());
                                }
                            });
                } finally {
                    ActionBar.removeMessage(sender);
                }
            } catch (MalformedURLException ex) {
                warning(sender, I.t("Invalid URL."));
            }


        });


    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.UPDATE.grantedTo(sender);
    }
}
