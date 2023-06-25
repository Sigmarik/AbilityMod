package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.world.World;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.TraitStates;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity {
    public EnderPearlEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "onCollision", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z")
    )
    private boolean traitedDamage(Entity entity, DamageSource damageSource, float amount) {
        if (TraitStates.hasTrait((PlayerEntity) entity, AbilityMod.TRAIT_EASY_PEARLS)) {
            return false;
        } else return entity.damage(damageSource, amount);
    }
}
