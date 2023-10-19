package ru.m9studio.SpawnerItemFrame;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.Material;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import java.util.Stack;
import org.bukkit.entity.ItemFrame;
import java.util.Map;
import java.util.Random;
import org.bukkit.World;
import org.bukkit.event.Listener;

public class PluginListener implements Listener
{
	boolean isItemsAdder;
	SpawnerItemFrame spawnerItemFrame;
	World world;
	Random random;
	Map<ItemFrame, Integer> Debris;
	Stack<ItemFrame> SpawnDebris;
	ItemStack[] DebrisItem;
	ItemStack Air;
	int TimeCoolDownMin;
	int TimeCoolDownPostMin;
	int items;
	int items_max;
	Runnable runnable;

	public PluginListener(SpawnerItemFrame spawnerItemFrame) {
		isItemsAdder = Bukkit.getServer().getPluginManager().isPluginEnabled("ItemsAdder");
		Debris = new HashMap<>();
		SpawnDebris = new Stack<>();
		DebrisItem = new ItemStack[0];
		Air = new ItemStack(Material.AIR);
		TimeCoolDownMin = 2;
		TimeCoolDownPostMin = 3;
		items = 0;
		items_max = 0;
		this.spawnerItemFrame = spawnerItemFrame;
		runnable = () ->
		{
			if (items < items_max || items_max <= 0) {
				for (ItemFrame key : Debris.keySet()) {
					int v = Debris.get(key) - 1;
					if (v == 0) {
						SpawnDebris.push(key);
					}
					Debris.put(key, v);
				}
				while (!SpawnDebris.empty()) {
					if (items < items_max || items_max <= 0) {
						PluginListener this$0 = PluginListener.this;
						++this$0.items;
						ItemFrame i = SpawnDebris.pop();
						Bukkit.getScheduler().runTask(this.spawnerItemFrame, () -> SetDebris(i));
					}
				}
			}
		};
		world = Bukkit.getWorld("world");
		random = new Random(world.getSeed());
		Bukkit.getScheduler().runTaskTimerAsynchronously(spawnerItemFrame, runnable, 1L, 1L);
		if (isItemsAdder) {
			Bukkit.getPluginManager().registerEvents(new ItemsAdderListener(this), spawnerItemFrame);
		}
		else {
			ConfigUpdate();
		}
	}

	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
		if (e.getRightClicked().getType() == EntityType.ITEM_FRAME) {
			ItemFrame i = (ItemFrame)e.getRightClicked();
			if (Debris.containsKey(i)) {
				if (i.getItem().getType() != Material.AIR) {
					items--;
					world.dropItem(i.getLocation(), i.getItem());
					ClickDebris(i);
				}
				e.setCancelled(true);
			}
		}
	}

	void SetDebris(ItemFrame i) {
		i.setItem(DebrisItem[random.nextInt(DebrisItem.length)]);
	}

	void ClickDebris(ItemFrame i) {
		i.setItem(Air);
		Debris.put(i, TimeCoolDownMin + random.nextInt(TimeCoolDownPostMin));
	}

	void AddDebris(int X, int Y, int Z, BlockFace facing) {
		ItemFrame i = null;
		Location l = new Location(world, X, Y, Z);
		for (Entity e : l.getChunk().getEntities()) {
			if (e.getType() == EntityType.ITEM_FRAME && e.getLocation().getBlockX() == X && e.getLocation().getBlockY() == Y && e.getLocation().getBlockZ() == Z) {
				ItemFrame I = (ItemFrame)e;
				if (e.getFacing() == facing) {
					i = I;
					break;
				}
			}
		}
		if (i == null) {
			i = world.spawn(l, ItemFrame.class);
			i.setFacingDirection(facing);
		}
		i.setVisible(false);
		i.setFixed(true);
		ClickDebris(i);
	}

	void ConfigUpdate() {
		FileConfiguration Config = spawnerItemFrame.GetConfig();
		Debris = new HashMap<>();
		SpawnDebris.clear();
		if (Config.isConfigurationSection("positions")) {
			ConfigurationSection _Positions = Config.getConfigurationSection("positions");
			for (int i = 1; _Positions.isConfigurationSection(i + ""); ++i) {
				ConfigurationSection _Element = _Positions.getConfigurationSection(i + "");
				if (_Element.isConfigurationSection("position")) {
					ConfigurationSection position = _Element.getConfigurationSection("position");
					if (position.isInt("x") && position.isInt("y") && position.isInt("z")) {
						int x = position.getInt("x");
						int y = position.getInt("y");
						int z = position.getInt("z");
						BlockFace face = BlockFace.UP;
						if (_Element.isString("facing")) {
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
		if (Config.isConfigurationSection("items")) {
			ConfigurationSection _Items = Config.getConfigurationSection("items");
			Map<ItemStack, Integer> ItemsChance = new HashMap<>();
			int AllChance = 0;
			for (int j = 1; _Items.isConfigurationSection(j + ""); ++j) {
				ConfigurationSection _Element2 = _Items.getConfigurationSection(j + "");
				int chance = 1;
				ItemStack item = null;
				if (_Element2.isInt("chance")) {
					chance = _Element2.getInt("chance");
					if (chance <= 0) {
						continue;
					}
				}
				if (isItemsAdder && _Element2.isString("item")) {
					CustomStack _item = CustomStack.getInstance(_Element2.getString("item"));
					if (_item != null) {
						item = _item.getItemStack();
					}
				}
				Label_0509: {
					if (item == null && _Element2.isString("type")) {
						try {
							item = new ItemStack(Material.valueOf(_Element2.getString("type")));
							break Label_0509;
						}
						catch (Exception e) {
							continue;
						}
					}
					if (item == null) {
						continue;
					}
				}
				AllChance += chance;
				if (ItemsChance.containsKey(item)) {
					chance += ItemsChance.get(item);
				}
				ItemsChance.put(item, chance);
			}
			DebrisItem = new ItemStack[AllChance];
			int I1 = 0;
			for (Map.Entry<ItemStack, Integer> v : ItemsChance.entrySet()) {
				for (int I2 = v.getValue(); I2 > 0; --I2) {
					DebrisItem[I1] = v.getKey();
					++I1;
				}
			}
		}
		if (Config.isConfigurationSection("cooldown")) {
			ConfigurationSection _CoolDown = Config.getConfigurationSection("cooldown");
			int _min = 2;
			int _max = 5;
			if (_CoolDown.isInt("min")) {
				_min = _CoolDown.getInt("min");
				if (_min < 2) {
					_min = 2;
				}
			}
			if (_CoolDown.isInt("max")) {
				_max = _CoolDown.getInt("max");
				if (_max < 5) {
					_max = 5;
				}
			}
			if (_min > _max) {
				int a = _min;
				_min = _max;
				_max = a;
			}
			TimeCoolDownMin = _min;
			TimeCoolDownPostMin = _max - _min;
		}
		items = 0;
		if (Config.isInt("item-max")) {
			items_max = Config.getInt("item-max");
		}
		else {
			items_max = 0;
		}
	}
}
