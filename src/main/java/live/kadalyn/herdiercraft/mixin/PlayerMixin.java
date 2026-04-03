package live.kadalyn.herdiercraft.mixin;

import live.kadalyn.herdiercraft.item.HerdierItems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ActionItem;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Player.class)
public class PlayerMixin {
	@Accessor("ACTIONS")
	private static List<ActionItem> getActions() {
		return List.of(
			Items.PUNCH_ACTION,
			Items.FOLLOW_ACTION,
			Items.MOVE_ACTION,
			Items.ATTACK_ACTION,
			Items.CRAFTING_ACTION,
			Items.BUILD_ACTION,
			Items.GROUP_ACTION,
			Items.HIGHLIGHT_ACTION,
			HerdierItems.ROTATE_ACTION
		);
	}
}