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

public enum WebhookSystem
{
    INSTANCE;
    
    private final URI WEBHOOK_URI;

    private final HttpClient client;

    private final String WEBHOOK_POST_DATA = "{\"username\": \"%s\", \"content\": \"%s\"}";

    private WebhookSystem()
    {
        this.WEBHOOK_URI = URI.create( this.readSecretWebhookUri() );

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
                String[] entry = line.trim().split( "=" );
                if ( entry[0].equals( "webhook_url" ) ) return entry[1];
            }
            return "error";
        }
        catch ( ArrayIndexOutOfBoundsException oobe )
        {

            return "error";
        }
        catch ( IOException ioe )
        {
            return "error";
        }
    }

    public void sendMessage( String username, String message )
    {
        // POST message to webhook
        HttpRequest req = HttpRequest.newBuilder( this.WEBHOOK_URI )
                .POST(
                    BodyPublishers.ofString( String.format( this.WEBHOOK_POST_DATA, username, message ) )
                )
                .build();

        this.client.sendAsync( req, BodyHandlers.discarding() );
    }
}
