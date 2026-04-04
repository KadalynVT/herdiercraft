package live.kadalyn.herdiercraft.mixin;

import com.mojang.authlib.GameProfile;
import live.kadalyn.herdiercraft.item.HerdierItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	void addAction(Level level, GameProfile gameProfile, CallbackInfo ci) {
		((Player)(Object) this).getInventory().add(HerdierItems.ROTATE_ACTION.getDefaultInstance());
	}
}