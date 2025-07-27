package com.simibubi.create.content.equipment.armor;

import net.fabricmc.fabric.api.registry.FuelRegistry;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.Create;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class CardboardArmorItem extends BaseArmorItem {

	public CardboardArmorItem(Type type, Properties properties) {
		super(AllArmorMaterials.CARDBOARD, type, properties, Create.asResource("cardboard"));
		FuelRegistry.INSTANCE.add(this, 1000);
	}

	/*@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		return 1000;
	}*/

}
