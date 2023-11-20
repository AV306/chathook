package me.av306.chathook.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;

import me.av306.chathook.minecraft.ChatHook;

public enum ConfigManager
{
    INSTANCE;
    
    public final HashTable<String, String> configs = new HashTable<>();


    private ConfigManager()
    {
    }

    private void readConfigFile()
    {
        try ( BufferedReader reader = new BufferedReader( new FileReader( "./chathook_config.congif" ) ) )
        {
            for ( String line : reader.lines().toArray( String[]::new ) )
            {
                String[] entry = line.split( "=" );
                try
                {
                    this.config.put( entry[0].trim(), entry[1].trim() );
                }
                catch ( ArrayOutOfBoundsException oobe )
                {
                    ChatHook.INSTANCE.LOGGER.error( "Invalid config line: {}", line );
                }
            }
        }
        catch ( IOException ioe )
        {
            ChatHook.INSTANCE.LOGGER.error( "IOException: {}", ioe.getMessage() );
        }
    }
}
