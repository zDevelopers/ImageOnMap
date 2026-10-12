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

package fr.moribus.imageonmap.tools;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.i18n.I;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

/**
 * Checks SpigotMC for new versions of the plugin, and notifies the console and operators.
 */
public final class UpdateChecker implements Listener {
    private static final Set<UUID> notificationSentTo = new HashSet<>();
    private static URI resourceUri;
    private static String newVersion = null;
    private static BukkitTask checkTask = null;

    private UpdateChecker() {
    }

    /**
     * Starts checking for updates.
     *
     * @param spigotIdentifier The SpigotMC resource identifier (e.g. {@code imageonmap.26585}).
     */
    public static void boot(final String spigotIdentifier) {
        final String[] identifierParts = spigotIdentifier.split("\\.");
        final int resourceId = Integer.parseInt(identifierParts[identifierParts.length - 1]);
        final URI checkUri;

        try {
            resourceUri = URI.create("https://www.spigotmc.org/resources/" + spigotIdentifier);
            checkUri = URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId);
        } catch (IllegalArgumentException e) {
            PluginLogger.error("Unable to boot update checker: invalid resource URI", e);
            return;
        }

        checkTask = RunTask.asyncTimer(() -> {
            try (InputStream inputStream = checkUri.toURL().openStream();
                    Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    final String version = scanner.next().trim();
                    if (!version.equalsIgnoreCase(getCurrentVersion())) {
                        newVersion = version;
                        RunTask.nextTick(() -> {
                            notifyConsole(version);
                            for (final Player player : Bukkit.getOnlinePlayers()) {
                                if (player.isOp()) {
                                    notifyPlayer(version, player);
                                    notificationSentTo.add(player.getUniqueId());
                                }
                            }
                            Bukkit.getPluginManager().registerEvents(new UpdateChecker(), ImageOnMap.getPlugin());
                        });
                        if (checkTask != null) {
                            checkTask.cancel();
                            checkTask = null;
                        }
                    }
                }
            } catch (IOException ignored) {
                // The update check is best-effort.
            }
        }, 40L, 20 * 3600 * 2L);
    }

    private static String getCurrentVersion() {
        return ImageOnMap.getPlugin().getPluginMeta().getVersion().trim();
    }

    private static void notifyConsole(final String version) {
        PluginLogger.warning("A new version of " + ImageOnMap.getPlugin().getPluginMeta().getName()
                + " is available! Latest version is " + version + ", and you're running " + getCurrentVersion());
        PluginLogger.warning("Download the new version here: " + resourceUri);
    }

    private static void notifyPlayer(final String version, final Player player) {
        final Component hover = Component.text()
                .append(Component.text(I.t("Click here to open")))
                .append(Component.text("\n"))
                .append(Component.text(resourceUri.toString().replaceFirst("https://", "").replaceFirst("www\\.", ""),
                        NamedTextColor.GRAY))
                .build();

        player.sendMessage(Component.empty());
        player.sendMessage(Component.text()
                .clickEvent(ClickEvent.openUrl(resourceUri.toString()))
                .hoverEvent(HoverEvent.showText(hover))
                .append(Component.text("✦ ", NamedTextColor.GOLD))
                .append(Component.text(
                        I.t("{0} {1} is available!", ImageOnMap.getPlugin().getPluginMeta().getName(), version),
                        NamedTextColor.GOLD, TextDecoration.BOLD))
                .build());
        player.sendMessage(Component.text(I.t("You're still running {0}. Click here to update.", getCurrentVersion()),
                        NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.openUrl(resourceUri.toString()))
                .hoverEvent(HoverEvent.showText(hover)));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent ev) {
        final Player player = ev.getPlayer();
        if (newVersion != null && !notificationSentTo.contains(player.getUniqueId()) && player.isOp()) {
            RunTask.later(() -> {
                if (player.isOnline()) {
                    notifyPlayer(newVersion, player);
                    notificationSentTo.add(player.getUniqueId());
                }
            }, 160L);
        }
    }
}
