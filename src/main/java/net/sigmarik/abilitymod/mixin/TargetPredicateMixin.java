package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.ServerState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetPredicate.class)
public abstract class TargetPredicateMixin {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void checkUndead(@Nullable LivingEntity baseEntity, LivingEntity targetEntity, CallbackInfoReturnable<Boolean> cir) {
        if (baseEntity == null) return;
        if (baseEntity.isUndead() &&
                targetEntity instanceof PlayerEntity &&
                ServerState.hasTrait((PlayerEntity)targetEntity, AbilityMod.TRAIT_IGNORED_BY_UNDEAD) &&
                baseEntity.getAttacker() != targetEntity) {
            cir.setReturnValue(false);
        }
    }
}
