package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import net.sigmarik.abilitymod.util.ServerState;
import net.sigmarik.abilitymod.util.Traits;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {

    public PotionItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void traitedFinishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof PlayerEntity && PotionUtil.getPotion(stack) == Potions.THICK &&
                ServerState.hasTrait((PlayerEntity)user, Traits.ADDICTION) &&
                ServerState.getAddictionTimer((PlayerEntity)user) < net.sigmarik.abilitymod.util.Settings.ADDICTION_MID_TIMER) {
            int time = ServerState.getAddictionTimer((PlayerEntity)user);
            if (0 < time && time < net.sigmarik.abilitymod.util.Settings.ADDICTION_WARNING_TIMER) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 300));
            }
            ServerState.setAddictionTimer((PlayerEntity)user, net.sigmarik.abilitymod.util.Settings.ADDICTION_MID_TIMER);
        }
    }
}
