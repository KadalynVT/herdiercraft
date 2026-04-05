package live.kadalyn.herdiercraft.mixin;

import net.minecraft.world.entity.animal.camel.Camel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camel.class)
public class CamelMixin {
    @Inject(method = "openCustomInventoryScreen", at=@At("HEAD"), cancellable = true)
    private void onOpenCustomInventoryScreen(CallbackInfo ci) {
        ci.cancel();
    }
}
