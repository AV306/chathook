package me.av306.chathook.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;

import me.av306.chathook.minecraft.ChatHook;

// TODO: we can reuse this elsewjere
public class ConfigManager
{
    private final HashTable<String, String> config = new HashTable<>();

    private final String configFilePath;
    
    private ConfigManager( String configFilePath )
    {
        this.configFilePath = configFilePath;
        this.readConfigFile();
    }

    private void readConfigFile()
    {
        try ( BufferedReader reader = new BufferedReader( new FileReader( this.configFilePath ) ) )
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

    public void saveConfigFile()
    {
        // TODO: read the existing configs to a single string, then replace the config entries
        //       with th ipdated entries and then write the entire thing to the file
    }
}
