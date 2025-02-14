package net.codingarea.challenges.plugin.challenges.implementation.challenge;

import net.anweisen.utilities.common.annotations.Since;
import net.anweisen.utilities.common.collection.Triple;
import net.anweisen.utilities.common.misc.StringUtils;
import net.codingarea.challenges.plugin.challenges.type.abstraction.Setting;
import net.codingarea.challenges.plugin.content.Message;
import net.codingarea.challenges.plugin.content.Prefix;
import net.codingarea.challenges.plugin.management.challenges.annotations.CanInstaKillOnEnable;
import net.codingarea.challenges.plugin.management.menu.MenuType;
import net.codingarea.challenges.plugin.spigot.events.PlayerInventoryClickEvent;
import net.codingarea.challenges.plugin.spigot.events.PlayerPickupItemEvent;
import net.codingarea.challenges.plugin.utils.item.ItemBuilder;
import net.codingarea.challenges.plugin.utils.misc.NameHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 2.0
 */
@Since("2.0")
@CanInstaKillOnEnable
public class NoDupedItemsChallenge extends Setting {

	public NoDupedItemsChallenge() {
		super(MenuType.CHALLENGES);
	}

	@Nonnull
	@Override
	public ItemBuilder createDisplayItem() {
		return new ItemBuilder(Material.OBSERVER, Message.forName("item-no-duped-items-challenge"));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClick(@Nonnull PlayerInventoryClickEvent event) {
		if (!shouldExecuteEffect()) return;
		if (ignorePlayer(event.getPlayer())) return;
		checkInventories();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityPickUpItem(@Nonnull PlayerPickupItemEvent event) {
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
		if (!shouldExecuteEffect()) return;
		if (ignorePlayer(event.getPlayer())) return;
			checkInventories();
		}, 1);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInteract(@Nonnull PlayerInteractEvent event) {
		if (!shouldExecuteEffect()) return;
		if (ignorePlayer(event.getPlayer())) return;
		if (event.getClickedBlock() == null) return;
		checkInventories();
	}

	private void checkInventories() {
		Map<Player, List<Material>> blackList = new HashMap<>();

		for (Player player : Bukkit.getOnlinePlayers()) {
			Triple<Player, Player, Material> result = checkInventory(player, blackList);
			if (result != null) {
				Message.forName("no-duped-items-failed").broadcast(
						Prefix.CHALLENGES,
						NameHelper.getName(result.getFirst()),
						NameHelper.getName(result.getSecond()),
						StringUtils.getEnumName(result.getThird())
				);
				kill(result.getFirst());
				kill(result.getSecond());
			}

		}

	}

	private Triple<Player, Player, Material> checkInventory(@Nonnull Player player, Map<Player, List<Material>> blacklist) {
		List<Material> localBlacklist = new ArrayList<>();
		List<Material> playerBlacklist = blacklist.getOrDefault(player, new ArrayList<>());
		blacklist.put(player, playerBlacklist);

		for (ItemStack item : player.getInventory().getContents()) {
			if (item == null) continue;
			if (localBlacklist.contains(item.getType())) continue;
			localBlacklist.add(item.getType());

			for (Entry<Player, List<Material>> entry : blacklist.entrySet()) {
				List<Material> currentBlacklist = entry.getValue();
				if (currentBlacklist.contains(item.getType())) {
					return new Triple<>(player, entry.getKey(), item.getType());
				}

			}
			playerBlacklist.add(item.getType());
		}

		return null;
	}

}