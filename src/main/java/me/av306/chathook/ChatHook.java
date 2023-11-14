package me.av306.chathook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ChatHook
{
    INSTANCE;

    public final String MODID = "chathook";

    public final Logger LOGGER = LoggerFactory.getLogger( this.MODID );

    public void initialise()
    {

    }
}
