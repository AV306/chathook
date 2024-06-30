package me.av306.chathook;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.av306.chathook.webhook.WebhookSystem;
import me.av306.chathook.config.ConfigManager;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.server.command.CommandManager;
import static net.minecraft.server.command.CommandManager.*;
import net.minecraft.server.command.ServerCommandSource;

public class ChatHook implements ModInitializer
{
    private static ChatHook chatHook;

    public final String MODID = "chathook";

    public final Logger LOGGER = LoggerFactory.getLogger( "ChatHook" );

    public ConfigManager cm = null;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        chatHook = this;

        cm = new ConfigManager(
                FabricLoader.getInstance().getGameDir().resolveSibling( "config/chathook.properties" ).toString()
        );

        // Register events
        this.registerEvents();

        // Register commands
        CommandRegistrationCallback.EVENT.register( this::registerCommands );

        LOGGER.info("ChatHook initialized.");
    }

    public static ChatHook getInstance() {
        return chatHook;
    }

    private void registerEvents()
    {
        // Chat messages
        ServerMessageEvents.CHAT_MESSAGE.register(
            (signedMessage, sender, params) ->
            {
                if ( cm.getBoolConfig("log_chat_messages") && cm.getBoolConfig( "enabled" ) )
                    WebhookSystem.INSTANCE.sendMessage( sender, signedMessage.getSignedContent() );
            }
        );

        // Player joined the server
        ServerPlayConnectionEvents.JOIN.register(
                (sender, player, hand) -> {
                    if ( cm.getBoolConfig("log_game_messages") && cm.getBoolConfig( "enabled" ) )
                        WebhookSystem.INSTANCE.sendMessage( sender.player,
                                String.format("**%s joined the game** %d/%d",
                                        sender.player.getName().getString(),
                                        sender.player.server.getCurrentPlayerCount() + 1,
                                        sender.player.server.getMaxPlayerCount()) );
                }
        );

        // Player left the server
        ServerPlayConnectionEvents.DISCONNECT.register(
                (sender, player) -> {
                    if ( cm.getBoolConfig("log_game_messages") && cm.getBoolConfig( "enabled" ) )
                        WebhookSystem.INSTANCE.sendMessage( sender.player,
                                String.format("**%s left the game** %d/%d",
                                        sender.player.getName().getString(),
                                        sender.player.server.getCurrentPlayerCount() - 1,
                                        sender.player.server.getMaxPlayerCount()) );
                }
        );

        // Command messages
        // It does work, it just posts messages originating from e.g. '/say It's MuffinTime.', '/me was killed by'
        ServerMessageEvents.COMMAND_MESSAGE.register(
            (message, source, params) ->
            {
                if ( cm.getBoolConfig("log_command_messages") && cm.getBoolConfig( "enabled" ) )
                    WebhookSystem.INSTANCE.sendMessage( source.getPlayer(), message.getSignedContent() );
            }
        );

        // Server startup process
        ServerLifecycleEvents.SERVER_STARTING.register(
                (server) -> WebhookSystem.INSTANCE.sendMessage( null, "Server starting." )
        );

        // Server startup process finished
        ServerLifecycleEvents.SERVER_STARTED.register(
                (server) -> WebhookSystem.INSTANCE.sendMessage( null, "Server started." )
        );

        // Server stop message
        ServerLifecycleEvents.SERVER_STOPPED.register(
                (server) -> WebhookSystem.INSTANCE.sendMessage( null, "Server stopped.", false)
        );
    }

    private void registerCommands(
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandRegistryAccess registryAccess,
        CommandManager.RegistrationEnvironment environment )
    {
        // Root command
        dispatcher.register( literal( "chathook" )
                // only L4 ops
                .requires( source -> source.hasPermissionLevel( 4 ) )

                // Basic command
                // mega line
                .executes( context ->
                {
                    context.getSource().sendFeedback( () -> Text.translatable(
                        "commands.chathook.status",
                        cm.getConfig("enabled"),
                        cm.getConfig("log_chat_messages"),
                        cm.getConfig("log_game_messages"),
                        cm.getConfig("log_command_messages")
                    ), false );
                    return 1;
                } )

                // Enable
                .then( literal( "enable" ).executes( context ->
                {
                    if ( !cm.getBoolConfig( "enabled" ) )
                    {
                        context.getSource().sendFeedback( () -> Text.translatable(
                                "commands.chathook.enabled" ), false );
                        cm.setConfig("enabled", true);
                    }
                    else context.getSource().sendFeedback( () -> Text.translatable(
                            "commands.chathook.already_enabled" ), false );

                    return 1;
                } ) )

                // Disable
                .then( literal( "disable" ).executes( context ->
                {
                    if ( cm.getBoolConfig( "enabled" ) )
                    {
                        context.getSource().sendFeedback( () -> Text.translatable(
                                "commands.chathook.disabled" ), false );
                        cm.setConfig( "enabled", false );
                    }
                    else context.getSource().sendFeedback( () -> Text.translatable(
                            "commands.chathook.already_disabled" ), false );

                    return 1;
                } ) )

                // Log chat
                .then( literal( "logChat" )
                        // Log chat status
                        .executes( context ->
                        {
                            context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logChat.status",
                                    cm.getConfig("log_chat_messages")
                            ), false );
                            return 1;
                        } )

                        // Log chat enable
                        .then( literal("enable") .executes( context ->
                        {
                            if ( !cm.getBoolConfig("log_chat_messages") )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logChat.enabled" ), false );
                                cm.setConfig("log_chat_messages", true);
                            }
                            else context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logChat.already_enabled" ), false );

                            return 1;
                        } ) )

                        // Log chat disable
                        .then( literal("disable") .executes( context ->
                        {
                            if ( cm.getBoolConfig("log_chat_messages") )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logChat.disabled" ), false );
                                cm.setConfig("log_chat_messages", false);
                            }
                            else context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logChat.already_disabled" ), false );

                            return 1;
                        } ) )
                )

                // Log game
                .then( literal( "logGame" )
                        // Log game status
                        .executes( context ->
                        {
                            context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logGame.status",
                                    cm.getConfig("log_game_messages")
                            ), false );
                            return 1;
                        } )

                        // Log game enable
                        .then( literal("enable") .executes( context ->
                        {
                            if ( !cm.getBoolConfig("log_game_messages") )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logGame.enabled" ), false );
                                cm.setConfig("log_game_messages", true);
                            }
                            else context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logGame.already_enabled" ), false );

                            return 1;
                        } ) )

                        // Log game disable
                        .then( literal("disable") .executes( context ->
                        {
                            if ( cm.getBoolConfig("log_game_messages") )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logGame.disabled" ), false );
                                cm.setConfig("log_game_messages", false);
                            }
                            else context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logGame.already_disabled" ), false );

                            return 1;
                        } ) )
                )

                // Log command messages
                .then( literal( "logCommand" )
                        // Log command messages status
                        .executes( context ->
                        {
                            context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logCommand.status",
                                    cm.getConfig("log_command_messages")
                            ), false );
                            return 1;
                        } )

                        // Log command message enable
                        .then( literal("enable") .executes( context ->
                        {
                            if ( !cm.getBoolConfig("log_command_messages") )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logCommand.enabled" ), false );
                                cm.setConfig("log_command_messages", true);
                            }
                            else context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logCommand.already_enabled" ), false );

                            return 1;
                        } ) )

                        // Log command message disable
                        .then( literal("disable") .executes( context ->
                        {
                            if ( cm.getBoolConfig("log_command_messages") )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logCommand.disabled" ), false );
                                cm.setConfig("log_command_messages", false);
                            }
                            else context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logCommand.already_disabled" ), false );

                            return 1;
                        } ) )
                )

                // Webhook url
                .then( literal( "webhook" )
                        // Current webhook url
                        .executes( context ->
                        {
                            context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.webhook.status",
                                    cm.getConfig("webhook_url")
                            ), false );
                            return 1;
                        } )

                        // Set new webhook url
                        .then( argument("url", greedyString() ) .executes(context ->
                        {
                            cm.setConfig("webhook_url", StringArgumentType.getString(context, "url"));
                            context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.webhook.update",
                                    cm.getConfig("webhook_url")
                            ), false );

                            return 1;
                        } ) )
                )

        );
    }
}
