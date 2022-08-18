package com.minecrafttas.savestatemod.mixin.client.networking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.savestatemod.networking.NetworkRegistry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;

/**
 * This mixin is purely responsible for the hooking up the events in {@link ClientLoTAS}. It also cancels the logger
 * @author Pancake
 */
@Mixin(ClientPacketListener.class)
@Environment(EnvType.CLIENT)
public class MixinClientGamePacketListenerImpl {
	
	@Inject(method = "handleCustomPayload", at = @At("HEAD"))
	public void hookCustomPayloadEvent(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
		NetworkRegistry.fireClientPackets(packet);
	}
	
}