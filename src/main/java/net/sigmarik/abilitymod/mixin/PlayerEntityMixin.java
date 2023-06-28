package net.sigmarik.abilitymod.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow public abstract PlayerInventory getInventory();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void damageOnWater(CallbackInfo ci) {
        if (ServerState.hasTrait((PlayerEntity)(Object)this, AbilityMod.TRAIT_FEAR_OF_WATER) && isWet()) {
            damage(getDamageSources().magic(), 3);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void damageByDaylight(CallbackInfo ci) {
        if (ServerState.hasTrait((PlayerEntity) ((LivingEntity) this), AbilityMod.TRAIT_DAMAGED_BY_LIGHT)) {
            World world = this.getWorld();
            ItemStack helmet = this.getInventory().getArmorStack(3);
            long time = world.getTimeOfDay() % 24000;
            int lightLevel = world.getLightLevel(LightType.SKY, this.getBlockPos());
            int fireResistanceStatusEffect = 12;
            int fireChance = 55;
            if (!(lightLevel <= 11 || !world.isSkyVisible(this.getBlockPos()) || this.isTouchingWaterOrRain() || this.isInLava() || this.hasStatusEffect(StatusEffect.byRawId(fireResistanceStatusEffect)) || this.getBlockStateAtPos().getBlock().equals(Blocks.COBWEB) || (time >= 12542 && time < 23460) || !world.getRegistryKey().getValue().equals(DimensionTypes.OVERWORLD_ID))) {
                if (world.random.nextInt(fireChance) == 0) {
                    if (helmet != null && helmet.isDamageable()) {
                        this.getInventory().damageArmor((new DamageSources(world.getRegistryManager())).inFire(), 1, PlayerInventory.HELMET_SLOTS);
                    } else {
                        this.setOnFireFromLava();
                    }
                }
            }
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
