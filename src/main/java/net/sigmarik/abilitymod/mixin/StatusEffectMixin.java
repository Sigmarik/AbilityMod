package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.sigmarik.abilitymod.util.ServerState;
import net.sigmarik.abilitymod.util.Traits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffect.class)
public abstract class StatusEffectMixin {
    @Inject(method = "applyUpdateEffect", at = @At("HEAD"), cancellable = true)
    private void invertRegeneration(LivingEntity entity, int amplifier, CallbackInfo ci) {
        if (entity instanceof PlayerEntity && ServerState.hasTrait((PlayerEntity)entity, Traits.TRAIT_INVERT_EFFECTS)) {
            if ((Object)this == StatusEffects.REGENERATION) {
                if (entity.getHealth() > 1.0F) {
                    entity.damage(entity.getDamageSources().magic(), 1.0F);
                }
                ci.cancel();
            } else if ((Object)this == StatusEffects.POISON) {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    entity.heal(1.0F);
                }
                ci.cancel();
            }
        }
    }

    @Redirect(method = "applyInstantEffect", at = @At(
            value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isUndead()Z"))
    private boolean traitedIsUndead(LivingEntity entity) {
        return  entity.isUndead() ||
                (entity instanceof PlayerEntity &&
                ServerState.hasTrait((PlayerEntity)entity, Traits.TRAIT_INVERT_EFFECTS));
    }
}
