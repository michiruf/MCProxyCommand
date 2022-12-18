package de.michiruf.proxycommand.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.michiruf.proxycommand.common.ProxyCommandConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

/**
 * @author Michael Ruf
 * @since 2022-12-15
 */
@Plugin(
        id = "proxy-command",
        name = "ProxyCommand",
        description = "Execute commands on the proxy server from minecraft nodes",
        authors = {"Michael Ruf"}
)
public class ProxyCommandPlugin {

    @Inject
    private Logger logger;
    @Inject
    private ProxyServer server;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Register the channel for the massage to listen on
        server.getChannelRegistrar().register(MinecraftChannelIdentifier.from(ProxyCommandConstants.COMMAND_PACKET_ID));

        logger.info("Loaded ProxyCommandPlugin {}", getClass().getPackage().getImplementationVersion());
    }

    @Subscribe
    private void onPluginMessageEvent(PluginMessageEvent e) {
        if (!ProxyCommandConstants.COMMAND_PACKET_ID.equals(e.getIdentifier().getId())) {
            logger.info("ProxyCommandPlugin got unhandled PluginMessageEvent with id {}", e.getIdentifier().getId());
            return;
        }

        if (!(e.getTarget() instanceof Player))
            logger.info("ProxyCommandPlugin got PluginMessageEvent without a player for id {}", e.getIdentifier().getId());
        var player = (Player) e.getTarget();

        try {
            var data = e.dataAsDataStream();
            // Skip the first byte because it is junk?
            data.skipBytes(1);

            runCommand(player, data.readLine());
        } catch (Exception ex) {
            logger.info("ProxyCommandPlugin got unreadable PluginMessageEvent with data {} ...", e.dataAsDataStream().readLine());
            throw new RuntimeException(ex);
        }

        e.setResult(PluginMessageEvent.ForwardResult.handled());
    }

    /**
     * Got from class VelocityConsole in Velocity module proxy.
     *
     * @param player  Player the command gets executed for
     * @param command The command to execute
     * @see com.velocitypowered.proxy.console.VelocityConsole#runCommand(String)
     */
    protected void runCommand(Player player, String command) {
        try {
            var commandResult = server.getCommandManager().executeAsync(player, command).join();

            if (!commandResult) {
                logger.error("Command \"{}\" could not get executed", command);
                player.sendMessage(Component.translatable("velocity.command.command-does-not-exist", NamedTextColor.RED));
                return;
            }

            logger.info("Command \"{}\" was executed!", command);
        } catch (Exception e) {
            logger.error("An error occurred while running this command.", e);
        }
    }
}
