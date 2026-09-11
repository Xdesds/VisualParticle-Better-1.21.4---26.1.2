package visualparticle.better.client.mixin;

import visualparticle.better.client.ClientCore;
import visualparticle.better.client.module.impl.render.InterfaceModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Inject(method="onButton",at=@At("HEAD"),cancellable=true)
	private void visualParticleBetter$hudMouse(long window,MouseButtonInfo info,int action,CallbackInfo ci){
		Minecraft mc=Minecraft.getInstance();if(!(mc.screen instanceof ChatScreen))return;
		double x=mc.mouseHandler.xpos()*mc.getWindow().getGuiScaledWidth()/mc.getWindow().getWidth();
		double y=mc.mouseHandler.ypos()*mc.getWindow().getGuiScaledHeight()/mc.getWindow().getHeight();
		ClientCore.getInstance().modules().find("interface").filter(InterfaceModule.class::isInstance).map(InterfaceModule.class::cast).filter(InterfaceModule::enabled).ifPresent(module->{
			if(module.widgets().mouseButton(x,y,info.button(),action))ci.cancel();
		});
	}
}
