package com.simibubi.create.content.equipment.armor;

import java.util.List;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.foundation.advancement.AllAdvancements;

import io.github.fabricators_of_create.porting_lib.enchant.CustomEnchantingBehaviorItem;
import io.github.fabricators_of_create.porting_lib.entity.events.tick.EntityTickEvent;
import io.github.fabricators_of_create.porting_lib.entity.events.tick.EntityTickEvent.Post;
import io.github.fabricators_of_create.porting_lib.item.extensions.CustomSupportsEnchantItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

public class DivingHelmetItem extends BaseArmorItem implements CustomSupportsEnchantItem, CustomEnchantingBehaviorItem {
	public static final EquipmentSlot SLOT = EquipmentSlot.HEAD;
	public static final ArmorItem.Type TYPE = ArmorItem.Type.HELMET;

	public DivingHelmetItem(Holder<ArmorMaterial> material, Properties properties, ResourceLocation textureLoc) {
		super(material, TYPE, properties, textureLoc);
	}

	@Override
	public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
		if (enchantment.is(Enchantments.AQUA_AFFINITY))
			return false;
		return CustomSupportsEnchantItem.super.supportsEnchantment(stack, enchantment);
	}

	@Override
	public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
		if (enchantment.is(Enchantments.AQUA_AFFINITY))
			return 1;
		return CustomEnchantingBehaviorItem.super.getEnchantmentLevel(stack, enchantment);
	}

	@Override
	public ItemEnchantments getAllEnchantments(ItemStack stack, RegistryLookup<Enchantment> lookup) {
		ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(CustomEnchantingBehaviorItem.super.getAllEnchantments(stack, lookup));
		enchants.set(lookup.getOrThrow(Enchantments.AQUA_AFFINITY), 1);
		return enchants.toImmutable();
	}

	public static boolean isWornBy(Entity entity) {
		return !getWornItem(entity).isEmpty();
	}

	public static ItemStack getWornItem(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity)) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = livingEntity.getItemBySlot(SLOT);
		if (!(stack.getItem() instanceof DivingHelmetItem)) {
			return ItemStack.EMPTY;
		}
		return stack;
	}

	static {
		Post.EVENT.register(event -> {
			if (event.getEntity() instanceof LivingEntity livingEntity)
				breatheUnderwater(livingEntity);
		});
	}

	public static void breatheUnderwater(/*LivingBreatheEvent event*/ LivingEntity entity) {
		//LivingEntity entity = event.getEntity();
		Level level = entity.level();

		if (level.isClientSide)
			entity.getCustomData().remove("VisualBacktankAir");

		ItemStack helmet = getWornItem(entity);
		if (helmet.isEmpty())
			return;

		boolean lavaDiving = entity.isInLava();
		if (!helmet.has(DataComponents.FIRE_RESISTANT) && lavaDiving)
			return;

		if (!entity.isEyeInFluid(AllFluidTags.DIVING_FLUIDS.tag) && !lavaDiving)
			return;
		if (entity instanceof Player player && (player.isSpectator() || player.isCreative()))
			return;

		List<ItemStack> backtanks = BacktankUtil.getAllWithAir(entity);
		if (backtanks.isEmpty())
			return;

		if (lavaDiving) {
			if (entity instanceof ServerPlayer sp)
				AllAdvancements.DIVING_SUIT_LAVA.awardTo(sp);
			if (backtanks.stream()
				.noneMatch(backtank -> backtank.has(DataComponents.FIRE_RESISTANT)))
				return;
		}

		float visualBacktankAir = 0f;
		for (ItemStack stack : backtanks)
			visualBacktankAir += BacktankUtil.getAir(stack);

		if (level.isClientSide)
			entity.getCustomData()
				.putInt("VisualBacktankAir", Math.round(visualBacktankAir));

		if (level.getGameTime() % 20 == 0)
			BacktankUtil.consumeAir(entity, backtanks.get(0), 1);

		if (lavaDiving)
			return;

		if (entity instanceof ServerPlayer sp)
			AllAdvancements.DIVING_SUIT.awardTo(sp);

		//event.setCanBreathe(true);
		//event.setRefillAirAmount(entity.getMaxAirSupply());
		entity.setAirSupply(Math.min(entity.getMaxAirSupply(), entity.getAirSupply() + 10));
		entity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30, 0, true, false, true));
	}
}
