package me.av306.chathook;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

import me.av306.chathook.config.Configurations;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.av306.chathook.webhook.WebhookSystem;
import me.av306.chathook.config.ConfigManager;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.server.command.CommandManager;
import static net.minecraft.server.command.CommandManager.*;
import net.minecraft.server.command.ServerCommandSource;

import java.io.IOException;
import java.util.Objects;

public class ChatHook implements DedicatedServerModInitializer
{
    public static final String PLAYER_JOINED_MESSAGE_FORMAT = "**%s** joined the game (%d/%d online)";
    public static final String PLAYER_DISCONNECTED_MESSAGE_FORMAT = "**%s** left the game* (%d/%d online)";
    public static final String SERVER_STARTED_MESSAGE_FORMAT = "Server started!";
    public static final String SERVER_STOPPED_MESSAGE_FORMAT = "Server stopped.";
    private static ChatHook INSTANCE;
    public static ChatHook getInstance() { return INSTANCE; };

    public final String MODID = "chathook";

    public final Logger LOGGER = LoggerFactory.getLogger( "ChatHook" );

    public ConfigManager configManager = null;

    @Override
    public void onInitializeServer()
    {
        INSTANCE = this;

        try
        {
            configManager = new ConfigManager( this.MODID, FabricLoader.getInstance().getConfigDir(),
                    "chathook.properties", Configurations.class, null );
        }
        catch ( IOException e )
        {
            // Honestly, we can do nothing, since ChatHook handles all the logging.

        }

        configManager.printAllConfigs();

        // Register events
        this.registerEvents();

        // Register commands
        CommandRegistrationCallback.EVENT.register( this::registerCommands );

        LOGGER.info( "[ChatHook] ChatHook initialized!" );
    }

