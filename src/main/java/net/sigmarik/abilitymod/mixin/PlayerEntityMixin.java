package net.sigmarik.abilitymod.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.Predicate;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow public abstract PlayerInventory getInventory();

    @Shadow @Nullable public abstract ItemEntity dropItem(ItemStack stack, boolean retainOwnership);

    @Shadow private ItemStack selectedItem;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    private boolean hasTrait(String trait) {
        return ServerState.hasTrait((PlayerEntity)(Object)this, trait);
    }

    private void tickFearOfWater() {
        if (!hasTrait(Traits.FEAR_OF_WATER) || !isWet()) return;

        if (hasStatusEffect(StatusEffects.WATER_BREATHING) && !isSubmergedInWater()) return;

        damage(getDamageSources().magic(), 3);
    }

    private void tickAddiction() {
        if (!hasTrait(Traits.ADDICTION)) return;

        ServerState.tickAddictionTimer((PlayerEntity)(Object)this);

        int timer = ServerState.getAddictionTimer((PlayerEntity)(Object)this);

        if (timer < Settings.ADDICTION_WARNING_TIMER) {
            addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, timer));
        }
        if (timer == 0) {
            addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200));
            addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200));
            addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 200));
            addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 4));
        }
    }

    private void tickBoatMagnet() {
        if (!hasTrait(Traits.BOAT_MAGNET)) return;
        if (!hasVehicle() || !(getVehicle() instanceof BoatEntity)) return;

        Box attractionBox = Box.of(getPos(), 8, 8, 8);
        Box ignoranceBox = Box.of(getPos(), 4, 4, 4);

        final Predicate<Entity> boatPredicate = boatEntity -> true;

        for (Entity entity : getEntityWorld().getEntitiesByClass(BoatEntity.class, attractionBox, boatPredicate)) {
            if (entity.hasPassenger(this)) continue;
            if (entity.hasVehicle()) continue;
            if (entity.getBoundingBox().intersects(ignoranceBox)) continue;

            Vec3d delta = getPos().subtract(entity.getPos());
            Vec3d direction = delta.normalize();
            double multiplier = delta.lengthSquared() * 0.05;

            entity.addVelocity(direction.multiply(1.0, 0.0, 1.0).multiply(multiplier));
        }
    }

    private void tickDirtSickness() {
        if (!hasTrait(Traits.DIRT_SICKNESS) || hasVehicle() || fallDistance > 0.0) return;

        if (isTouchingWater() &&
                (getWorld().getBiome(getBlockPos()).matchesKey(BiomeKeys.SWAMP) ||
                getWorld().getBiome(getBlockPos()).matchesKey(BiomeKeys.MANGROVE_SWAMP) ||
                getWorld().getBiome(getBlockPos()).matchesKey(BiomeKeys.LUSH_CAVES)))
            addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 5 * 20));
        if (PropSets.DIRTY_BLOCKS.contains(getSteppingBlockState().getBlock()))
            addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 5 * 20));
    }

    private void tickHated() {
        if (!hasTrait(Traits.HATED)) return;

        Box aggroBox = Box.of(getPos(), 20, 10, 20);

        Predicate<LivingEntity> aggroPredicate = entity -> true;

        for (LivingEntity entity : getEntityWorld().getEntitiesByClass(LivingEntity.class, aggroBox, aggroPredicate)) {
            if (!(entity instanceof Angerable)) continue;

            ((Angerable) entity).setAngryAt(getUuid());
        }
    }

    private void tickFast() {
        if (hasTrait(Traits.FAST)) {
            Objects.requireNonNull(getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.13);
        } else {  //                                                     PlayerEntity::createPlayerAttributes --v
            Objects.requireNonNull(getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.1);
        }
    }

    private void tickStrong() {
        if (hasTrait(Traits.STRONG)) {
            Objects.requireNonNull(getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(4.0);
        } else {  //                                                    PlayerEntity::createPlayerAttributes --v
            Objects.requireNonNull(getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(1.0);
        }
    }

    private void tickHotIron() {
        if (!hasTrait(Traits.HOT_IRON)) return;

        if (getInventory().containsAny(PropSets.IRON_ITEMS))
            addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200));
    }

    // Copied from MobEntity::isAffectedByDayLight
    private boolean isAffectedByDaylight() {
        if (this.world.isDay() && !this.world.isClient) {
            float f = this.getBrightnessAtEyes();
            BlockPos blockPos = BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ());
            boolean bl = this.isWet() || this.inPowderSnow || this.wasInPowderSnow;
            return f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && !bl && this.world.isSkyVisible(blockPos);
        }

        return false;
    }

    // Adapted version of ZombieEntity::tickMovement
    private void tickDamagedByLight() {
        if (!hasTrait(Traits.DAMAGED_BY_LIGHT)) return;
        if (!isAffectedByDaylight()) return;

        ItemStack helmet = this.getEquippedStack(EquipmentSlot.HEAD);

        if (!helmet.isEmpty() && helmet.isDamageable()) {
            helmet.setDamage(helmet.getDamage() + this.random.nextInt(2));
            if (helmet.getDamage() >= helmet.getMaxDamage()) {
                this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
            }
        } else {
            this.setOnFireFor(8);
        }
    }

    private int lastSelectedSlot = -1;

    private void tickShakyHands() {
        if (lastSelectedSlot != -1 && getInventory().selectedSlot != lastSelectedSlot && hasTrait(Traits.SHAKY_HANDS) &&
                getWorld().getRandom().nextInt(100) < 10) {
            dropItem(getMainHandStack(), true);
            getInventory().removeStack(getInventory().selectedSlot);
        }
        lastSelectedSlot = getInventory().selectedSlot;
    }

    private void tickTrueDarkness() {
        if (!hasTrait(Traits.TRUE_DARKNESS)) return;

        if (getBrightnessAtEyes() < 0.1) addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 100));
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
        tickShakyHands();
        tickTrueDarkness();
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void traitedEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.getItem().equals(Items.ROTTEN_FLESH) && hasTrait(Traits.HARMFUL_ROTTEN_FLESH)) {
            if (hasTrait(Traits.INVERT_EFFECTS)) {
                addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 30 * 20));
            } else {
                addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 30 * 20));
            }
        }
    }

    private BarrelBlockEntity attemptBarrelCreation(BlockPos pos, int startingSlot) {
        if (getWorld().getBlockState(pos) == null) return null;
        if (getWorld().getBlockState(pos).getBlock().getHardness() < 0.0) return null;

        if (getInventory().count(Items.BARREL) == 0) return null;

        for (int stackId = 0; stackId < getInventory().size(); ++stackId) {
            if (getInventory().getStack(stackId).getItem() == Items.BARREL) {
                getInventory().removeStack(stackId, 1);
                break;
            }
        }

        BarrelBlockEntity barrel = new BarrelBlockEntity(pos, Blocks.BARREL.getDefaultState());

        getWorld().setBlockState(pos, Blocks.BARREL.getDefaultState());
        getWorld().addBlockEntity(barrel);

        return barrel;
    }

    private void fillBarrelWithLoot(BarrelBlockEntity barrel, int startingSlot) {
        NbtCompound barrelNbt = barrel.createNbt();

        DefaultedList<ItemStack> inventorySlice = DefaultedList.of();
        for (int slotId = startingSlot;
             slotId < Math.min(getInventory().size(), startingSlot + barrel.size());
             ++slotId) {
            inventorySlice.add(slotId - startingSlot, getInventory().getStack(slotId));
            getInventory().removeStack(slotId);
        }

        Inventories.writeNbt(barrelNbt, inventorySlice);
        barrel.readNbt(barrelNbt);
    }

    private void placeGraveSign(BlockPos pos) {
        if (world.getBlockState(pos) == null) return;
        if (world.getBlockState(pos).getBlock().getHardness() < 0.0) return;

        BlockState signState = Blocks.OAK_SIGN.getDefaultState()
                .with(Properties.ROTATION, getWorld().getRandom().nextInt(16));
        SignBlockEntity topSign = new SignBlockEntity(pos.add(0, 1, 0), signState);
        getWorld().setBlockState(pos.add(0, 1, 0), signState);
        getWorld().addBlockEntity(topSign);
        SignBlockEntity bottomSign = new SignBlockEntity(pos, signState);
        getWorld().setBlockState(pos, signState);
        getWorld().addBlockEntity(bottomSign);

        PhraseGenerator.generateGraveSign(topSign, bottomSign, getName().getString(), getWorld().getRandom());
    }

    @Inject(method = "dropInventory", at = @At("HEAD"))
    private void createCoffin(CallbackInfo ci) {
        if (!hasTrait(Traits.COFFIN)) return;

        BlockPos firstBarrelPos = getBlockPos();
        if (getSteppingBlockState() != null) firstBarrelPos = firstBarrelPos.add(0, -2, 0);

        BarrelBlockEntity firstBarrel = attemptBarrelCreation(firstBarrelPos, 0);

        if (firstBarrel == null) return;

        placeGraveSign(getBlockPos());

        BarrelBlockEntity secondBarrel = attemptBarrelCreation(firstBarrelPos.add(0, -1, 0), firstBarrel.size());

        fillBarrelWithLoot(firstBarrel, 0);
        if (secondBarrel == null) return;
        fillBarrelWithLoot(secondBarrel, firstBarrel.size());
    }

    @Inject(method = "updateTurtleHelmet", at = @At("HEAD"), cancellable = true)
    private void traitedTickTurtleHelmet(CallbackInfo ci) {
        if (hasTrait(Traits.FEAR_OF_WATER)) ci.cancel();
    }
}
