package live.kadalyn.herdiercraft.item;

import live.kadalyn.herdiercraft.Herdiercraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ActionItem;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class HerdierItems {
    public static final ActionItem ROTATE_ACTION = register("rotate_action", RotateAction::new,
        (new Item.Properties()).stacksTo(1).attributes(RotateAction.createAttributes()));

    public static <T extends Item> T register(String name, Function<Item.Properties, T> itemFactory, Item.Properties settings) {
        // Create the item key.
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Herdiercraft.MOD_ID, name));

        // Create the item instance.
        T item = itemFactory.apply(settings.setId(itemKey));

        // Register the item.
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);

        return item;
    }

    public static void initialize() {
    }
}
