package live.kadalyn.herdiercraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractChestedHorse.class)
public class AbstractChestedHorseMixin {
    @WrapOperation(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
    private boolean allowPunchAction(ItemStack itemStack, Operation<Boolean> original) {
        return itemStack.is(Items.PUNCH_ACTION) || original.call(itemStack);
    }
}
