package net.codingarea.challenges.plugin.management.menu.generator.implementation.custom;

import net.codingarea.challenges.plugin.challenges.custom.api.ChallengeCondition;
import net.codingarea.challenges.plugin.management.menu.generator.ChooseItemGenerator;
import org.bukkit.entity.Player;

/**
 * @author KxmischesDomi | https://github.com/kxmischesdomi
 * @since 2.1
 */
public class ConditionSubMenuGenerator extends ChooseItemGenerator {

	private final IParentCustomGenerator parent;

	public ConditionSubMenuGenerator(IParentCustomGenerator parent, ChallengeCondition condition) {
		super(condition.getSubSettingsBuilder().getSettings());
		this.parent = parent;
	}

	@Override
	public String[] getSubTitles(int page) {
		return new String[]{ "Create", "Condition" };
	}

	@Override
	public void onItemClick(Player player, String key) {
		parent.accept(player, key);
	}

	@Override
	public void onBackToMenuItemClick(Player player) {
		parent.decline(player);
	}

	public IParentCustomGenerator getParent() {
		return parent;
	}

}
