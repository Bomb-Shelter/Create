package com.simibubi.create.foundation.mixin.accessor.fabric;

import net.minecraft.data.tags.TagsProvider.TagAppender;

import net.minecraft.tags.TagBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TagAppender.class)
public interface TagAppenderAccessor {
	@Accessor
	TagBuilder getBuilder();
}
