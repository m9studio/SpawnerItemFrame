package ru.m9studio.SpawnerItemFrame;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class Group
{
	PluginListener pluginListener;


	Map<ItemFrame, Integer> Debris = new HashMap<>();
	Stack<ItemFrame> SpawnDebris = new Stack<>();
	ItemStack[] DebrisItem = new ItemStack[0];
	int TimeCoolDownMin = 2;
	int TimeCoolDownPostMin = 3;
	int items = 0;
	int items_max = 0;


	public void SetDebris(ItemFrame i)
	{
		i.setItem(DebrisItem[pluginListener.random.nextInt(DebrisItem.length)]);
	}
	public void ClickDebris(ItemFrame i)
	{
		i.setItem(pluginListener.Air);
		Debris.put(i, TimeCoolDownMin + pluginListener.random.nextInt(TimeCoolDownPostMin));
	}

	public boolean isContainItemFrame(ItemFrame I)
	{
		return Debris.containsKey(I);
	}

	public void Tick()
	{
		if(items < items_max || items_max <= 0)
		{
			for (ItemFrame key : Debris.keySet())
			{
				int v = Debris.get(key) - 1;
				if(v == 0)
				{
					SpawnDebris.push(key);
				}
				Debris.put(key, v);
			}
			while(!SpawnDebris.empty())
			{
				if(items < items_max || items_max <= 0)
				{
					items++;
					ItemFrame  i = SpawnDebris.pop();
					Bukkit.getScheduler().runTask(pluginListener.main, () -> SetDebris(i));
				}
			}
		}
	}
	public void AddDebris(int X, int Y, int Z, BlockFace facing)
	{
		ItemFrame i = null;
		boolean set = true;
		Location l = new Location(pluginListener.world, X, Y, Z);
		for (Entity e : l.getChunk().getEntities())
		{
			if(e.getType() == EntityType.ITEM_FRAME)
			{
				if
				(
						e.getLocation().getBlockX() == X &&
						e.getLocation().getBlockY() == Y &&
						e.getLocation().getBlockZ() == Z
				)
				{
					ItemFrame I = (ItemFrame) e;
					if(e.getFacing() == facing)
					{
						i = I;
						for (Group g : pluginListener.group)
						{
							if(g.isContainItemFrame(I))
							{
								set = false;
								break;
							}
						}
					}
				}
			}
		}
		if(set)
		{
			if(i == null)
			{
				i = pluginListener.world.spawn(l, ItemFrame.class);
				i.setFacingDirection(facing);
			}
			i.setVisible(false);
			i.setFixed(true);
			ClickDebris(i);
		}
	}



	public void ConfigUpdate(String name)
	{
		FileConfiguration Config = pluginListener.main.GetConfig(name);
		Debris = new HashMap<>();
		SpawnDebris.clear();
		if(Config.isConfigurationSection("positions"))
		{
			ConfigurationSection _Positions = Config.getConfigurationSection("positions");
			for(int i = 1; _Positions.isConfigurationSection(i + ""); i++)
			{
				ConfigurationSection _Element = _Positions.getConfigurationSection(i + "");
				if(_Element.isConfigurationSection("position"))
				{
					ConfigurationSection position = _Element.getConfigurationSection("position");
					if(position.isInt("x") && position.isInt("y") && position.isInt("z"))
					{
						int x = position.getInt("x");
						int y = position.getInt("y");
						int z = position.getInt("z");
						BlockFace face = BlockFace.UP;
						if(_Element.isString("facing"))
						{
							try
							{
								face = BlockFace.valueOf(_Element.getString("facing").toLowerCase());
							} catch (Exception ignored) { }
						}
						AddDebris(x, y, z, face);
					}
				}
			}
		}
		if(Config.isConfigurationSection("items"))
		{
			ConfigurationSection _Items = Config.getConfigurationSection("items");


			Map<ItemStack, Integer> ItemsChance = new HashMap<>();
			int AllChance = 0;
			for(int i = 1; _Items.isConfigurationSection(i + ""); i++)
			{
				ConfigurationSection _Element = _Items.getConfigurationSection(i + "");
				int chance = 1;
				ItemStack item = null;
				if(_Element.isInt("chance"))
				{
					chance = _Element.getInt("chance");

					if(chance <= 0)
					{
						continue;
					}
				}
				if(pluginListener.isItemsAdder && _Element.isString("item"))
				{
					CustomStack _item = CustomStack.getInstance(_Element.getString("item"));
					if(_item != null)
					{
						item = _item.getItemStack();
					}
				}

				if(item == null && _Element.isString("type"))
				{
					try
					{
						item = new ItemStack(Material.valueOf(_Element.getString("type")));
					}
					catch (Exception e)
					{
						continue;
					}
				}
				else if(item == null)
				{
					continue;
				}


				AllChance += chance;
				if(ItemsChance.containsKey(item))
				{
					chance += ItemsChance.get(item);
				}
				ItemsChance.put(item, chance);
			}
			DebrisItem = new ItemStack[AllChance];
			int I1 = 0;
			for(Map.Entry<ItemStack, Integer> v: ItemsChance.entrySet())
			{
				int I2 = v.getValue();
				for(;I2 > 0; I2--)
				{
					DebrisItem[I1] = v.getKey();
					I1++;
				}
			}
		}
		if(Config.isConfigurationSection("cooldown"))
		{
			ConfigurationSection _CoolDown = Config.getConfigurationSection("cooldown");
			int _min = 2;
			int _max = 5;
			if(_CoolDown.isInt("min"))
			{
				_min = _CoolDown.getInt("min");
				if(_min < 2)
				{
					_min = 2;
				}
			}
			if(_CoolDown.isInt("max"))
			{
				_max = _CoolDown.getInt("max");
				if(_max < 5)
				{
					_max = 5;
				}
			}
			if(_min > _max)
			{
				int a = _min;
				_min = _max;
				_max = a;
			}
			TimeCoolDownMin = _min;
			TimeCoolDownPostMin = _max - _min;
		}
		items = 0;
		if(Config.isInt("item-max"))
		{
			items_max = Config.getInt("item-max");
		}
		else
		{
			items_max = 0;
		}
	}
}
