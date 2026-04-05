package live.kadalyn.herdiercraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import live.kadalyn.herdiercraft.interact.BuildItemInteraction;
import live.kadalyn.herdiercraft.interact.WaxNearbyBlockInteraction;
import net.minecraft.world.entity.livingblock.LivingBlockType;
import net.minecraft.world.entity.livingblock.LivingBlockTypeRegistry;
import net.minecraft.world.entity.livingblock.LivingBlockTypes;
import net.minecraft.world.entity.livingblock.behavior.BuildBehavior;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingBlockTypes.class)
public interface LivingBlockTypesMixin {
    @WrapOperation(method = "buildRegistry", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/livingblock/LivingBlockTypeRegistry$Builder;build()Lnet/minecraft/world/entity/livingblock/LivingBlockTypeRegistry;"))
    private static LivingBlockTypeRegistry onBuild(LivingBlockTypeRegistry.Builder builder, Operation<LivingBlockTypeRegistry> original) {
        builder.register(
            Items.REDSTONE,
            LivingBlockType.builder()
                .behavior(BuildBehavior.BUILD)
                .apply(LivingBlockTypes.ITEM)
                .onInteract(BuildItemInteraction::new)
        ).register(
            Items.HONEYCOMB,
            LivingBlockType.builder()
                .apply(LivingBlockTypes.ITEM)
                .onInteract(WaxNearbyBlockInteraction::new)
        );
        return original.call(builder);
    }
}
