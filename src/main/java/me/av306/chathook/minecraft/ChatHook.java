package me.av306.chathook.minecraft;

import com.mojang.brigadier.CommandDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.av306.chathook.webhook.WebhookSystem;
import me.av306.chathook.config.ConfigManager;

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

public enum ChatHook
{
    INSTANCE;

    public final String MODID = "chathook";

    public final Logger LOGGER = LoggerFactory.getLogger( this.MODID );

    public final ConfigManager configManager = new ConfigManager(
        FabricLoader.getInstance().getGameDir().resolveSibling( "chathook.properties" ).toString()
    );

    public boolean enabled = true;
    public boolean logChatMessages = true;
    public boolean logGameMessages = true;
    public boolean logCommandMessages = true;


    public void initialise()
    {
        configManager.initialConfigFile();

        // Initialise flags
        this.enabled = Boolean.parseBoolean( configManager.getConfig( "enabled" ) );
        this.logChatMessages = Boolean.parseBoolean( configManager.getConfig( "log_chat" ) );
        this.logGameMessages = Boolean.parseBoolean( configManager.getConfig( "log_game_messages" ) );
        this.logCommandMessages = Boolean.parseBoolean( configManager.getConfig( "log_command_messages" ) );

        // Register events
        this.registerEvents();

        // Register commands
        CommandRegistrationCallback.EVENT.register( this::registerCommands );
    }

    private void registerEvents()
    {
        // Chat messages
        ServerMessageEvents.CHAT_MESSAGE.register(
            (signedMessage, sender, params) ->
            {
                if ( this.logChatMessages&& this.enabled )
                    WebhookSystem.INSTANCE.sendMessage( sender, signedMessage.getSignedContent() );
            }
        );

        // Player joined the server
        ServerPlayConnectionEvents.JOIN.register(
                (sender, player, hand) -> {
                    if ( this.logGameMessages && this.enabled )
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
                    if ( this.logGameMessages && this.enabled )
                        WebhookSystem.INSTANCE.sendMessage( sender.player,
                                String.format("**%s left the game** %d/%d",
                                        sender.player.getName().getString(),
                                        sender.player.server.getCurrentPlayerCount() - 1,
                                        sender.player.server.getMaxPlayerCount()) );
                }
        );

        // Command messages
        // It does work, it just posts messages originating from e.g. '/say message'
        ServerMessageEvents.COMMAND_MESSAGE.register(
            (message, source, params) ->
            {
                if ( this.logCommandMessages && this.enabled )
                    WebhookSystem.INSTANCE.sendMessage( null, message.getSignedContent() );
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
            (server) -> WebhookSystem.INSTANCE.sendMessage( null, "Server stopped." )
        );
    }

    private void registerCommands(
        CommandDispatcher<ServerCommandSource> dispatcher,
        CommandRegistryAccess registryAccess,
        CommandManager.RegistrationEnvironment environment )
    {
        // Root command
        dispatcher.register(
            literal( "chathook" )
                    // only L4 ops
                    .requires( source -> source.hasPermissionLevel( 4 ) )

                    // Basic command
                    // mega line
                    .executes( context ->
                    {
                        context.getSource().sendFeedback( () -> Text.translatable(
                            "text.chathook.status",
                            this.enabled,
                            this.logChatMessages,
                            this.logGameMessages,
                            this.logCommandMessages
                        ), false );
                        return 1;
                    } )

                    // Enable
                    .then( literal( "enable" ).executes( context ->
                    {
                        if ( !this.enabled )
                        {
                            context.getSource().sendFeedback( () -> Text.translatable( "text.chathook.enabled" ), false );
                            this.enabled = true;
                        }
                        else context.getSource().sendFeedback( () -> Text.translatable( "text.chathook.already_enabled" ), false );

                        return 1;
                    } ) )

                    // Disable
                    .then( literal( "disable" ).executes( context ->
                    {
                        if ( this.enabled )
                        {
                            context.getSource().sendFeedback( () -> Text.translatable( "text.chathook.disabled" ), false );
                            this.enabled = false;
                        }
                        else context.getSource().sendFeedback( () -> Text.translatable( "text.chathook.already_disabled" ), false );

                        return 1;
                    } ) )

                    // TODO: individual settings
        );
    }
}
