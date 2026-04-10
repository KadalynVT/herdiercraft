package live.kadalyn.herdiercraft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import live.kadalyn.herdiercraft.Herdiercraft;
import live.kadalyn.herdiercraft.behavior.BuildOnFarmlandBehavior;
import live.kadalyn.herdiercraft.behavior.FlowersIntoPotsBehavior;
import live.kadalyn.herdiercraft.interact.ApplyToSignInteraction;
import live.kadalyn.herdiercraft.interact.BuildItemInteraction;
import live.kadalyn.herdiercraft.interact.WaxNearbyBlockInteraction;
import net.minecraft.world.entity.livingblock.CollisionInteraction;
import net.minecraft.world.entity.livingblock.LivingBlockType;
import net.minecraft.world.entity.livingblock.LivingBlockTypeRegistry;
import net.minecraft.world.entity.livingblock.LivingBlockTypes;
import net.minecraft.world.entity.livingblock.behavior.BuildBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(LivingBlockTypes.class)
public interface LivingBlockTypesMixin {
    @WrapOperation(method = "buildRegistry", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/livingblock/LivingBlockTypeRegistry$Builder;build()Lnet/minecraft/world/entity/livingblock/LivingBlockTypeRegistry;"))
    private static LivingBlockTypeRegistry onBuild(LivingBlockTypeRegistry.Builder builder, Operation<LivingBlockTypeRegistry> original) {
        List<Item> flowers = List.of(
            Items.TORCHFLOWER,
            Items.OAK_SAPLING,
            Items.SPRUCE_SAPLING,
            Items.BIRCH_SAPLING,
            Items.JUNGLE_SAPLING,
            Items.ACACIA_SAPLING,
            Items.CHERRY_SAPLING,
            Items.DARK_OAK_SAPLING,
            Items.PALE_OAK_SAPLING,
            Items.MANGROVE_PROPAGULE,
            Items.FERN,
            Items.DANDELION,
            Items.GOLDEN_DANDELION,
            Items.POPPY,
            Items.BLUE_ORCHID,
            Items.ALLIUM,
            Items.AZURE_BLUET,
            Items.RED_TULIP,
            Items.ORANGE_TULIP,
            Items.WHITE_TULIP,
            Items.PINK_TULIP,
            Items.OXEYE_DAISY,
            Items.CORNFLOWER,
            Items.LILY_OF_THE_VALLEY,
            Items.WITHER_ROSE,
            Items.RED_MUSHROOM,
            Items.BROWN_MUSHROOM,
            Items.DEAD_BUSH,
            Items.CACTUS
        );

        builder.register(
            Items.REDSTONE,
            LivingBlockType.builder()
                .behavior(BuildBehavior.BUILD)
                .apply(LivingBlockTypes.ITEM)
                .onInteract(BuildItemInteraction::new)
        ).register(
            List.of(
                Items.WHEAT_SEEDS,
                Items.COCOA_BEANS,
                Items.PUMPKIN_SEEDS,
                Items.MELON_SEEDS,
                Items.NETHER_WART,
                Items.TORCHFLOWER_SEEDS,
                Items.PITCHER_POD,
                Items.BEETROOT_SEEDS
            ),
            LivingBlockType.builder()
                .behavior(BuildOnFarmlandBehavior.BUILD)
                .apply(LivingBlockTypes.ITEM)
                .onInteract(BuildItemInteraction::new)
        ).register(
            List.of(
                Items.CARROT,
                Items.POTATO
            ),
            LivingBlockType.builder()
                .behavior(BuildOnFarmlandBehavior.BUILD)
                .apply(LivingBlockTypes.ITEM)
        ).register(
            List.of(
                Items.SWEET_BERRIES,
                Items.GLOW_BERRIES
            ),
            LivingBlockType.builder()
                .behavior(BuildBehavior.BUILD)
                .apply(LivingBlockTypes.ITEM)
        ).register(
            Items.HONEYCOMB,
            LivingBlockType.builder()
                .apply(LivingBlockTypes.ITEM)
                .onInteract(WaxNearbyBlockInteraction::new)
        ).register(
            List.of(
                Items.GLOW_INK_SAC,
                Items.WHITE_DYE,
                Items.ORANGE_DYE,
                Items.MAGENTA_DYE,
                Items.LIGHT_BLUE_DYE,
                Items.YELLOW_DYE,
                Items.LIME_DYE,
                Items.PINK_DYE,
                Items.GRAY_DYE,
                Items.LIGHT_GRAY_DYE,
                Items.CYAN_DYE,
                Items.PURPLE_DYE,
                Items.BLUE_DYE,
                Items.BROWN_DYE,
                Items.GREEN_DYE,
                Items.RED_DYE,
                Items.BLACK_DYE
            ),
            LivingBlockType.builder()
                .apply(LivingBlockTypes.ITEM)
                .onInteract(ApplyToSignInteraction::new)
        );

        for (Item flower : flowers) {
            builder.itemLookup.set(
                Item.getId(flower),
                LivingBlockType.builder()
                    // From DEFAULT_NON_COLLIDABLE_BLOCK
                    .apply(LivingBlockTypes.BLOCK)
                    .collision(CollisionInteraction.ENTITY)
                    // New
                    .behavior(FlowersIntoPotsBehavior.placeInPot())
                    .build()
            );
        }

        return original.call(builder);
    }
}
