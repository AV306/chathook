package me.av306.chathook.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.av306.chathook.Constants;
import me.av306.chathook.config.Configurations;
import me.av306.chathook.webhook.WebhookSystem;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin( ServerCommandSource.class )
public abstract class ServerCommandSourceMixin
{
    @Shadow
    @Nullable
    public abstract ServerPlayerEntity getPlayer();

    @Shadow
    @Final
    private boolean silent;

    @Inject( method = "sendFeedback", at = @At( "HEAD" ) )
    public void onSendFeedback( Supplier<Text> feedbackSupplier, boolean broadcastToOps, CallbackInfo ci,
                                @Local( ordinal = 0 ) boolean bl )
    {
        // FIXME: This does not respect sendCommandFeedback
        if ( Configurations.ENABLED && Configurations.LOG_COMMAND_MESSAGES && !this.silent )
        {
            WebhookSystem.INSTANCE.sendWebhookMessage(
                    getPlayer(),
                    String.format( Constants.COMMAND_MESSAGE_FORMAT, getPlayer().getName().getString(),
                            feedbackSupplier.get().getString() ),
                    true
            );
        }
    }
}
