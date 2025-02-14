package net.codingarea.challenges.plugin.management.database;

import net.anweisen.utilities.bukkit.utils.logging.Logger;
import net.anweisen.utilities.common.collection.Tuple;
import net.anweisen.utilities.common.config.Document;
import net.anweisen.utilities.database.Database;
import net.anweisen.utilities.database.DatabaseConfig;
import net.anweisen.utilities.database.SQLColumn;
import net.anweisen.utilities.database.internal.sql.mysql.MySQLDatabase;
import net.anweisen.utilities.database.internal.sql.sqlite.SQLiteDatabase;
import net.codingarea.challenges.plugin.Challenges;
import net.codingarea.challenges.plugin.utils.logging.ConsolePrint;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 2.0
 */
public final class DatabaseManager {

	private final Map<String, Tuple<String, JavaPlugin>> registry = new HashMap<>();
	private String type;
	private Database database;

	{
		// Database types supported by default
		registerDatabase("sqlite", SQLiteDatabase.class, Challenges.getInstance());
		registerDatabase("mysql", MySQLDatabase.class, Challenges.getInstance());
	}

	public void enable() {
		Document document = Challenges.getInstance().getConfigDocument().getDocument("database");

		type = document.getString("type", "none").toLowerCase();
		if ("none".equals(type)) return;

		// Check dependencies
		if ("mongodb".equals(type) && !checkDependencies("com.mongodb.client.MongoClient")) {
			ConsolePrint.noMongoDependencies();
			return;
		}

		try {
			Tuple<String, JavaPlugin> pair = getDatabaseForName(type);
			if (pair == null) {
				Logger.error("Selected illegal database type '{}'", type);
				return;
			}

			JavaPlugin provider = pair.getSecond();
			PluginManager manager = Bukkit.getPluginManager();
			if (!manager.isPluginEnabled(provider) && provider != Challenges.getInstance()) {
				manager.enablePlugin(provider);
			}

			ClassLoader loader = provider.getClass().getClassLoader();

			String className = pair.getFirst();
			@SuppressWarnings("unchecked")
			Class<? extends Database> classOfDatabase = (Class<? extends Database>) loader.loadClass(className);

			DatabaseConfig config = new DatabaseConfig(document.getDocument(type));
			Constructor<? extends Database> constructor = classOfDatabase.getDeclaredConstructor(DatabaseConfig.class);
			database = constructor.newInstance(config);

			connect();
		} catch (ClassNotFoundException ex) {
			Logger.error("Could not find class for database '{}'", type, ex);
		} catch (Throwable ex) {
			Logger.error("Could not create database", ex);
		}
	}

	private void connect() {
		Challenges.getInstance().runAsync(() -> {
			database.connectSafely();
			database.createTableSafely("challenges",
					new SQLColumn("uuid", "varchar", 36),
					new SQLColumn("name", "varchar", 16),
					new SQLColumn("textures", "varchar", 500),
					new SQLColumn("stats", "varchar", 1500),
					new SQLColumn("config", "varchar", 7500)
			);
		});
	}

	public void disconnectIfConnected() {
		if (database != null) {
			database.disconnectSafely();
		}
	}

	@Nullable
	private Tuple<String, JavaPlugin> getDatabaseForName(@Nonnull String type) throws ClassNotFoundException {
		return registry.get(type);
	}

	public void registerDatabase(@Nonnull String name, @Nonnull Class<? extends Database> classOfDatabase, @Nonnull JavaPlugin provider) {
		registry.put(name, new Tuple<>(classOfDatabase.getName(), provider));
	}

	public boolean isConnected() {
		return database != null && database.isConnected();
	}

	public boolean isEnabled() {
		return database != null;
	}

	public Database getDatabase() {
		return database;
	}

	public String getType() {
		return type;
	}

	private boolean checkDependencies(@Nonnull String... classes) {
		try {
			for (String name : classes) {
				Class.forName(name);
			}
		} catch (Throwable ex) {
			return false;
		}
		return true;
	}

}
