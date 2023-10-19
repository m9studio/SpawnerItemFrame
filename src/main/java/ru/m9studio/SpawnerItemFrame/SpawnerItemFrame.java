package ru.m9studio.SpawnerItemFrame;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class SpawnerItemFrame extends JavaPlugin
{
    PluginListener listener;
    @Override
    public void onEnable()
    {
        listener = new PluginListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
    }


    FileConfiguration GetConfig(String name)
    {
        File Config = new File(getDataFolder(), name + ".yml");
        if(!Config.exists())
        {
            Config.getParentFile().mkdirs();
            try
            {
                OutputStream out = Files.newOutputStream(Config.toPath());
                InputStream in = getResource(name + ".yml");
                byte[] buf = new byte[4096];
                int len;
                while(true)
                {
                    assert in != null;
                    if (!((len=in.read(buf))>0)) break;
                    out.write(buf,0,len);
                }
                out.close();
                in.close();
            }
            catch (Exception e)
            {
                Config.delete();
                return null;
            }
        }
        return YamlConfiguration.loadConfiguration(Config);
    }
}
