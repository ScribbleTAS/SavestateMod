package com.minecrafttas.savestatemod.mixin.tickratechanger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.server.dedicated.DedicatedServer;

/**
 * This mixin disabled the max tick length
 * @author Pancake
 */
@Mixin(DedicatedServer.class)
public class MixinDedicatedServerTRC {

	@Overwrite
    public long getMaxTickLength() {
        return Long.MAX_VALUE;
    }
	
}
