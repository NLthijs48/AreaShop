package me.wiefferink.areashop.tools;

import me.wiefferink.areashop.AreaShop;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;

public class Task {

	// No access
	private Task() {}

	/**
	 * Run a task on the main server thread.
	 * @param runnable The BukkitRunnable to run
	 */
	public static void sync(Run runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTask(AreaShop.getInstance());
	}

	/**
	 * Run a task on the main server thread.
	 * @param runnable The BukkitRunnable to run
	 * @param delay Ticks to wait before running the task
	 */
	public static void syncLater(long delay, Run runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskLater(AreaShop.getInstance(), delay);
	}

	/**
	 * Run a task on an asynchronous thread.
	 * @param runnable The BukkitRunnable to run
	 */
	public static void async(Run runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskAsynchronously(AreaShop.getInstance());
	}

	/**
	 * Run a task on an asynchronous thread.
	 * @param runnable The BukkitRunnable to run
	 * @param delay    Ticks to wait before running the task
	 */
	public static void asyncLater(long delay, Run runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskLaterAsynchronously(AreaShop.getInstance(), delay);
	}

	/**
	 * Run a timer task on the main server thread.
	 * @param runnable The BukkitRunnable to run
	 * @param period Time between task runs
	 */
	public static void syncTimer(long period, Run runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskTimer(AreaShop.getInstance(), 0, period);
	}

	/**
	 * Run a timer task on the main server thread.
	 * @param runnable The BukkitRunnable to run
	 * @param period Time between task runs
	 */
	public static void syncTimer(long period, RunResult<Boolean> runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(!runnable.run()) {
					this.cancel();
				}
			}
		}.runTaskTimer(AreaShop.getInstance(), 0, period);
	}

	/**
	 * Run a timer task on an asynchronous thread.
	 * @param runnable The BukkitRunnable to run
	 * @param period Time between task runs
	 */
	public static void asyncTimer(long period, Run runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				runnable.run();
			}
		}.runTaskTimerAsynchronously(AreaShop.getInstance(), 0, period);
	}

	/**
	 * Run a timer task on an asynchronous thread.
	 * @param runnable The BukkitRunnable to run
	 * @param period Time between task runs
	 */
	public static void asyncTimer(long period, RunResult<Boolean> runnable) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(!runnable.run()) {
					this.cancel();
				}
			}
		}.runTaskTimerAsynchronously(AreaShop.getInstance(), 0, period);
	}

	/**
	 * Perform an action for each given object.
	 * @param perTick Number of objects to process per tick
	 * @param objects Objects to process
	 * @param runArgument Function to execute for each object
	 * @param <T> Type of object to process
	 */
	public static <T> void doForAll(int perTick, Collection<T> objects, RunArgument<T> runArgument) {
		final ArrayList<T> finalObjects = new ArrayList<>(objects);
		new BukkitRunnable() {
			private int current = 0;

			@Override
			public void run() {
				for(int i = 0; i < perTick; i++) {
					if(current < finalObjects.size()) {
						runArgument.run(finalObjects.get(current));
						current++;
					}
				}
				if(current >= finalObjects.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(AreaShop.getInstance(), 1, 1);
	}


	public interface Run {
		void run();
	}

	public interface RunResult<T> {
		T run();
	}

	public interface RunArgument<T> {
		void run(T argument);
	}
}
