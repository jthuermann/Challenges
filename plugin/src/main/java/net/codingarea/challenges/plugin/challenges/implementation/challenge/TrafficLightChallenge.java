package net.codingarea.challenges.plugin.challenges.implementation.challenge;

import net.anweisen.utilities.commons.annotations.Since;
import net.codingarea.challenges.plugin.challenges.type.TimedChallenge;
import net.codingarea.challenges.plugin.challenges.type.helper.ChallengeHelper;
import net.codingarea.challenges.plugin.language.Message;
import net.codingarea.challenges.plugin.language.Prefix;
import net.codingarea.challenges.plugin.management.menu.MenuType;
import net.codingarea.challenges.plugin.utils.animation.SoundSample;
import net.codingarea.challenges.plugin.utils.item.ItemBuilder;
import net.codingarea.challenges.plugin.utils.misc.BlockUtils;
import net.codingarea.challenges.plugin.utils.misc.NameHelper;
import net.codingarea.challenges.plugin.utils.misc.RandomizeUtils;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 2.0
 */
@Since("1.3")
public class TrafficLightChallenge extends TimedChallenge {

	private static final int GREEN = 0, YELLOW = 1, RED = 2;

	private final Random random = new Random();
	private int state;

	public TrafficLightChallenge() {
		super(MenuType.CHALLENGES, 1, 10, 5);
	}

	@Nonnull
	@Override
	public ItemBuilder createDisplayItem() {
		return new ItemBuilder(Material.LIME_STAINED_GLASS, Message.forName("item-traffic-light-challenge"));
	}

	@Nullable
	@Override
	protected String[] getSettingsDescription() {
		return Message.forName("item-time-seconds-range-description").asArray(getValue() * 60 - 20, getValue() * 60 + 20);
	}

	@Override
	public void playValueChangeTitle() {
		ChallengeHelper.playChallengeSecondsRangeValueChangeTitle(this, getValue() * 60 - 20, getValue() * 60 + 20);
	}

	@Override
	public void onEnable() {
		bossbar.setContent((bossbar, player) -> {
			switch (state) {
				case GREEN:
					bossbar.setColor(BarColor.GREEN);
					bossbar.setTitle("§8{ §a§l■■■ §8} §8{ §7§l■■■ §8} §8{ §7§l■■■ §8}");
					break;
				case YELLOW:
					bossbar.setColor(BarColor.YELLOW);
					bossbar.setTitle("§8{ §7§l■■■ §8} §8{ §e§l■■■ §8} §8{ §7§l■■■ §8}");
					break;
				case RED:
					bossbar.setColor(BarColor.RED);
					bossbar.setTitle("§8{ §7§l■■■ §8} §8{ §7§l■■■ §8} §8{ §c§l■■■ §8}");
					break;
			}
		});
		bossbar.show();
	}

	@Override
	public void onDisable() {
		bossbar.hide();
	}

	@Override
	protected int getSecondsUntilNextActivation() {
		return RandomizeUtils.randomAround(random, getValue() * 60, 20);
	}

	@Override
	protected void onTimeActivation() {
		switch (state) {
			case GREEN:
				state = YELLOW;
				restartTimer(2);
				SoundSample.BASS_OFF.broadcast();
				bossbar.update();
				break;
			case YELLOW:
				state = RED;
				restartTimer(3);
				SoundSample.BASS_OFF.broadcast();
				bossbar.update();
				break;
			case RED:
				state = GREEN;
				restartTimer();
				SoundSample.BASS_ON.broadcast();
				bossbar.update();
				break;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(@Nonnull PlayerMoveEvent event) {
		if (!shouldExecuteEffect()) return;
		if (ignorePlayer(event.getPlayer())) return;
		if (state != RED) return;
		if (event.getTo() == null) return;
		if (BlockUtils.isSameLocation(event.getFrom(), event.getTo())) return;

		state = GREEN;
		bossbar.update();
		restartTimer();

		Player player = event.getPlayer();
		Message.forName("traffic-light-challenge-fail").broadcast(Prefix.CHALLENGES, NameHelper.getName(player));
		player.damage(player.getMaxHealth());
	}

}
