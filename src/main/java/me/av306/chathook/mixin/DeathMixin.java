package me.av306.chathook.mixin;

import me.av306.chathook.minecraft.ChatHook;
import me.av306.chathook.webhook.WebhookSystem;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class DeathMixin {
    // Player death messages
    @Inject(method="onDeath", at=@At("HEAD"))
    private void died(DamageSource damageSource, CallbackInfo info){
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ChatHook chatHook = ChatHook.getInstance();
        if ( chatHook.logGameMessages && chatHook.enabled )
            WebhookSystem.INSTANCE.sendMessage( player, "**" + player.getDamageTracker().getDeathMessage().getString() + "**");
    }
}