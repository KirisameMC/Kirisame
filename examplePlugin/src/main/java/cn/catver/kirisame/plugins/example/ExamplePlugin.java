package cn.catver.kirisame.plugins.example;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Difficulty;
import org.kirisame.mc.KirisameMC;
import org.kirisame.mc.api.plugin.KirisamePlugin;
import org.kirisame.mc.console.message.impl.player.PlayerJoinMessage;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.EventHandler;
import org.kirisame.mc.event.impl.ConsoleMessageEvent;
import org.kirisame.mc.event.impl.reflect.ChatMessageEvent;
import org.kirisame.mc.event.impl.reflect.PlayerJoinEvent;
import org.kirisame.mc.event.impl.reflect.TickEvent;

public class ExamplePlugin extends KirisamePlugin {
    static DedicatedServer server;

    @Override
    public void onLoad(KirisameMC kirisameMC) {
        EventBus.register(this);
        server = (DedicatedServer) kirisameMC.getServer();

        server.getCommands().getDispatcher().register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("example")
                        .requires(stack->!stack.isPlayer())
                        .executes(context -> {
                            if (!context.getSource().isPlayer()){
                                log$info("You are successfully run a command register by Kirisame!");
                            }
                            return 1;
                        })
        );

        server.getCommands().getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal("suicide")
                .requires(CommandSourceStack::isPlayer)
                .executes(ctx->{
                    ServerPlayer player = (ServerPlayer) ctx.getSource().getEntity();
                    assert player != null;
                    kirisameMC.getMinecraftWrapper().getCommandWrapper().execute("kill "+player.getPlainTextName());
                    return 1;
                }));

        new TpaImpl().init(kirisameMC);

        log$info("Example Plugin Loading...");
    }

    @EventHandler
    public void consoleMessage(ConsoleMessageEvent event){
        server.execute(()->{
            if (event.getMessage().getContent() instanceof PlayerJoinMessage message){
                log$info("Player {} has joined!", message.getPlayerName());
                log$info(server.getPlayerList().getPlayers().toString());
                ServerPlayer player = server.getPlayerList().getPlayer(message.getPlayerName());
                if (player != null){
                    log$info("Player {} ip address is {}", player.getStringUUID(),player.getIpAddress());
                    player.sendSystemMessage(Component.literal("This is server is using Kirisame!"),true);
                }
            }
        });
    }

    @EventHandler
    public void _player_join(PlayerJoinEvent event){
        ServerPlayer serverPlayer = (ServerPlayer) event.getServerPlayer();

        serverPlayer.connection.send(new ClientboundChangeDifficultyPacket(Difficulty.PEACEFUL, false));
    }

    @EventHandler
    public void _player_chat(ChatMessageEvent event){
        ServerGamePacketListenerImpl connection = (ServerGamePacketListenerImpl) event.getConnection();
        PlayerChatMessage message = (PlayerChatMessage) event.getMessage();

        log$info("Player {} has sent {}.",connection.getPlayer().getName().getString(),message.decoratedContent().getString());
    }

    long steppedTick = 0;
    @EventHandler
    public void _tick(TickEvent event){
        steppedTick++;
    }

    @Override
    public void onUnload(KirisameMC kirisameMC) {
        log$info("SteppedTick = {}",steppedTick);
    }
}
