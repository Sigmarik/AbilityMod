package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.ServerState;
import net.sigmarik.abilitymod.util.Traits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Redirect(method = "respawnPlayer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;refreshPositionAndAngles(DDDFF)V"))
    private void traitedRefreshPositionAndAngles(ServerPlayerEntity instance, double X, double Y,
                                                 double Z, float angle, float pitch) {
        if (!ServerState.hasTrait(instance, Traits.RANDOM_RESPAWNS)) {
            instance.refreshPositionAndAngles(X, Y, Z, angle, pitch);
            return;
        }

        BlockPos respawnPos = new BlockPos((int)X, (int)Y, (int)Z);

        if (instance.getLastDeathPos().isEmpty()) return;

        int radius = instance.getLastDeathPos().get().getPos().getManhattanDistance(respawnPos);

        if (instance.getWorld().getRegistryKey() != instance.getSpawnPointDimension()) {
            instance.refreshPositionAndAngles(X, Y, Z, angle, pitch);
            return;
        }

        respawnPos = respawnPos.add(instance.getWorld().getRandom().nextBetween(-radius, radius), 0,
                instance.getWorld().getRandom().nextBetween(-radius, radius));

        Optional<Vec3d> position = PlayerEntity.findRespawnPosition(instance.getWorld(), respawnPos, angle,
                true, instance.isAlive());

        if (position.isEmpty()) {
            instance.refreshPositionAndAngles(X, Y, Z, angle, pitch);
            return;
        }

        Vec3d chosenPos = position.get();

        AbilityMod.LOGGER.info("Calculated spawn position: " + chosenPos);

        instance.refreshPositionAndAngles(chosenPos.getX(), chosenPos.getY(), chosenPos.getZ(), angle, pitch);
    }
}
