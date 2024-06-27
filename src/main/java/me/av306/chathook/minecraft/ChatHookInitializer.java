package me.av306.chathook.minecraft;

import net.fabricmc.api.ModInitializer;

public class ChatHookInitializer implements ModInitializer
{
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ChatHook.INSTANCE.initialise();
	}
}