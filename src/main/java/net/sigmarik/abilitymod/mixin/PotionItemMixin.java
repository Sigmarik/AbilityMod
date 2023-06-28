package net.sigmarik.abilitymod.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import net.sigmarik.abilitymod.AbilityMod;
import net.sigmarik.abilitymod.util.ServerState;
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
                ServerState.hasTrait((PlayerEntity)user, AbilityMod.TRAIT_ADDICTION) &&
                ServerState.getAddictionTimer((PlayerEntity)user) < AbilityMod.ADDICTION_MID_TIMER) {
            ServerState.setAddictionTimer((PlayerEntity)user, AbilityMod.ADDICTION_MID_TIMER);
        }
    }
}
