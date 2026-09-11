package visualparticle.better.client.mixin;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.InterfaceModule;
import visualparticle.better.client.module.impl.render.CrosshairModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	private void visualparticlebetter$hideVanillaEffects(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		ClientCore.getInstance().modules().find("interface")
				.filter(InterfaceModule.class::isInstance)
				.map(InterfaceModule.class::cast)
				.filter(InterfaceModule::enabled)
				.filter(module -> module.elements().isEnabled("Potions"))
				.ifPresent(module -> ci.cancel());
	}
}