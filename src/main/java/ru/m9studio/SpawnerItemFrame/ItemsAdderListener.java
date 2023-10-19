package ru.m9studio.SpawnerItemFrame;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;

public class ItemsAdderListener implements org.bukkit.event.Listener
{
	ru.m9studio.SpawnerItemFrame.PluginListener listener;
	public ItemsAdderListener(ru.m9studio.SpawnerItemFrame.PluginListener listener)
	{
		super();
		this.listener = listener;
	}
	@EventHandler
	public void onLoad(ItemsAdderLoadDataEvent e)
	{
		listener.ConfigUpdate();
	}
}
