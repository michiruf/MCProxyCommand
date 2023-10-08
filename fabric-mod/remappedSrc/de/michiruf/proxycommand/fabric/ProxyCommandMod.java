package de.michiruf.proxycommand.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.michiruf.proxycommand.common.ProxyCommandConstants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Ruf
 * @since 2022-12-15
 */
public class ProxyCommandMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("ProxyCommand");

    @Override
    public void onInitialize() {
        LOGGER.info("ProxyCommand is active");
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> registerCommand(dispatcher));
    }

    private static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> proxyCommand = CommandManager
                .literal("proxycommand")
                .requires(cmd -> cmd.hasPermissionLevel(2))
                .then(CommandManager.argument("command", StringArgumentType.string())
                        .executes(ProxyCommandMod::sendMessage)
                        .build())
                .build();
        dispatcher.getRoot().addChild(proxyCommand);
    }

    private static int sendMessage(CommandContext<ServerCommandSource> context) {
        var command = StringArgumentType.getString(context, "command");

        var player = context.getSource().getPlayer();
        if (player == null) {
            LOGGER.warn("Command \"" + command + "\" was executed without the player as source");
            context.getSource().sendMessage(Text.literal("Command source must be a player"));
            return -1;
        }

        // To communicate with the proxy, a S2C packet sent via the players connection is needed (which
        // is the connection to the proxy indeed)
        ServerPlayNetworking.send(
                player,
                new Identifier(ProxyCommandConstants.COMMAND_PACKET_ID),
                PacketByteBufs.create().writeString(command));
        return 1;
    }
}
