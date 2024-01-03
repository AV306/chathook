package me.av306.chathook.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.util.Hashtable;

import me.av306.chathook.minecraft.ChatHook;

// TODO: we can reuse this elsewjere
public class ConfigManager
{
    private final Hashtable<String, String> config = new Hashtable<>();

    private final String configFilePath;
    
    public ConfigManager( String configFilePath )
    {
        this.configFilePath = configFilePath;
        this.readConfigFile();
    }

    private void readConfigFile()
    {
        try ( BufferedReader reader = new BufferedReader( new FileReader( this.configFilePath ) ) )
        {
            // Iterate over each line in the file
            for ( String line : reader.lines().toArray( String[]::new ) )
            {
                if ( line.startsWith( "#" ) ) continue;
                
                // Split it by the equals sign (.properties format)
                String[] entry = line.split( "=" );
                try
                {
                    // Try adding the entry into the hashmap
                    this.config.put( entry[0].trim(), entry[1].trim() );
                }
                catch ( ArrayOutOfBoundsException oobe )
                {
                    // Catch an out-of-bounds when an incomplete line is found
                    ChatHook.INSTANCE.LOGGER.error( "Invalid config line: {}", line );
                }
            }
        }
        catch ( IOException ioe )
        {
            ChatHook.INSTANCE.LOGGER.error( "IOException while reading config file: {}", ioe.getMessage() );
        }
    }

    public void saveConfigFile()
    {
        // TODO: read the existing configs to a single string, then replace the config entries
        //       with th ipdated entries and then write the entire thing to the file

        // Let's just overwrite the entire file
        try ( BufferedWriter writer = new BufferedWriter( new FileWriter( this.configFilePath ) ) )
        {
            StringBuilder builder = new StringBuilder( "# Please note: This file will be overwritten every time ChatHook restarts.\n" );

            for ( String key : this.config.keys() )
            {
                builder.append( key )
                        .append( '=' )
                        .append( this.config.get( key ) )
                        .append( '\n' );
            }

            writer.write( builder.toString(), 0, builder.length() );
        }
    }

    
    public String getConfig( String name )
    {
        return this.config.get( name );
    }

    public String setConfig( String name, String value )
    {
        return this.config.put( name, value );
    }
}