package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.ServerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow public abstract PlayerInventory getInventory();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    private boolean hasTrait(String trait) {
        return ServerState.hasTrait((PlayerEntity)(Object)this, trait);
    }

    private void tickFearOfWater() {
        if (!hasTrait(AbilityMod.TRAIT_FEAR_OF_WATER) || !isWet()) return;

        damage(getDamageSources().magic(), 3);
    }

    private void tickAddiction() {
        if (!hasTrait(AbilityMod.TRAIT_ADDICTION)) return;

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

    private void tickBoatMagnet() {
        if (!hasTrait(AbilityMod.TRAIT_BOAT_MAGNET)) return;
        if (!hasVehicle() || !(getVehicle() instanceof BoatEntity)) return;

        Box attractionBox = Box.of(getPos(),
                AbilityMod.BOAT_ATTRACTION_DISTANCE,
                AbilityMod.BOAT_ATTRACTION_DISTANCE,
                AbilityMod.BOAT_ATTRACTION_DISTANCE);
        Box ignoranceBox = Box.of(getPos(), 4, 4, 4);

        final Predicate<Entity> boatPredicate = boatEntity -> true;

        for (Entity entity : getEntityWorld().getEntitiesByClass(BoatEntity.class, attractionBox, boatPredicate)) {
            if (entity.hasPassenger(this)) continue;
            if (entity.hasVehicle()) continue;
            if (entity.getBoundingBox().intersects(ignoranceBox)) continue;

            Vec3d delta = getPos().subtract(entity.getPos());
            Vec3d direction = delta.normalize();
            double multiplier = delta.lengthSquared() * AbilityMod.BOAT_ATTRACTION_FACTOR;

            entity.addVelocity(direction.multiply(1.0, 0.0, 1.0).multiply(multiplier));
        }
    }

    private void tickDirtSickness() {
        if (!hasTrait(AbilityMod.TRAIT_DIRT_SICKNESS) || hasVehicle() || fallDistance > 0.0) return;

        if (isTouchingWater() &&
                (getWorld().getBiome(getBlockPos()).matchesKey(BiomeKeys.SWAMP) ||
                getWorld().getBiome(getBlockPos()).matchesKey(BiomeKeys.MANGROVE_SWAMP) ||
                getWorld().getBiome(getBlockPos()).matchesKey(BiomeKeys.LUSH_CAVES)))
            addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 5 * 20));
        if (AbilityMod.DIRTY_BLOCKS.contains(getSteppingBlockState().getBlock()))
            addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 5 * 20));
    }

    private void tickHated() {
        if (!hasTrait(AbilityMod.TRAIT_HATED)) return;

        Box aggroBox = Box.of(getPos(), 20, 10, 20);

        Predicate<LivingEntity> aggroPredicate = entity -> true;

        for (LivingEntity entity : getEntityWorld().getEntitiesByClass(LivingEntity.class, aggroBox, aggroPredicate)) {
            if (!(entity instanceof Angerable)) continue;

            ((Angerable) entity).setAngryAt(getUuid());
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void traitedTick(CallbackInfo ci) {
        tickFearOfWater();
        tickAddiction();
        tickBoatMagnet();
        tickDirtSickness();
        tickHated();
    }

    @Inject(method = "eatFood", at = @At("RETURN"))
    private void traitedEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.getItem().equals(Items.ROTTEN_FLESH) && hasTrait(AbilityMod.TRAIT_HARMFUL_ROTTEN_FLESH)) {
            if (hasTrait(AbilityMod.TRAIT_INVERT_EFFECTS)) {
                addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 30 * 20));
            } else {
                addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 30 * 20));
            }
        }
    }
}
