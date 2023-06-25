package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.TraitStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void damageOnWater(CallbackInfo ci) {
        if (TraitStates.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_FEAR_OF_WATER) && isWet()) {
            damage(getDamageSources().magic(), 3);
        }
    }
}
