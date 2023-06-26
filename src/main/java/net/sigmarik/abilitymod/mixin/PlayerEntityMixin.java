package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.ServerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void damageOnWater(CallbackInfo ci) {
        if (ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_FEAR_OF_WATER) && isWet()) {
            damage(getDamageSources().magic(), 3);
        }
    }

    @Inject(method = "eatFood", at = @At("RETURN"))
    private void poisonOnRotten(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.getItem().equals(Items.ROTTEN_FLESH) &&
                ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_HARMFUL_ROTTEN_FLESH)) {
            if (ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_INVERT_EFFECTS)) {
                addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 30 * 20));
            } else {
                addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 30 * 20));
            }
        }
    }
}
