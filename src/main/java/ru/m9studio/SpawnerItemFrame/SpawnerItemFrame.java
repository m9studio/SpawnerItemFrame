package ru.m9studio.SpawnerItemFrame;

import java.io.InputStream;
import java.io.OutputStream;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.nio.file.Files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnerItemFrame extends JavaPlugin
{
	ru.m9studio.SpawnerItemFrame.PluginListener listener;

	public void onEnable() {
		listener = new ru.m9studio.SpawnerItemFrame.PluginListener(this);
		Bukkit.getPluginManager().registerEvents(listener, this);
	}

	FileConfiguration GetConfig() {
		File Config = new File(getDataFolder(), "config.yml");
		if (!Config.exists()) {
			Config.getParentFile().mkdirs();
			try {
				OutputStream out = Files.newOutputStream(Config.toPath());
				InputStream in = getResource("config.yml");
				byte[] buf = new byte[1024];
				int len;
				while (true) {
					assert in != null;
					if (!((len = in.read(buf)) > 0)) break;
					out.write(buf, 0, len);
				}
				out.close();
				in.close();
			}
			catch (Exception ignored) {}
		}
		return YamlConfiguration.loadConfiguration(Config);
	}
}
