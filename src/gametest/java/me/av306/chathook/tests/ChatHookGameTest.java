package me.av306.chathook.tests;

import java.lang.reflect.Method;

import net.minecraft.block.Blocks;
import net.minecraft.test.TestContext;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChatHookGameTest implements CustomTestMethodInvoker
{
    // TODO: write tests for the following:
    // Pre-test
    // - start a webserver to listen to webhook messages
    // - set all configs to true
    // 
    // Chat message
    // - send a message to the server
    // - assert content and source (player)
    //
    // Game messages
    // - gain an advancement & assert content and source (server)
    // - die & assert content and source (server)
    //
    // Command messages
    // - /give & assert content and source (server)
    // - /gamerule sendCommandFeedback false & assert no message sent
    //   (note: this feature isn't even working in the first place. to fix)
    // - /say & assert content and source (player)
    // - /tellraw & assert content and source (player)
    //
    // Config
    // - change each config value to false in turn and re-run all the above tests
    //   for each case, asserting messages sent only for the other message types
    // - assert that config file is saves properly

    @GameTest
	public void test( TestContext context ) throws java.io.IOException
    {
        
	}

	@Override
	public void invokeTestMethod( TestContext context, Method method ) throws ReflectiveOperationException
    {
		
	}
}