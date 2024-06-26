package me.av306.chathook.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import me.av306.chathook.minecraft.ChatHook;

// TODO: we can reuse this elsewhere
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
                catch ( IndexOutOfBoundsException oobe )
                {
                    // Catch an out-of-bounds when an incomplete line is found
                    if (ChatHook.INSTANCE != null)
                        ChatHook.INSTANCE.LOGGER.error( "Invalid config line: {}", line );
                    else
                        System.out.println( "Invalid config line: {}".formatted( line ) );
                }

            }
        }
        catch ( IOException ioe )
        {
            // INSTANCE potentially null?
            if (ChatHook.INSTANCE != null)
                ChatHook.INSTANCE.LOGGER.error( "IOException while reading config file: {}", ioe.getMessage() );
            else
                System.out.println( "IOException while reading config file: {}".formatted( ioe.getMessage()) );
        }
    }

    public void saveConfigFile()
    {
        // TODO: read the existing configs to a single string, then replace the config entries
        //       with the updated entries and then write the entire thing to the file

        // Let's just overwrite the entire file
        try ( BufferedWriter writer = new BufferedWriter( new FileWriter( this.configFilePath ) ) )
        {
            StringBuilder builder = new StringBuilder( "# Please note: This file will be overwritten every time ChatHook restarts.\n" );

            for ( String key : this.config.values() )
            {
                builder.append( key )
                        .append( '=' )
                        .append( this.config.get( key ) )
                        .append( '\n' );
            }

            writer.write( builder.toString(), 0, builder.length() );
        } catch (IOException ioe) {
            ChatHook.INSTANCE.LOGGER.error( "IOException while writing config file: {}", ioe.getMessage() );
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