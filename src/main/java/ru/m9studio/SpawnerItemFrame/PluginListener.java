package ru.m9studio.SpawnerItemFrame;


import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PluginListener implements org.bukkit.event.Listener
{
	List<Group> group = new ArrayList<>();
	boolean isItemsAdder = Bukkit.getServer().getPluginManager().isPluginEnabled("ItemsAdder");
	ItemStack Air = new ItemStack(Material.AIR);
	Random random;
	SpawnerItemFrame main;
	World world;



	public PluginListener(SpawnerItemFrame main)
	{
		super();
		this.main = main;
		world = Bukkit.getWorld("world");
		random = new Random(world.getSeed());
		isItemsAdder = Bukkit.getServer().getPluginManager().isPluginEnabled("ItemsAdder");

		Bukkit.getScheduler().runTaskTimerAsynchronously(main, runnable, 1, 1);
		if(isItemsAdder)
		{
			Bukkit.getPluginManager().registerEvents(new ItemsAdderListener(this), main);
		}
		else
		{
			ConfigUpdate();
		}
	}
	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e)
	{
		if(e.getRightClicked().getType() == EntityType.ITEM_FRAME)
		{
			ItemFrame i = (ItemFrame) e.getRightClicked();
			if(i.getItem().getType() != Material.AIR)
			{
				for (Group g : group)
				{
					if (g.isContainItemFrame(i))
					{
						world.dropItem(i.getLocation(), i.getItem());
						g.items--;
						g.ClickDebris(i);
						e.setCancelled(true);
						break;
					}
				}

			}
		}
	}


	Runnable runnable = () ->
	{
		for (Group g : group)
		{
			g.Tick();
		}
	};
	public void ConfigUpdate()
	{
		FileConfiguration Config = main.GetConfig("config");
		if(Config.isList("file"))
		{
			List<String> list = Config.getStringList("file");
			for (String FileName : list)
			{
				FileConfiguration FileConfig = main.GetConfig(FileName);
				if (FileConfig != null)
				{
					Group g = new Group();
					g.pluginListener = this;
					g.ConfigUpdate(FileName);
					group.add(g);
				}
			}

		}

	}
}
