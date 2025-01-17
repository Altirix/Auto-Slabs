package io.github.andrew6rant.autoslabs.mixin;

import io.github.andrew6rant.autoslabs.PlacementUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Massive thanks to Oliver-makes-code for some of the code behind this mixin
// https://github.com/Oliver-makes-code/autoslab/blob/1.19/src/main/java/olivermakesco/de/autoslab/mixin/Mixin_ServerPlayerInteractionManager.java
@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Shadow
    protected ServerWorld world;

    @Redirect(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private boolean autoslabs$tryBreakSlab(ServerWorld instance, BlockPos pos, boolean b) {
        BlockState breakState = instance.getBlockState(pos);
        if (breakState.getBlock() instanceof SlabBlock) {
            SlabType slabType = breakState.get(SlabBlock.TYPE);
            if (slabType != SlabType.DOUBLE) return instance.removeBlock(pos, b);
            ServerPlayerEntity serverPlayer = player;
            assert serverPlayer != null;
            if (serverPlayer.isSneaking()) return instance.removeBlock(pos, b);

            SlabType breakType = PlacementUtil.calcKleeSlab(breakState, PlacementUtil.calcRaycast(serverPlayer));
            boolean removed = instance.removeBlock(pos, b);
            world.setBlockState(pos, breakState.with(SlabBlock.TYPE, breakType));
            return removed;
        }
        return instance.removeBlock(pos, b);
    }
}
