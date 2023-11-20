package me.av306.chathook.webhook;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

import me.av306.chathook.minecraft.ChatHook;

public enum WebhookSystem
{
    INSTANCE;
    
    private final URI WEBHOOK_URI;

    private final HttpClient client;

    private WebhookSystem()
    {
        ChatHook.INSTANCE.LOGGER.info( "Reading secret..." );
        this.WEBHOOK_URI = URI.create( this.readSecretWebhookUri() );

        ChatHook.INSTANCE.LOGGER.info( "POSTing to: {}", this.WEBHOOK_URI );

        this.client = HttpClient.newBuilder()
                .version( Version.HTTP_2 )
                .followRedirects( Redirect.NORMAL )
                .build();
    }

    private String readSecretWebhookUri()
    {
        try ( BufferedReader reader = new BufferedReader( new FileReader( "./secrets.txt" ) ) )
        {
            for ( String line : reader.lines().toArray( String[]::new ) )
            {
                String[] entry = line.split( "=" );
                if ( entry[0].trim().equals( "webhook_url" ) ) return entry[1].trim();
            }
            return "error";
        }
        catch ( ArrayIndexOutOfBoundsException oobe )
        {
            return "error";
        }
        catch ( IOException ioe )
        {
            ChatHook.INSTANCE.LOGGER.error( "IOException: {}", ioe.getMessage() );
            return "error";
        }
    }

    public void sendMessage( String username, String message )
    {
        // POST message to webhook
        HttpRequest req = HttpRequest.newBuilder( this.WEBHOOK_URI )
                .POST(
                    BodyPublishers.ofString( String.format(
                            "{\"username\": \"%s\", \"content\": \"%s\"}",
                            username,
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
