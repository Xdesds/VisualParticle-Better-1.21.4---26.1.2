package visualparticle.better.client.mixin;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemCooldowns.class)
public interface ItemCooldownsAccessor {
	@Accessor("cooldowns") Map<Identifier, Object> visualParticleBetter$getCooldowns();
	@Accessor("tickCount") int visualParticleBetter$getTickCount();
}