    private void registerEvents()
    {
        // Chat messages
        ServerMessageEvents.CHAT_MESSAGE.register(
            (signedMessage, sender, params) ->
            {
                if ( Configurations.ENABLED && Configurations.LOG_CHAT_MESSAGES )
                    WebhookSystem.INSTANCE.sendWebhookMessage( sender, signedMessage.getSignedContent() );
            }
        );

        // Command feedback messages are handled in ServerCommandSourceMixin

        // I'm opting for this more general event, since it catches advancements, death, join/leave etc by default.
        // IMO a player count when join/leave messages are sent isn't important enough to justify
        // handling all game messages separately.
        ServerMessageEvents.GAME_MESSAGE.register( (server, messageText, overlay) ->
        {
            LOGGER.info( messageText.getString() );
            if ( Configurations.ENABLED && Configurations.LOG_GAME_MESSAGES )
            {
                String messageString;
                if ( overlay ) messageString = "> " + messageText.getString();
                else messageString = messageText.getString();

                WebhookSystem.INSTANCE.sendWebhookMessage( null, messageString, true );
            }
        } );

        // Player joined the server
        /*ServerPlayConnectionEvents.JOIN.register(
                (sender, player, hand) -> {
                    if ( Configurations.ENABLED && Configurations.LOG_GAME_MESSAGES )
                    {
                        WebhookSystem.INSTANCE.sendWebhookMessage( null,
                                String.format(
                                        PLAYER_JOINED_MESSAGE_FORMAT,
                                        sender.player.getName().getString(),
                                        Objects.requireNonNull( sender.player.getServer() ).getCurrentPlayerCount() + 1,
                                        sender.player.getServer().getMaxPlayerCount()
                                )
                        );
                    }
                }
        );*/

        // Player left the server
        /*ServerPlayConnectionEvents.DISCONNECT.register(
                (sender, player) -> {
                    if ( Configurations.ENABLED && Configurations.LOG_GAME_MESSAGES )
                    {
                        WebhookSystem.INSTANCE.sendWebhookMessage( null,
                                String.format(
                                        PLAYER_DISCONNECTED_MESSAGE_FORMAT,
                                        sender.player.getName().getString(),
                                        Objects.requireNonNull( sender.player.getServer() ).getCurrentPlayerCount() - 1,
                                        sender.player.getServer().getMaxPlayerCount()
                                )
                        );
                    }
                }
        );*/

        // Command messages (broadcasts, e.g. /me, /say only)
        ServerMessageEvents.COMMAND_MESSAGE.register(
            (message, source, params) ->
            {
                LOGGER.info( message.getSignedContent() );
                if ( Configurations.ENABLED && Configurations.LOG_COMMAND_MESSAGES )
                    WebhookSystem.INSTANCE.sendWebhookMessage( source.getPlayer(), message.getSignedContent() );
            }
        );

        // Server startup process finished
        ServerLifecycleEvents.SERVER_STARTED.register(
                server -> WebhookSystem.INSTANCE.sendWebhookMessage( null, SERVER_STARTED_MESSAGE_FORMAT )
        );

        // Server stop message
        ServerLifecycleEvents.SERVER_STOPPED.register(
                server -> WebhookSystem.INSTANCE.sendWebhookMessage( null, SERVER_STOPPED_MESSAGE_FORMAT, false )
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
                        Configurations.ENABLED,
                        Configurations.LOG_CHAT_MESSAGES,
                        Configurations.LOG_GAME_MESSAGES,
                        Configurations.LOG_COMMAND_MESSAGES
                    ), false );
                    return 1;
                } )

                // Enable
                .then( literal( "enable" ).executes( context ->
                {
                    if ( !Configurations.ENABLED )
                    {
                        context.getSource().sendFeedback( () -> Text.translatable(
                                "commands.chathook.enabled" ), false );
                        Configurations.ENABLED = true;
                    }
                    else context.getSource().sendFeedback( () -> Text.translatable(
                            "commands.chathook.already_enabled" ), false );

                    return 1;
                } ) )

                // Disable
                .then( literal( "disable" ).executes( context ->
                {
                    if ( Configurations.ENABLED )
                    {
                        context.getSource().sendFeedback( () -> Text.translatable(
                                "commands.chathook.disabled" ), false );
                        Configurations.ENABLED = (false );
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
                                    Configurations.LOG_CHAT_MESSAGES
                            ), false );
                            return 1;
                        } )

                        // Log chat enable
                        .then( literal("enable") .executes( context ->
                        {
                            if ( !Configurations.LOG_CHAT_MESSAGES )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logChat.enabled" ), false );
                                Configurations.LOG_CHAT_MESSAGES = true;
                            }
                            else context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logChat.already_enabled" ), false );

                            return 1;
                        } ) )

                        // Log chat disable
                        .then( literal("disable") .executes( context ->
                        {
                            if ( Configurations.LOG_CHAT_MESSAGES )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logChat.disabled" ), false );
                                Configurations.LOG_CHAT_MESSAGES = false;
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
                                    Configurations.LOG_GAME_MESSAGES
                            ), false );
                            return 1;
                        } )

                        // Log game enable
                        .then( literal("enable") .executes( context ->
                        {
                            if ( !Configurations.LOG_GAME_MESSAGES )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logGame.enabled" ), false );
                                Configurations.LOG_GAME_MESSAGES = true;
                            }
                            else context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logGame.already_enabled" ), false );

                            return 1;
                        } ) )

                        // Log game disable
                        .then( literal("disable") .executes( context ->
                        {
                            if ( Configurations.LOG_GAME_MESSAGES )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logGame.disabled" ), false );
                                Configurations.LOG_GAME_MESSAGES = false;
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
                                    Configurations.LOG_COMMAND_MESSAGES
                            ), false );
                            return 1;
                        } )

                        // Log command message enable
                        .then( literal("enable") .executes( context ->
                        {
                            if ( !Configurations.LOG_COMMAND_MESSAGES )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logCommand.enabled" ), false );
                                Configurations.LOG_COMMAND_MESSAGES = true;
                            }
                            else context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.logCommand.already_enabled" ), false );

                            return 1;
                        } ) )

                        // Log command message disable
                        .then( literal("disable") .executes( context ->
                        {
                            if ( Configurations.LOG_COMMAND_MESSAGES )
                            {
                                context.getSource().sendFeedback( () -> Text.translatable(
                                        "commands.chathook.logCommand.disabled" ), false );
                                Configurations.LOG_COMMAND_MESSAGES = false;
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
                                    Configurations.WEBHOOK_URL
                            ), false );
                            return 1;
                        } )

                        // Set new webhook url
                        .then( argument("url", greedyString() ) .executes(context ->
                        {
                            Configurations.WEBHOOK_URL = (StringArgumentType.getString(context, "url"));
                            context.getSource().sendFeedback( () -> Text.translatable(
                                    "commands.chathook.webhook.update",
                                    Configurations.WEBHOOK_URL
                            ), false );

                            return 1;
                        } ) )
                )

        );
    }
}
