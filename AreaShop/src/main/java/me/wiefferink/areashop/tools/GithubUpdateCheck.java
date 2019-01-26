package me.wiefferink.areashop.tools;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.logging.Logger;

public class GithubUpdateCheck {

	public static final String API_HOST = "https://api.github.com/repos";
	public static final String API_LATEST_RELEASE = "releases/latest";
	public static final String USER_AGENT = "GithubUpdateCheck by NLThijs48";
	public static final boolean DEBUG = false;

	private final String author;
	private final String repository;
	private final Plugin plugin;
	private final Logger logger;
	private URL url;
	private VersionComparator versionComparator;

	// Status
	private boolean checking;
	private boolean error;
	private boolean hasUpdate;
	private String latestVersion;
	private final String currentVersion;

	/**
	 * Create a new GithubUpdateCheck with the required information.
	 * @param plugin            The plugin to create it for (used for logging and checking version)
	 * @param author            The author of the plugin as used on Github
	 * @param repository        The repository name of the plugin on Github
	 */
	public GithubUpdateCheck(Plugin plugin, String author, String repository) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
		this.author = author;
		this.repository = repository;
		this.currentVersion = plugin.getDescription().getVersion();
		this.versionComparator = (latest, current) ->
				!latest.equalsIgnoreCase(current);

		this.checking = false;
		this.error = false;
		this.hasUpdate = false;
	}

	/**
	 * Change the version comparator.
	 * @param versionComparator VersionComparator to use for checking if one version is newer than the other
	 * @return this
	 */
	public GithubUpdateCheck withVersionComparator(VersionComparator versionComparator) {
		this.versionComparator = versionComparator;
		return this;
	}

	/**
	 * Check if an update is available.
	 */
	public void checkUpdate() {
		checkUpdate(null);
	}

	/**
	 * Check if an update is available.
	 * @param callback Callback to execute when the update check is done
	 * @return GithubUpdateCheck containing the status of the check
	 */
	public GithubUpdateCheck checkUpdate(UpdateCallback callback) {
		checking = true;
		final GithubUpdateCheck self = this;
		// Check for update on asyn thread
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					try {
						String rawUrl = API_HOST + "/" + author + "/" + repository + "/" + API_LATEST_RELEASE;
						url = new URL(rawUrl);
					} catch(MalformedURLException e) {
						logger.severe("Invalid url: '" + url + "', are the author '" + author + "' and repository '" + repository + "' correct?");
						error = true;
						return;
					}

					try {
						URLConnection conn = url.openConnection();
						// Give up after 15 seconds
						conn.setConnectTimeout(15000);
						// Identify ourselves
						conn.addRequestProperty("User-Agent", USER_AGENT);
						// Make sure we access the correct api version
						conn.addRequestProperty("Accept", "application/vnd.github.v3+json");
						// We want to read the result
						conn.setDoOutput(true);
						// Open connection
						try(BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
							String response = reader.readLine();
							debug("Response:", response);

							JSONObject latestRelease = (JSONObject)JSONValue.parse(response);

							if(latestRelease.isEmpty()) {
								logger.warning("Failed to get api response from " + url);
								error = true;
								return;
							}
							debug("json: " + latestRelease.toJSONString());

							// Latest version
							latestVersion = (String)latestRelease.get("tag_name");
							debug("Tag name:", latestVersion);

							// Current version
							debug("Plugin version:", currentVersion);

							// Compare version
							hasUpdate = versionComparator.isNewer(latestVersion, currentVersion);
						}
					} catch(IOException e) {
						logger.severe("Failed to get latest release:" + ExceptionUtils.getStackTrace(e));
						error = true;
					} catch(ClassCastException e) {
						logger.info("Unexpected structure of the result, failed to parse it");
						error = true;
					}
				} finally {
					checking = false;
					debug("result:", self);
					if(callback != null) {
						// Switch back to main thread and call the callback
						new BukkitRunnable() {
							@Override
							public void run() {
								callback.run(self);
							}
						}.runTask(plugin);
					}
				}
			}
		}.runTaskAsynchronously(plugin);
		return this;
	}

	/**
	 * Check if an update check is running.
	 * @return true if an update check is running
	 */
	public boolean isChecking() {
		return checking;
	}

	/**
	 * Check if the update check failed.
	 * @return true if the update check failed (an error message has been logged)
	 */
	public boolean hasFailed() {
		return error;
	}

	/**
	 * Check if an update has been found.
	 * @return true if an update has been found
	 */
	public boolean hasUpdate() {
		return hasUpdate;
	}

	/**
	 * Get the repository that this update checker is checking.
	 * @return Used repository
	 */
	public String getRepository() {
		return repository;
	}

	/**
	 * Get the author that this update checker is checking.
	 * @return Used author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Get the current version.
	 * @return Current version of the plugin
	 */
	public String getCurrentVersion() {
		return currentVersion;
	}

	/**
	 * Get the latest version.
	 * @return Latest version of the plugin (if checking is complete)
	 */
	public String getLatestVersion() {
		return latestVersion;
	}

	public interface VersionComparator {
		/**
		 * Check if a version should be considered 'newer' than another.
		 * @param latestVersion  Version that is available on Github
		 * @param currentVersion Version of the current plugin
		 * @return true if the latestVersion is newer than the localVersion, otherwise false
		 */
		boolean isNewer(String latestVersion, String currentVersion);
	}

	public interface UpdateCallback {
		void run(GithubUpdateCheck result);
	}

	/**
	 * Print a debug message if DEBUG is enabled.
	 * @param message Message to print
	 */
	private void debug(Object... message) {
		if(DEBUG) {
			logger.info("[" + this.getClass().getSimpleName() + "] [DEBUG] " + StringUtils.join(message, " "));
		}
	}

	@Override
	public String toString() {
		return "GithubUpdateCheck(" + StringUtils.join(Arrays.asList(
				"author=" + author,
				"repository=" + repository,
				"plugin=" + plugin.getName(),
				"checking=" + checking,
				"hasUpdate=" + hasUpdate,
				"error=" + error,
				"currentVersion=" + currentVersion,
				"latestVersion=" + latestVersion
		), ", ") + ")";
	}
}
