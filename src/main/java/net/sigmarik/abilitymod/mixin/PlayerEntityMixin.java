package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.PropSets;
import net.sigmarik.abilitymod.util.ServerState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
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
        if (PropSets.DIRTY_BLOCKS.contains(getSteppingBlockState().getBlock()))
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

    private void tickFast() {
        if (hasTrait(AbilityMod.TRAIT_FAST)) {
            Objects.requireNonNull(getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.15);
        } else {  //                             PlayerEntity::createPlayerAttributes --v
            Objects.requireNonNull(getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.1);
        }
    }

    private void tickStrong() {
        if (hasTrait(AbilityMod.TRAIT_STRONG)) {
            Objects.requireNonNull(getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(1.5);
        } else {  //                            PlayerEntity::createPlayerAttributes --v
            Objects.requireNonNull(getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(1.0);
        }
    }

    private void tickHotIron() {
        if (!hasTrait(AbilityMod.TRAIT_HOT_IRON)) return;

        if (getInventory().containsAny(PropSets.IRON_ITEMS)) setOnFireFor(3);
    }

    // Copied from MobEntity::isAffectedByDayLight
    private boolean isAffectedByDaylight() {
        if (this.world.isDay() && !this.world.isClient) {
            float f = this.getBrightnessAtEyes();
            BlockPos blockPos = BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ());
            boolean bl = this.isWet() || this.inPowderSnow || this.wasInPowderSnow;
            if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !bl && this.world.isSkyVisible(blockPos)) {
                return true;
            }
        }

        return false;
    }

    // Adapted version of ZombieEntity::tickMovement
    private void tickDamagedByLight() {
        if (!hasTrait(AbilityMod.TRAIT_DAMAGED_BY_LIGHT)) return;
        if (!isAffectedByDaylight()) return;

        ItemStack helmet = this.getEquippedStack(EquipmentSlot.HEAD);

        if (!helmet.isEmpty()) {
            if (helmet.isDamageable()) {
                helmet.setDamage(helmet.getDamage() + this.random.nextInt(2));
                if (helmet.getDamage() >= helmet.getMaxDamage()) {
                    this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                    this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }
        } else {
            this.setOnFireFor(8);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void traitedTick(CallbackInfo ci) {
        tickFearOfWater();
        tickAddiction();
        tickBoatMagnet();
        tickDirtSickness();
        tickHated();
        tickFast();
        tickStrong();
        tickHotIron();
        tickDamagedByLight();
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
