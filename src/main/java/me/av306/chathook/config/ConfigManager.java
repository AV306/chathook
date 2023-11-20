package me.av306.chathook.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;

public enum ConfigManager
{
    INSTANCE;
    
    private final HashTable<String, String> configs = new HashTable<>();


    private ConfigManager()
    {
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

    
}
