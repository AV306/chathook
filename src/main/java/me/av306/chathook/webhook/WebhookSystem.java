package me.av306.chathook.webhook;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;

public enum WebhookSystem
{
    INSTANCE;
    
    private final URI WEBHOOK_URI;

    private WebhookSystem()
    {
        this.WEBHOOK_URI = URI.create( this.readSecretWebhookUri() );
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
}
