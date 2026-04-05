package live.kadalyn.herdiercraft.mixin;

import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractNautilus.class)
public class AbstractNautilusMixin {
    @Inject(method = "openCustomInventoryScreen", at=@At("HEAD"), cancellable = true)
    private void onOpenCustomInventoryScreen(CallbackInfo ci) {
        ci.cancel();
    }
}
