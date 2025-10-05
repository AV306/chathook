package me.av306.chathook.webhook;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import me.av306.chathook.ChatHook;
import me.av306.chathook.config.Configurations;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public enum WebhookSystem
{
    INSTANCE;
    private final ChatHook chatHook;
    private final HttpClient client;

    WebhookSystem()
    {
        this.chatHook = ChatHook.getInstance();
        this.client = HttpClient.newBuilder()
                .version( Version.HTTP_2 )
                .followRedirects( Redirect.NORMAL )
                .build();
    }

    public void sendWebhookMessage( ServerPlayerEntity player, String message, boolean async )
    {
        URI uri;
        HttpRequest.Builder builder;
        try
        {
            // FIXME: I don't want to create a new URI every time... maybe we can make LiteConfig use annotations and add a callback annotation
            uri = URI.create( Configurations.WEBHOOK_URL );
            builder = HttpRequest.newBuilder( uri );
        }
        catch ( IllegalArgumentException | NullPointerException e )
        {
            chatHook.LOGGER.info( "Invalid webhook url: {}", Configurations.WEBHOOK_URL );
            return;
        }

        // Start a StringBuilder for the body json with the content field filled
        StringBuilder requestBodyBuilder = createWebhookMessageBody( player, message );

        // POST message to webhook
        HttpRequest request = builder.POST( BodyPublishers.ofString( requestBodyBuilder.toString() ) )
                .header( "Content-Type", "application/json" )
                .build();

        if ( async )
        {
            this.client.sendAsync( request, BodyHandlers.ofString() )
                    .thenAccept( this::checkResponse );
        }
        else
        {
            try
            {
                HttpResponse<String> response = this.client.send( request, responseInfo -> null );
                this.checkResponse( response );
            }
            catch ( IOException | InterruptedException e )
            {
                ChatHook.getInstance().LOGGER.warn( "Error while sending webhook message: {}", e.getMessage() );
            }
        }
    }

    private static @NotNull StringBuilder createWebhookMessageBody( ServerPlayerEntity player, String message )
    {
        StringBuilder requestBodyBuilder = new StringBuilder( "{" );

        if ( player != null )
        {
            // Populate avatar_url and name overrides
            requestBodyBuilder.append( String.format( "\"username\": \"%s\",", player.getName().getString() ) );
            requestBodyBuilder.append( String.format( "\"avatar_url\": \"https://visage.surgeplay.com/bust/%s\",", player.getUuid() ) );
        }

        // End the body with the content field
        requestBodyBuilder.append( String.format( "\"content\": \"%s\"}", message ) );
        return requestBodyBuilder;
    }

    private void checkResponse( HttpResponse<String> response )
    {
        if ( response.statusCode() != 204 )
            chatHook.LOGGER.info(
                    "Received unexpected status code {}: {}",
                    response.statusCode(),
                    response.body()
            );
    }

    public void sendWebhookMessage( ServerPlayerEntity player, String message )
    {
        this.sendWebhookMessage( player, message, true );
    }
}
