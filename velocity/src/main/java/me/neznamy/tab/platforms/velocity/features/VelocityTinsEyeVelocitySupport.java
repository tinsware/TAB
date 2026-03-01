package me.neznamy.tab.platforms.velocity.features;

import com.velocitypowered.api.event.Subscribe;
import me.neznamy.tab.platforms.velocity.VelocityTAB;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.proxy.ProxySupport;
import me.tins.tinseyevelocity.api.valio.PubSubMessageEvent;
import me.tins.tinseyevelocity.api.valio.TinsEyeVelocityValioAPIProvider;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalInt;

/**
 * TinsEyeVelocity implementation for Velocity.
 * Uses embedded Valio (multi-proxy) when TinsEyeVelocity is loaded with multi-proxy enabled.
 */
public class VelocityTinsEyeVelocitySupport extends ProxySupport {

    @NotNull
    private final VelocityTAB plugin;

    public VelocityTinsEyeVelocitySupport(@NotNull VelocityTAB plugin, @NotNull String channelName) {
        super(channelName);
        this.plugin = plugin;
    }

    @Subscribe
    public void onMessage(PubSubMessageEvent e) {
        if (!e.getChannel().equals(getChannelName())) return;
        processMessage(e.getMessage());
    }

    @Override
    public void register() {
        plugin.getServer().getEventManager().register(plugin, this);
        try {
            var api = TinsEyeVelocityValioAPIProvider.getApi();
            if (api == null) {
                TAB.getInstance().getErrorManager().printError("TinsEyeVelocity API not available (multi-proxy may be disabled)", null);
                return;
            }
            api.registerPubSubChannels(getChannelName());
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().redisBungeeRegisterFail(e);
        }
    }

    @Override
    public void unregister() {
        plugin.getServer().getEventManager().unregisterListener(plugin, this);
        var api = TinsEyeVelocityValioAPIProvider.getApi();
        if (api != null) {
            api.unregisterPubSubChannels(getChannelName());
        }
    }

    @Override
    public void sendMessage(@NotNull String message) {
        try {
            var api = TinsEyeVelocityValioAPIProvider.getApi();
            if (api == null) {
                TAB.getInstance().getErrorManager().printError("TinsEyeVelocity API not available", null);
                return;
            }
            api.sendChannelMessage(getChannelName(), message);
        } catch (Exception e) {
            TAB.getInstance().getErrorManager().redisBungeeMessageSendFail(e);
        }
    }

    @Override
    public OptionalInt getTotalOnlineCount() {
        try {
            var api = TinsEyeVelocityValioAPIProvider.getApi();
            if (api != null) {
                int count = api.getTotalOnlineCount();
                if (count >= 0) return OptionalInt.of(count);
            }
        } catch (Exception ignored) {
        }
        return OptionalInt.empty();
    }
}
