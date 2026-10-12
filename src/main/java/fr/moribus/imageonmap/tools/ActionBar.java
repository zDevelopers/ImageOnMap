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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * Sends action bar messages, including “permanent” ones re-sent until removed.
 */
public final class ActionBar {
    private static final Map<UUID, String> actionMessages = new ConcurrentHashMap<>();
    private static BukkitTask actionMessagesUpdaterTask = null;

    private ActionBar() {
    }

    /**
     * Sends a message to the action bar of the given player.
     *
     * @param player  The receiver.
     * @param message The message (legacy formatting codes allowed).
     */
    public static void sendMessage(Player player, String message) {
        if (player == null || message == null) {
            return;
        }
        player.sendActionBar(Text.legacy(message));
    }

    /**
     * Sends a message to the action bar of the given player, and keeps it displayed until
     * {@link #removeMessage(Player)} is called.
     *
     * @param player  The receiver.
     * @param message The message (legacy formatting codes allowed).
     */
    public static void sendPermanentMessage(Player player, String message) {
        actionMessages.put(player.getUniqueId(), message);
        sendMessage(player, message);
        checkActionMessageUpdaterRunningState();
    }

    /**
     * Stops re-sending the permanent message of the given player. The message fades out normally.
     *
     * @param player The player.
     */
    public static void removeMessage(Player player) {
        actionMessages.remove(player.getUniqueId());
        checkActionMessageUpdaterRunningState();
    }

    /**
     * Stops re-sending all permanent messages.
     */
    public static void clear() {
        actionMessages.clear();
        if (actionMessagesUpdaterTask != null) {
            actionMessagesUpdaterTask.cancel();
            actionMessagesUpdaterTask = null;
        }
    }

    private static void checkActionMessageUpdaterRunningState() {
        final int messagesCount = actionMessages.size();

        if (messagesCount == 0 && actionMessagesUpdaterTask != null) {
            actionMessagesUpdaterTask.cancel();
            actionMessagesUpdaterTask = null;
        } else if (messagesCount > 0 && actionMessagesUpdaterTask == null) {
            actionMessagesUpdaterTask = RunTask.timer(() -> {
                for (Map.Entry<UUID, String> entry : actionMessages.entrySet()) {
                    final Player player = Bukkit.getPlayer(entry.getKey());
                    if (player != null && player.isOnline()) {
                        sendMessage(player, entry.getValue());
                    }
                }
            }, 20, 30);
        }
    }
}
