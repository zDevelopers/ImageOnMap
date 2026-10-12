/*
 * Copyright or © or Copr. Moribus (2013)
 * Copyright or © or Copr. ProkopyL <prokopylmc@gmail.com> (2015)
 * Copyright or © or Copr. Amaury Carrade <amaury@carrade.eu> (2016 – 2021)
 * Copyright or © or Copr. Vlammar <valentin.jabre@gmail.com> (2019 – 2021)
 *
 * Parts of this file are adapted from QuartzLib (https://github.com/zDevelopers/QuartzLib),
 * Copyright or © or Copr. ZDevelopers (2015 – 2021), governed by the CeCILL-B license.
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

package fr.moribus.imageonmap.gui.framework;

import fr.moribus.imageonmap.tools.RunTask;
import io.papermc.paper.event.packet.UncheckedSignChangeEvent;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A GUI asking the player for a text, using a sign editor.
 *
 * <p>The sign only exists client-side (it is never placed in the world): it is displayed a few blocks below the
 * player while they edit it, and removed afterwards.</p>
 */
public class PromptGui extends GuiBase {
    private static final int SIGN_LINES_COUNT = 4;
    private static final int SIGN_COLUMNS_COUNT = 15;

    /**
     * How many blocks below the player the virtual sign is displayed. The client only allows editing signs close to
     * the player.
     */
    private static final int SIGN_DEPTH = 3;

    private final Consumer<String> callback;
    private final String contents;
    private Location signLocation;

    public PromptGui(Consumer<String> callback, String contents) {
        this.callback = callback;
        this.contents = contents;
    }

    /**
     * Prompts a player for a text.
     *
     * @param owner    The player.
     * @param callback Called with the text entered by the player.
     * @param contents The text initially displayed.
     * @param parent   The parent GUI, re-opened after the prompt (can be {@code null}).
     */
    public static void prompt(Player owner, Consumer<String> callback, String contents, GuiBase parent) {
        Gui.open(owner, new PromptGui(callback, contents), parent);
    }

    private static String getSignContents(List<Component> lines) {
        final StringBuilder content = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            final String line = PlainTextComponentSerializer.plainText().serialize(lines.get(i));
            if (i == 0) {
                content.append(line.trim());
            } else if (!line.isEmpty()) {
                content.append(" ").append(line.trim());
            }
        }

        return content.toString().trim();
    }

    private static String[] splitSignContents(String content) {
        final String[] lines = new String[SIGN_LINES_COUNT + 1];
        int curLineIndex = 0;

        if (content != null) {
            lines[0] = content;

            while (curLineIndex < SIGN_LINES_COUNT) {
                final String curLine = lines[curLineIndex];
                if (curLine.length() <= SIGN_COLUMNS_COUNT) {
                    break;
                }

                final int spacePos = curLine.lastIndexOf(' ', SIGN_COLUMNS_COUNT);
                if (spacePos < 0) {
                    break;
                }

                lines[curLineIndex + 1] = curLine.substring(spacePos + 1);
                lines[curLineIndex] = curLine.substring(0, spacePos);
                curLineIndex++;
            }
        }

        return lines;
    }

    private static Location findAvailableLocation(Player player) {
        final Location location = player.getLocation().getBlock().getLocation();
        location.setY(Math.max(location.getBlockY() - SIGN_DEPTH, player.getWorld().getMinHeight()));
        return location;
    }

    @Override
    protected void open(final Player player) {
        super.open(player);

        signLocation = findAvailableLocation(player);

        final BlockData signData = Material.OAK_SIGN.createBlockData();
        final Sign sign = (Sign) signData.createBlockState();
        final SignSide side = sign.getSide(Side.FRONT);
        final String[] lines = splitSignContents(contents);

        for (int i = 0; i < SIGN_LINES_COUNT; i++) {
            side.line(i, Component.text(lines[i] != null ? lines[i] : ""));
        }

        player.sendBlockChange(signLocation, signData);
        player.sendBlockUpdate(signLocation, sign);

        RunTask.later(() -> {
            if (isOpen() && player.isOnline()) {
                player.openVirtualSign(Position.block(signLocation), Side.FRONT);
            }
        }, 3);
    }

    @Override
    protected void onClose() {
        final Player player = getPlayer();

        if (signLocation != null && player != null && player.isOnline()
                && player.getWorld().equals(signLocation.getWorld())) {
            final Block block = signLocation.getBlock();
            player.sendBlockChange(signLocation, block.getBlockData());

            final BlockState state = block.getState();
            if (state instanceof TileState) {
                player.sendBlockUpdate(signLocation, (TileState) state);
            }
        }

        super.onClose();
    }

    private void validate(List<Component> lines) {
        callback.accept(getSignContents(lines));
        this.close(true);
    }

    @Override
    protected Listener getEventListener() {
        return new PromptGuiListener();
    }

    private final class PromptGuiListener implements Listener {
        @EventHandler
        public void onSignChange(UncheckedSignChangeEvent event) {
            if (event.getPlayer() != getPlayer() || signLocation == null) {
                return;
            }

            final BlockPosition position = event.getEditedBlockPosition();
            if (position.blockX() != signLocation.getBlockX() || position.blockY() != signLocation.getBlockY()
                    || position.blockZ() != signLocation.getBlockZ()) {
                return;
            }

            event.setCancelled(true);

            final List<Component> lines = event.lines();
            RunTask.nextTick(() -> {
                if (isOpen()) {
                    validate(lines);
                }
            });
        }
    }
}
