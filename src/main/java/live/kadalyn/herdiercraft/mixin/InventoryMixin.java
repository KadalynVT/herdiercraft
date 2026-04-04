package live.kadalyn.herdiercraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public class InventoryMixin {
    @ModifyReturnValue(method = "getSelectionSize", at = @At("RETURN"))
    private static int getSelectionSize(int original) {
        // Basically every use of this just does a -1 after it so...
        return 10;
    }
}
