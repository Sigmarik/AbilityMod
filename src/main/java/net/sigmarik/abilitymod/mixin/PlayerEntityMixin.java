package net.sigmarik.abilitymod.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
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

    @Shadow public abstract Iterable<ItemStack> getArmorItems();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    private void tickFearOfWater() {
        if (!ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_FEAR_OF_WATER) && isWet()) {
            return;
        }
        damage(getDamageSources().magic(), 3);
    }

    private void tickAddiction() {
        if (!ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_ADDICTION)) {
            return;
        }
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
        if (!ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_BOAT_MAGNET) && hasVehicle() &&
                (getVehicle().getType() == EntityType.BOAT || getVehicle().getType() == EntityType.CHEST_BOAT)) {
            return;
        }
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

    private void tickLightBurn() {
        if (!ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_DAMAGED_BY_LIGHT)) {
            return;
        }
        World world = this.getWorld();
        ItemStack helmet = this.getInventory().getArmorStack(3);
        long time = world.getTimeOfDay() % 24000;
        int lightLevel = world.getLightLevel(LightType.SKY, this.getBlockPos());
        if (!(lightLevel <= 11 ||
                !world.isSkyVisible(this.getBlockPos()) || this.isTouchingWaterOrRain() || this.isInLava() ||
                this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE) ||
                this.getBlockStateAtPos().getBlock().equals(Blocks.COBWEB) || (time >= 12542 && time < 23460) ||
                !world.getRegistryKey().getValue().equals(DimensionTypes.OVERWORLD_ID))) {
            if (world.random.nextInt(AbilityMod.DAYLIGHT_BURNING_CHANCE_PER_TICK) == 0) {
                if (helmet != null && helmet.isDamageable()) {
                    this.getInventory().damageArmor((new DamageSources(world.getRegistryManager())).inFire(),
                            1, PlayerInventory.HELMET_SLOTS);
                } else {
                    this.setOnFireFromLava();
                }
            }
        }
    }

    private void tickIronBurn() {
        if (!ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_HOT_IRON)) {
            return;
        }
        boolean doDamage = false;
        for (int stackId = 0; stackId < this.getInventory().size(); ++stackId) {
            doDamage |= AbilityMod.IRON_ITEMS.contains(this.getInventory().getStack(stackId).getItem());
        }
        if (doDamage) {
            this.damage(new DamageSources(world.getRegistryManager()).hotFloor(), 2);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void traitedTick(CallbackInfo ci) {
        tickFearOfWater();
        tickAddiction();
        tickBoatMagnet();
        tickLightBurn();
        tickIronBurn();
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
