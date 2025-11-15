package cn.catver.kirisame.plugins.example;

import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import org.kirisame.mc.KirisameMC;
import org.kirisame.mc.api.KirisamePlugin;
import org.kirisame.mc.api.KirisamePluginInfo;
import org.kirisame.mc.console.message.impl.player.PlayerJoinMessage;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.EventHandler;
import org.kirisame.mc.event.impl.ConsoleMessageEvent;

@KirisamePluginInfo(name = "ExamplePlugin", version = "1.0.0", author = "Hikari", minecraftVersion = "25w45a_unobfuscated")
public class ExamplePlugin implements KirisamePlugin {
    DedicatedServer server;

    @Override
    public void onLoad(KirisameMC kirisameMC) {
        EventBus.register(this);
        server = (DedicatedServer) kirisameMC.getServer();
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
                }
            }
        });
    }
}
