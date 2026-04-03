package live.kadalyn.herdiercraft.mixin.client;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class ExtractItemHotbarMixin {
	@Shadow @Final private static Identifier HOTBAR_SPRITE;
	@Shadow @Final private static Identifier HOTBAR_SELECTION_SPRITE;
	@Shadow @Final private static Identifier HOTBAR_OFFHAND_LEFT_SPRITE;
	@Shadow @Final private static Identifier HOTBAR_OFFHAND_RIGHT_SPRITE;
	@Shadow @Final private static Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE;
	@Shadow @Final private static Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE;
	@Shadow @Final private Minecraft minecraft;
	@Shadow protected abstract @Nullable Player getCameraPlayer();
	@Shadow protected abstract void extractSlot(final GuiGraphicsExtractor graphics, final int x, final int y, final DeltaTracker deltaTracker, final Player player, final ItemStack itemStack, final int seed);

	@Inject(at = @At("HEAD"), method = "extractItemHotbar", cancellable = true)
	private void extractItemHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Player player = this.getCameraPlayer();
		if (player != null) {
			ItemStack offhand = player.getOffhandItem();
			HumanoidArm offhandArm = player.getMainArm().getOpposite();
			int screenCenter = graphics.guiWidth() / 2;
			int hotbarWidth = 182;
			int halfHotbar = 91;
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, screenCenter - 91, graphics.guiHeight() - 22, 182, 22);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, screenCenter - 91 - 1 + player.getInventory().getSelectedSlot() * 20, graphics.guiHeight() - 22 - 1, 24, 23);
			if (!offhand.isEmpty()) {
				if (offhandArm == HumanoidArm.LEFT) {
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, screenCenter - 91 - 29, graphics.guiHeight() - 23, 29, 24);
				} else {
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_SPRITE, screenCenter + 91, graphics.guiHeight() - 23, 29, 24);
				}
			}

			int seed = 1;

			for(int i = 0; i < 9; ++i) {
				int x = screenCenter - 90 + i * 20 + 2;
				int y = graphics.guiHeight() - 16 - 3;
				this.extractSlot(graphics, x, y, deltaTracker, player, player.getInventory().getItem(i), seed++);
			}

			if (!offhand.isEmpty()) {
				int y = graphics.guiHeight() - 16 - 3;
				if (offhandArm == HumanoidArm.LEFT) {
					this.extractSlot(graphics, screenCenter - 91 - 26, y, deltaTracker, player, offhand, seed++);
				} else {
					this.extractSlot(graphics, screenCenter + 91 + 10, y, deltaTracker, player, offhand, seed++);
				}
			}

			if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
				float attackStrengthScale = this.minecraft.player.getAttackStrengthScale(0.0F);
				if (attackStrengthScale < 1.0F) {
					int y = graphics.guiHeight() - 20;
					int x = screenCenter + 91 + 6;
					if (offhandArm == HumanoidArm.RIGHT) {
						x = screenCenter - 91 - 22;
					}

					int progress = (int)(attackStrengthScale * 19.0F);
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 18, 18);
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - progress, x, y + 18 - progress, 18, progress);
				}
			}
		}
		ci.cancel();
	}
}