package org.zephy.zkeys.mixins;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zephy.zkeys.ZKeys;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Inject(
        method = "onButton",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;",
            opcode = Opcodes.GETFIELD
        )
    )
    private void injectOnMouseButton(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        ZKeys.onRawMouseInput(input.button(), action);
    }

//    @Inject(
//        method = "onScroll",
//        at = @At(
//            value = "FIELD",
//            target = "Lnet/minecraft/client/Minecraft;options:Lnet/minecraft/client/Options;",
//            opcode = Opcodes.GETFIELD
//        )
//    )
//    private void injectOnMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
//        MouseListener.onRawMouseScroll(vertical);
//    }
}
