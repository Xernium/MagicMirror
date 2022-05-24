package me.fivepb.magicmirror.mixin;

import me.fivepb.magicmirror.MagicMirror;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class ClientLoginListenerMixin {

    @Inject(method = "handleCustomQuery", at = @At("HEAD"))
    private void onCustomQuery(ClientboundCustomQueryPacket cbc, CallbackInfo ci){
        MagicMirror.getInstance().track(cbc);
    }

    @Inject(method = "handleGameProfile", at = @At("TAIL"))
    private void onGameProfile(CallbackInfo ci){
        MagicMirror.getInstance().onDone();
    }

    @Inject(method = "handleHello", at = @At("TAIL"))
    private void onHello(CallbackInfo ci){
        MagicMirror.getInstance().clearQueue();
    }

}
