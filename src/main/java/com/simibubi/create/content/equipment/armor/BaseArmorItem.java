package com.simibubi.create.content.equipment.armor;

import io.github.fabricators_of_create.porting_lib.item.extensions.ArmorTextureItem;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class BaseArmorItem extends ArmorItem implements ArmorTextureItem {
	protected final ResourceLocation textureLoc;

	public BaseArmorItem(Holder<ArmorMaterial> armorMaterial, ArmorItem.Type type, Properties properties, ResourceLocation textureLoc) {
		super(armorMaterial, type, properties.stacksTo(1));
		this.textureLoc = textureLoc;
	}

	@Override
	public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
		return ResourceLocation.parse(String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d.png", textureLoc.getNamespace(), textureLoc.getPath(), slot == EquipmentSlot.LEGS ? 2 : 1));
	}
}
