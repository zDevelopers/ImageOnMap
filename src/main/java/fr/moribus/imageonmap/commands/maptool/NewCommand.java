/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2022)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2022)
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
import fr.moribus.imageonmap.commands.IoMCommand;
import fr.moribus.imageonmap.gui.RenderGui;
import fr.moribus.imageonmap.image.ImageRendererExecutor;
import fr.moribus.imageonmap.image.ImageUtils;
import fr.moribus.imageonmap.map.ImageMap;
import fr.moribus.imageonmap.map.MapManager;
import fr.moribus.imageonmap.map.PosterMap;
import fr.zcraft.quartzlib.components.commands.CommandException;
import fr.zcraft.quartzlib.components.commands.CommandInfo;
import fr.zcraft.quartzlib.components.gui.Gui;
import fr.zcraft.quartzlib.components.i18n.I;
import fr.zcraft.quartzlib.components.worker.WorkerCallback;
import fr.zcraft.quartzlib.tools.PluginLogger;
import fr.zcraft.quartzlib.tools.text.ActionBar;
import fr.zcraft.quartzlib.tools.text.MessageSender;
import java.net.MalformedURLException;
import java.net.URL;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(name = "new", usageParameters = "<URL> [resize]")
public class NewCommand extends IoMCommand {

    private ImageUtils.ScalingType resizeMode() throws CommandException {
        switch (args[1]) {
            case "resize":
                return ImageUtils.ScalingType.CONTAINED;
            case "stretch":
            case "stretched":
            case "resize-stretched":
                return ImageUtils.ScalingType.STRETCHED;
            case "cover":
            case "covered":
            case "resize-covered":
                return ImageUtils.ScalingType.COVERED;
            default:
                throwInvalidArgument(I.t("Invalid Stretching mode."));
                return ImageUtils.ScalingType.NONE;
        }
    }

    @Override
    protected void run() throws CommandException {
        final Player player = playerSender();
        ImageUtils.ScalingType scaling = ImageUtils.ScalingType.NONE;
        URL url;
        int width = 0;
        int height = 0;

        if (args.length < 1) {
            throwInvalidArgument(I.t("You must give an URL to take the image from."));
        }
        //Checking if the map limit and image limit
        if (!Permissions.BYPASS_IMAGE_LIMIT.grantedTo(player)) {
            int imageLimit = Permissions.NEW.getLimitPermission(player, Permissions.LimitType.image);
            int imageCount = MapManager.getPlayerMapStore(player.getUniqueId()).getImagesCount();
            if (imageLimit <= imageCount) {
                throwInvalidArgument(
                        I.t("Your image limit is set to {0} and you currently have {1} loaded image(s)",
                                imageLimit,
                                imageCount));
            }
        }
        if (!Permissions.BYPASS_MAP_LIMIT.grantedTo(player)) {
            int mapLimit = Permissions.NEW.getLimitPermission(player, Permissions.LimitType.map);
            int mapCount = MapManager.getPlayerMapStore(player.getUniqueId()).getMapCount();
            if (mapLimit <= mapCount) {
                throwInvalidArgument(
                        I.t("Your map limit is set to {0} and you currently have {1} loaded map(s)",
                                mapLimit,
                                mapCount));
            }
        }
        try {
            url = new URL(args[0]);
            if (!Permissions.BYPASS_WHITELIST.grantedTo(player) && !checkHostnameWhitelist(url)) {
                throwInvalidArgument(I.t("This hosting website is not trusted, if you think that this is an error "
                        + " contact your server administrator"));
                return;
            }

        } catch (MalformedURLException ex) {
            throwInvalidArgument(I.t("Invalid URL."));
            return;
        }
        boolean isPlayer = sender != null;
        // TODO Add a per-player toggle for the GUI.
        if (args.length < 2 && isPlayer) {
            Gui.open(player, new RenderGui(url));
        } else {
            //ImageRendererExecutor.renderAndNotify(url, scaling, player.getUniqueId(), width, height);
            if (args.length >= 2) {
                if (args.length >= 3) {
                    try {
                        if (args.length >= 4) {
                            width = Integer.parseInt(args[2]);
                            height = Integer.parseInt(args[3]);
                        } else {
                            String[] size;
                            if (args[2].contains("*") && !args[2].contains("x")) {
                                size = args[2].split("\\*");
                                width = Integer.parseInt(size[0]);
                                height = Integer.parseInt(size[1]);
                            }
                            if (!args[2].contains("*") && args[2].contains("x")) {
                                size = args[2].split("x");
                                width = Integer.parseInt(size[0]);
                                height = Integer.parseInt(size[1]);
                            }
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        throwInvalidArgument(I.t("resize dimension as to be in format <n m> or <nxm> or <n*m>."));
                        return;
                    }
                }
                scaling = resizeMode();
            }
            if (width < 0 || height < 0) {
                throwInvalidArgument(I.t("You need to specify a valid size. e.g. resize 4 5"));
                return;
            }
            try {
                ActionBar.sendPermanentMessage(player, ChatColor.DARK_GREEN + I.t("Rendering..."));
                ImageRendererExecutor
                        .render(url, scaling, player.getUniqueId(), width, height, new WorkerCallback<ImageMap>() {
                            @Override
                            public void finished(ImageMap result) {
                                ActionBar.removeMessage(player);
                                MessageSender
                                        .sendActionBarMessage(player,
                                                ChatColor.DARK_GREEN + I.t("Rendering finished!"));

                                if (result.give(player)
                                        && (result instanceof PosterMap && !((PosterMap) result).hasColumnData())) {
                                    info(I.t("The rendered map was too big to fit in your inventory."));
                                    info(I.t("Use '/maptool getremaining' to get the remaining maps."));
                                }
                            }

                            @Override
                            public void errored(Throwable exception) {
                                player.sendMessage(I.t("{ce}Map rendering failed: {0}", exception.getMessage()));

                                PluginLogger.warning("Rendering from {0} failed: {1}: {2}",
                                        player.getName(),
                                        exception.getClass().getCanonicalName(),
                                        exception.getMessage());
                            }
                        });
            } finally {
                ActionBar.removeMessage(player);
            }
        }
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return Permissions.NEW.grantedTo(sender);
    }
}
