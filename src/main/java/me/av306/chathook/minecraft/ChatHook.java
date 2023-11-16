package me.av306.chathook.minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.av306.chathook.webhook.WebhookSystem;

public enum ChatHook
{
    INSTANCE;

    public final String MODID = "chathook";

    public final Logger LOGGER = LoggerFactory.getLogger( this.MODID );

    public void initialise()
    {
        WebhookSystem.INSTANCE.sendMessage( "Server", "ChatHook started" );
    }
}
