package me.av306.chathook.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Hashtable;

import me.av306.chathook.minecraft.ChatHook;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigManager
{
    private final Hashtable<String, String> config = new Hashtable<>();
    private final String configFilePath;
    private final ChatHook chatHook;

    public ConfigManager( String configFilePath )
    {
        this.configFilePath = configFilePath;
        this.chatHook = ChatHook.getInstance();
        this.initialConfigFile();
        this.readConfigFile();
    }

    private void readConfigFile()
    {
        try ( BufferedReader reader = new BufferedReader( new FileReader( this.configFilePath ) ) )
        {
            // Iterate over each line in the file
            for ( String line : reader.lines().toArray( String[]::new ) )
            {
                if ( line.startsWith( "#" ) || line.isEmpty() ) continue;

                // Split it by the equals sign (.properties format)
                String[] entry = line.split( "=" );
                try
                {
                    // Try adding the entry into the hashmap
                    this.config.put( entry[0].trim(), entry[1].trim() );
                }
                catch ( IndexOutOfBoundsException oobe )
                {
                    chatHook.LOGGER.error( "Invalid config line: {}", line );
                }

            }
        }
        catch ( IOException ioe )
        {
            chatHook.LOGGER.error( "IOException while reading config file: {}", ioe.getMessage() );
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
            chatHook.LOGGER.error( "IOException while writing config file: {}", ioe.getMessage() );
        }
    }

    public void initialConfigFile() {
        // Create config folder if not exist
        try {
            Files.createDirectories(FabricLoader.getInstance().getGameDir().resolveSibling( "config/"));
        } catch (IOException e) {
            chatHook.LOGGER.error( "IOException while creating config directory: {}", e.getMessage() );
        }
        // Create standard configuration if file does not exist
        File f = new File(this.configFilePath);
        if (!f.exists()) {
            try ( BufferedWriter writer = new BufferedWriter( new FileWriter( this.configFilePath ) ) )
            {
                String string = """ 
                            # +------------------------------------------------+
                            # | ChatHook main config file                      |
                            # |   Modify this file to change ChatHook settings |
                            # +------------------------------------------------+

                            webhook_url=https://discord.com/api/webhooks/[id]/[token]
                            enabled=true
                            log_chat=true
                            log_game_messages=true
                            log_command_messages=true
                            """;

                writer.write( string, 0, string.length() );
            } catch (IOException ioe) {
                chatHook.LOGGER.error( "IOException while writing initial config file: {}", ioe.getMessage() );
            }
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