package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.ServerState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    private void tickFearOfWater() {
        if (ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_FEAR_OF_WATER) && isWet()) {
            damage(getDamageSources().magic(), 3);
        }
    }

    private void tickAddiction() {
        if (ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_ADDICTION)) {
            ServerState.tickAddictionTimer((PlayerEntity)(Object)this);

            int timer = ServerState.getAddictionTimer((PlayerEntity)(Object)this);

            if (timer < AbilityMod.ADDICTION_WARNING_TIMER) {
                addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, timer));
            }
            if (timer == 0) {
                addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 20));
                addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 20));
                addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20));
                addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 20));
                addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20));
            }
        }
    }

    private void tickBoatMagnet() {
        if (ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_BOAT_MAGNET) && hasVehicle() &&
                (getVehicle().getType() == EntityType.BOAT || getVehicle().getType() == EntityType.CHEST_BOAT)) {
            Box attractionBox = Box.of(getPos(),
                    AbilityMod.BOAT_ATTRACTION_DISTANCE,
                    AbilityMod.BOAT_ATTRACTION_DISTANCE,
                    AbilityMod.BOAT_ATTRACTION_DISTANCE);

            final Predicate<Entity> boatPredicate = boatEntity -> true;

            for (Entity entity : getEntityWorld().getEntitiesByClass(BoatEntity.class, attractionBox, boatPredicate)) {
                if (entity.hasPassenger(this)) continue;
                if (entity.hasVehicle()) continue;
                if (entity.getBoundingBox().intersects(getVehicle().getBoundingBox())) continue;

                entity.addVelocity(getPos().subtract(entity.getPos()).add(getVelocity())
                       .multiply(1.0, 0.0, 1.0).multiply(AbilityMod.BOAT_ATTRACTION_FACTOR));
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void traitedTick(CallbackInfo ci) {
        tickFearOfWater();
        tickAddiction();
        tickBoatMagnet();
    }

    @Inject(method = "eatFood", at = @At("RETURN"))
    private void traitedEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
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
