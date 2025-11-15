package cn.catver.kirisame.plugins.example;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import lombok.SneakyThrows;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickAction;
import org.kirisame.mc.KirisameMC;
import org.kirisame.mc.event.EventBus;
import org.kirisame.mc.event.EventHandler;
import org.kirisame.mc.event.impl.KirisameLoopEvent;

import java.awt.*;
import java.util.HashMap;

public class TpaImpl {

    HashMap<String,TpaData> tpa_list = new HashMap<>();

    @SneakyThrows
    public void init(KirisameMC km){
        DedicatedServer server = (DedicatedServer) km.getMinecraftInstance().getMinecraftServer();
        server.getCommands().getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>
                literal("tpa")
                .then(Commands.argument("target", EntityArgument.player())
                        .requires(CommandSourceStack::isPlayer)
                        .executes(ctx->{
                            ServerPlayer player = (ServerPlayer) ctx.getSource().getEntity();
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");

                            assert player != null;

                            if (player.is(target)){
                                return 1;
                            }

                            TpaData data = new TpaData(player, target, System.currentTimeMillis() + 60 * 1000);

                            if (tpa_list.containsKey(data.lookup())){
                                ctx.getSource().sendFailure(Component.literal("You have sent a request."));
                                return 1;
                            }

                            ctx.getSource().sendSuccess(()->
                                    Component.literal("Request sent to " + target.getPlainTextName()),false);

                            tpa_list.put(data.lookup(), data);
                            target.sendSystemMessage(Component.literal("You have received a request from " + player.getPlainTextName()).append(
                                    Component.literal(" [Accept]").withColor(Color.GREEN.getRGB()).withStyle(style -> style.withClickEvent(
                                            new ClickEvent.RunCommand("/tpaccept " + player.getPlainTextName())
                                    ))
                            ));

                            return 1;
                        })));

        server.getCommands().getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>
                        literal("tpaccept")
                        .then(Commands.argument("target", EntityArgument.player())
                .requires(CommandSourceStack::isPlayer)
                .executes(ctx->{
                    ServerPlayer target = (ServerPlayer) ctx.getSource().getEntity();
                    ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
                    assert target != null;

                    if (target.is(player)){
                        return 1;
                    }

                    if (tpa_list.containsKey(player.getStringUUID()+"->"+target.getStringUUID())){
                        tpa_list.remove(player.getStringUUID()+"->"+target.getStringUUID());
                        player.teleportTo(target.getX(), target.getY(), target.getZ());
                        player.sendSystemMessage(Component.literal("You have been teleported to " + target.getPlainTextName()));
                        ctx.getSource().sendSuccess(()->
                                Component.literal("You are accepted the request from "+player.getPlainTextName()),false);
                    }else {
                        ctx.getSource().sendFailure(Component.literal("You didn't have a request from "+player.getPlainTextName()));
                    }
                    return 1;
                })));

        server.getPlayerList().getPlayers().forEach(server.getCommands()::sendCommands);

        EventBus.register(this);
    }

    private boolean isOffline(ServerPlayer player){
        return player == null || ExamplePlugin.server.getPlayerList().getPlayer(player.getUUID()) == null;
    }

    @EventHandler
    @SneakyThrows
    public void onTick(KirisameLoopEvent event){
        for (TpaData data : tpa_list.values()){
            if (isOffline(data.sender) || isOffline(data.receiver)){
                tpa_list.remove(data.lookup());
                continue;
            }
            if (data.isTimeout()){
                tpa_list.remove(data.lookup());
                data.sender.sendSystemMessage(Component.literal("Request timeout"));
                data.receiver.sendSystemMessage(Component.literal("Request from ").append(data.sender.getPlainTextName()).append(" timeout"));
            }
        }
    }

    public record TpaData(ServerPlayer sender, ServerPlayer receiver,long timeout_date){
        public String lookup(){
            return sender.getStringUUID()+"->"+receiver.getStringUUID();
        }

        public boolean isTimeout(){
            return System.currentTimeMillis() > timeout_date;
        }
    }
}
