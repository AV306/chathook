package me.av306.chathook.minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.av306.chathook.webhook.WebhookSystem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public enum ChatHook
{
    INSTANCE;

    public final String MODID = "chathook";

    public final Logger LOGGER = LoggerFactory.getLogger( this.MODID );

    public boolean logChatMessages = true;
    public boolean logGameMessages = true;
    public boolean logCommandMessages = true;


    public void initialise()
    {
        WebhookSystem.INSTANCE.sendMessage( "Server", "ChatHook started" );

        // Register events
        ServerMessageEvents.CHAT_MESSAGE.register(
            (signedMessage, sender, params) ->
            {
                if ( this.logChatMessages )
                    WebhookSystem.INSTANCE.sendMessage( sender.getEntityName(), signedMessage.getSignedContent() );
            }
        );

        ServerMessageEvents.GAME_MESSAGE.register(
            (server, message, overlay) ->
            {
                if ( this.logGameMessages )
                    WebhookSystem.INSTANCE.sendMessage( "Server", message.getString() );
            }
        );

        ServerMessageEvents.COMMAND_MESSAGE.register(
            (message, source, params) ->
            {
                if ( this.logCommandMessages )
                    WebhookSystem.INSTANCE.sendMessage( source.getName(), message.getSignedContent() );
            }
        );

        ServerLifecycleEvents.SERVER_STOPPING.register(
            (server) -> WebhookSystem.INSTANCE.sendMessage( "Server", "Server stopping" )
        );
    }
}
