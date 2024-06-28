package me.av306.chathook.webhook;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import me.av306.chathook.minecraft.ChatHook;
import net.minecraft.server.network.ServerPlayerEntity;

public enum WebhookSystem
{
    INSTANCE;

    private final HttpClient client;

    WebhookSystem()
    {
        this.client = HttpClient.newBuilder()
                .version( Version.HTTP_2 )
                .followRedirects( Redirect.NORMAL )
                .build();
    }

    public void sendMessage(ServerPlayerEntity player, String message ) {
        String username = "";
        String userIcon = "";
        if (player != null) {
            username = String.format("\"username\": \"%s\", ", player.getName().getString());
            userIcon = String.format("\"avatar_url\": \"https://visage.surgeplay.com/bust/%s\", ", player.getUuid());
        }

        URI uri;
        HttpRequest.Builder builder;
        try {
            uri = URI.create(String.valueOf(ChatHook.INSTANCE.configManager.getConfig("webhook_url")));
            builder = HttpRequest.newBuilder( uri );
        } catch (IllegalArgumentException | NullPointerException e) {
            ChatHook.INSTANCE.LOGGER.info("Invalid webhook url.");
            return;
        }

        // POST message to webhook
        HttpRequest req = builder.POST(
                    BodyPublishers.ofString( String.format(
                            "{" + username + userIcon + "\"content\": \"%s\"}",
                            message
                    ) )
                )
                .header( "Content-Type", "application/json" )
                .build();

        this.client.sendAsync( req, BodyHandlers.ofString() )
                .thenAccept(
                    (res) ->
                    {
                        if ( res.statusCode() != 204 )
                            ChatHook.INSTANCE.LOGGER.info(
                                    "Received unexpected status code: {}; response body: {}",
                                    res.statusCode(),
                                    res.body()
                            );
                    }
                );

    }
}
