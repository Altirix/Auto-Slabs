package io.github.andrew6rant.autoslabs.mixin;

import io.github.andrew6rant.autoslabs.PlacementUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


// Massive thanks to Oliver-makes-code for some of the code behind this mixin
// https://github.com/Oliver-makes-code/autoslab/blob/1.19/src/main/java/olivermakesco/de/autoslab/mixin/Mixin_ClientPlayerInteractionManager.java
@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Redirect(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean autoslabs$tryBreakSlab(World instance, BlockPos pos, BlockState state, int flags) {
        var breakState = instance.getBlockState(pos);
        if (breakState.getBlock() instanceof SlabBlock) {
            SlabType slabType = breakState.get(SlabBlock.TYPE);
            if (slabType != SlabType.DOUBLE) return instance.setBlockState(pos, state, flags);
            ClientPlayerEntity clientPlayer = client.player;
            assert clientPlayer != null;
            if (clientPlayer.isSneaking()) return instance.setBlockState(pos, state, flags);

            SlabType breakType = PlacementUtil.calcKleeSlab(breakState, PlacementUtil.calcRaycast(clientPlayer));
            return instance.setBlockState(pos, breakState.with(SlabBlock.TYPE, breakType), flags);
        }
        return instance.setBlockState(pos, state, flags);
    }
}
