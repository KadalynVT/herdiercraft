package live.kadalyn.herdiercraft.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow @Final public Options options;
    @Shadow @Final public Gui gui;
    @Shadow public @Nullable LocalPlayer player;
    @Shadow public @Nullable Screen screen;

    @Inject(at = @At("HEAD"), method = "handleKeybinds", cancellable = true)
    private void handleKeybinds(CallbackInfo ci) {
        boolean savePressed = this.options.keySaveHotbarActivator.isDown();
        boolean loadPressed = this.options.keyLoadHotbarActivator.isDown();
        if (this.options.keyHotbarSlots[8].consumeClick()) {
            if (this.player.isSpectator()) {
                this.gui.getSpectatorGui().onHotbarSelected(8);
            } else if (!this.player.hasInfiniteMaterials() || this.screen != null || !loadPressed && !savePressed) {
                this.player.getInventory().setSelectedSlot(8);
            } else {
                CreativeModeInventoryScreen.handleHotbarLoadOrSave((Minecraft)(Object)this, 8, loadPressed, savePressed);
            }
            ci.cancel();
        }
    }
}
