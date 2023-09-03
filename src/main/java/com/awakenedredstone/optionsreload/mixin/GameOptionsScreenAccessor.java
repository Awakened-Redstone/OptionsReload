package com.awakenedredstone.optionsreload.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameOptionsScreen.class)
public interface GameOptionsScreenAccessor {
    @Accessor Screen getParent();
}
