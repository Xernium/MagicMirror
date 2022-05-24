package me.fivepb.magicmirror.mixin;

import me.fivepb.magicmirror.MagicMirror;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Connection.class)
public class ClientConnectionMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void interceptPacketExchange(Packet<?> packet, CallbackInfo ci){
        if (packet instanceof ServerboundCustomQueryPacket pi) {
            MagicMirror.getInstance().track(pi);
        }
    }

}
