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
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * Shortcuts to schedule tasks on the server main thread.
 */
public final class RunTask {
    private RunTask() {
    }

    public static BukkitTask nextTick(Runnable runnable) {
        return Bukkit.getScheduler().runTask(ImageOnMap.getPlugin(), runnable);
    }

    public static BukkitTask later(Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(ImageOnMap.getPlugin(), runnable, delay);
    }

    public static BukkitTask timer(Runnable runnable, long wait, long period) {
        return Bukkit.getScheduler().runTaskTimer(ImageOnMap.getPlugin(), runnable, wait, period);
    }

    public static BukkitTask asyncTimer(Runnable runnable, long wait, long period) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(ImageOnMap.getPlugin(), runnable, wait, period);
    }
}
