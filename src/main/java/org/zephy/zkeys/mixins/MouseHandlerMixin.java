package org.zephy.zkeys.mixins;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zephy.zkeys.ZKeys;

//#if MC<26.2
//$$import org.objectweb.asm.Opcodes;
//#endif

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(
        method = "onButton",
        at = @At(
            //#if MC<26.2
            //$$value = "FIELD",
            //$$target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;",
            //$$opcode = Opcodes.GETFIELD
            //#else
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;screen()Lnet/minecraft/client/gui/screens/Screen;"
            //#endif
        )
)
    private void injectOnMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        ZKeys.onRawMouseInput(input.button(), action);
    }
}
