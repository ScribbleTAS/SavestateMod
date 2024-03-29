package com.minecrafttas.savestatemod.tickratechanger;

import com.minecrafttas.savestatemod.networking.IClientPacketHandler;
import com.minecrafttas.savestatemod.networking.IServerPacketHandler;
import com.minecrafttas.savestatemod.networking.duck.ServerboundCustomPayloadPacketDuck;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

/**
 * Tick advance for minecraft.
 * ~ same logic as tickrate changer
 * @author Pancake
 */
public class TickAdvance implements IClientPacketHandler, IServerPacketHandler{

	static final ResourceLocation TICK_ADVANCE_RL = new ResourceLocation("lotas", "tickadvance");
	static final ResourceLocation TICK_RL = new ResourceLocation("lotas", "tick");
	@Environment(EnvType.CLIENT)
	public Minecraft mc;
	public MinecraftServer mcserver;

	// Is tick advance enabled
	public boolean tickadvance;
	// Should tick advance clientside
	public boolean shouldTickClient;
	// Should tick advance serverside
	public boolean shouldTickServer;
	
	/**
	 * Updates the client tickadvance status when receiving a packet
	 */
	@Override
	@Environment(EnvType.CLIENT)
	public void onClientPacket(ClientboundCustomPayloadPacket p, ClientPacketListener clientPacketListener, Minecraft minecraft) {
		if (TICK_ADVANCE_RL.equals(p.getIdentifier()))
			this.tickadvance = p.getData().readBoolean();
		if (TICK_RL.equals(p.getIdentifier()))
			this.shouldTickClient = true; // Tick the Client
	}

	/**
	 * Updates the server tickadvance status and resend when receiving a packet
	 */
	@Override
	public void onServerPacket(ServerboundCustomPayloadPacketDuck packet, ServerGamePacketListenerImpl serverGamePacketListenerImpl, ServerPlayer player) {
		if (TICK_ADVANCE_RL.equals(packet.getIdentifier()))
			this.updateTickadvanceStatus(packet.getData().readBoolean());
		if (TICK_RL.equals(packet.getIdentifier()))
			this.updateTickadvance();
	}

	/**
	 * Client-side only tick method
	 */
	@Environment(EnvType.CLIENT)
	public void onTick(Minecraft mc) {
		this.shouldTickClient = false;
	}

	/**
	 * Client-side only tickadvance update request. Sends a packet to the server toggeling tickadvance.
	 * @param tickadvance Tickadvance Status
	 */
	@Environment(EnvType.CLIENT)
	public void requestTickadvanceToggle() {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBoolean(!this.tickadvance);
		this.mc.getConnection().send(new ServerboundCustomPayloadPacket(TICK_ADVANCE_RL, buf));
	}

	/**
	 * Client-Side only tick advance request. Sends a packet to the server advancing a tick.
	 */
	@Environment(EnvType.CLIENT)
	public void requestTickadvance() {
		if (!this.tickadvance || this.shouldTickClient) return;
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBoolean(false);
		this.mc.getConnection().send(new ServerboundCustomPayloadPacket(TICK_RL, buf));
	}

	/**
	 * Server-Side only tickadvance update. Sends a packet to all players
	 * @param tickadvance Tickadvance status
	 */
	public void updateTickadvanceStatus(boolean tickadvance) {
		this.tickadvance = tickadvance;
		// Update Tickadvance for all Clients
		this.mcserver.getPlayerList().getPlayers().forEach(c -> {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			buf.writeBoolean(tickadvance);
			c.connection.send(new ClientboundCustomPayloadPacket(TICK_ADVANCE_RL, buf));
		});
	}

	/**
	 * Server-Side only tick advance. Sends a packet to all players
	 */
	public void updateTickadvance() {
		this.shouldTickServer = true;
		// Tick all Clients
		this.mcserver.getPlayerList().getPlayers().forEach(c -> {
			c.connection.send(new ClientboundCustomPayloadPacket(TICK_RL, new FriendlyByteBuf(Unpooled.buffer())));
		});
	}

	/**
	 * Clears local data on disconnect
	 */
	@Environment(EnvType.CLIENT)
	public void onDisconnect() {
		this.tickadvance = false;
	}

	/**
	 * Updates client data on connect
	 */
	public void onConnect(ServerPlayer c) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBoolean(this.tickadvance);
		c.connection.send(new ClientboundCustomPayloadPacket(TICK_ADVANCE_RL, buf));
	}

	// Place getters here to not confuse with public variables that shall not be set

	public boolean isTickadvanceEnabled() {
		return this.tickadvance;
	}

}
