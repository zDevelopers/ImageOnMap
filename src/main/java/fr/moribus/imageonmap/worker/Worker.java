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

package fr.moribus.imageonmap.worker;

import fr.moribus.imageonmap.ImageOnMap;
import fr.moribus.imageonmap.tools.PluginLogger;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;

/**
 * A worker executes queries one after the other on its own thread, and calls back on the server main thread.
 */
public abstract class Worker {
    private static final Map<Class<? extends Worker>, Worker> runningWorkers = new ConcurrentHashMap<>();

    private final String name;
    private ExecutorService executor;

    protected Worker(String name) {
        this.name = name;
    }

    /**
     * Starts the worker of the given class.
     *
     * @param workerClass The worker class. It must have a public no-argument constructor.
     */
    public static void start(Class<? extends Worker> workerClass) {
        final Worker worker;
        try {
            worker = workerClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate worker " + workerClass.getName(), e);
        }

        final Worker previous = runningWorkers.put(workerClass, worker);
        if (previous != null) {
            PluginLogger.warning("Restarting thread '{0}'.", previous.getName());
            previous.shutdown(0);
        }

        worker.executor = Executors.newSingleThreadExecutor(runnable -> {
            final Thread thread = new Thread(runnable, worker.getName());
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Stops the worker of the given class.
     *
     * @param workerClass The worker class.
     * @param timeoutMs   The time to wait for the queued queries to finish, in milliseconds. If zero, the queued
     *                    queries are dropped and the running one is interrupted.
     */
    public static void stop(Class<? extends Worker> workerClass, long timeoutMs) {
        final Worker worker = runningWorkers.remove(workerClass);
        if (worker != null) {
            worker.shutdown(timeoutMs);
        }
    }

    /**
     * Submits a query to the given worker, ignoring its result.
     *
     * @param workerClass The worker that will execute the query.
     * @param runnable    The query.
     */
    protected static void submitQuery(Class<? extends Worker> workerClass, WorkerRunnable<?> runnable) {
        submitQuery(workerClass, runnable, null);
    }

    /**
     * Submits a query to the given worker.
     *
     * @param workerClass The worker that will execute the query.
     * @param runnable    The query.
     * @param callback    The callback called on the main thread with the result (can be {@code null}).
     * @param <T>         The type of the result.
     */
    protected static <T> void submitQuery(Class<? extends Worker> workerClass, WorkerRunnable<T> runnable,
                                          WorkerCallback<T> callback) {
        final Worker worker = runningWorkers.get(workerClass);
        if (worker == null) {
            throw new IllegalStateException(
                    "Worker '" + workerClass.getName() + "' has not been correctly initialized");
        }

        try {
            worker.executor.execute(() -> {
                T result = null;
                Throwable exception = null;

                try {
                    result = runnable.run();
                } catch (Throwable ex) {
                    exception = ex;
                }

                if (callback != null) {
                    callBack(callback, result, exception);
                }
            });
        } catch (RejectedExecutionException e) {
            PluginLogger.warning("Worker '{0}' is stopped; query ignored.", worker.getName());
        }
    }

    /**
     * Executes a task on the server main thread. Must be called from a worker thread.
     *
     * @param callable The task.
     * @param <T>      The type of the result.
     * @return A future to retrieve the result of the task.
     */
    protected static <T> Future<T> submitToMainThread(Callable<T> callable) {
        return Bukkit.getScheduler().callSyncMethod(ImageOnMap.getPlugin(), callable);
    }

    private static <T> void callBack(WorkerCallback<T> callback, T result, Throwable exception) {
        final ImageOnMap plugin = ImageOnMap.getPlugin();
        if (plugin == null || !plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (exception != null) {
                callback.errored(exception);
            } else {
                callback.finished(result);
            }
        });
    }

    private void shutdown(long timeoutMs) {
        if (executor == null) {
            return;
        }

        if (timeoutMs <= 0) {
            executor.shutdownNow();
            return;
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)) {
                PluginLogger.warning("Thread '{0}' did not finish its queued tasks in time.", getName());
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String getName() {
        final ImageOnMap plugin = ImageOnMap.getPlugin();
        return (plugin != null ? plugin.getName() : "ImageOnMap") + "-" + name;
    }
}
