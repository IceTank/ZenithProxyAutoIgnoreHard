package org.example.module;

import com.github.rfresh2.EventConsumer;
import com.zenith.Proxy;
import com.zenith.event.chat.DeathMessageChatEvent;
import com.zenith.event.chat.PublicChatEvent;
import com.zenith.event.chat.WhisperChatEvent;
import com.zenith.feature.deathmessages.DeathMessageParseResult;
import com.zenith.module.api.Module;
import com.zenith.network.server.ServerSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.example.AutoIgnoreHardPlugin;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.rfresh2.EventConsumer.of;

public class AutoIgnoreHardModule extends Module {
    @Override
    public boolean enabledSetting() {
        return AutoIgnoreHardPlugin.PLUGIN_CONFIG.autoIgnoreHardModule.enabled;
    }

    @Override
    public List<EventConsumer<?>> registerEvents() {
        return List.of(
                of(PublicChatEvent.class, this::handlePublicChat),
                of(WhisperChatEvent.class, this::handleWhisperChatEvent),
                of(DeathMessageChatEvent.class, this::handleDeathMessageChatEvent)
        );
    }

    private void handlePublicChat(PublicChatEvent chatEvent) {
        String username = chatEvent.sender().getName();
        String message = chatEvent.message();
        handleChatMessage(username, message);
    }

    private void handleWhisperChatEvent(WhisperChatEvent chatEvent) {
        String username = chatEvent.sender().getName();
        String message = chatEvent.message();
        handleChatMessage(username, message);
    }

    private void handleDeathMessageChatEvent(DeathMessageChatEvent chatEvent) {
        DeathMessageParseResult parseResult = chatEvent.deathMessage();

        handleChatMessage(parseResult.victim(), chatEvent.message());
    }

    private boolean hasSpamTrigger(String content) {
        return content.toLowerCase().contains("gg/") || content.toLowerCase().contains(".com/invite/");
    }

    private void handleChatMessage(String username, String message) {
        if (AutoIgnoreHardPlugin.PLUGIN_CONFIG.autoIgnoreHardModule.allowList.contains(username.toLowerCase())) {
            return;
        }
        if (false && !Proxy.getInstance().isOn2b2t()) {
            return;
        }
        if (hasSpamTrigger(message)) {
            String command = "/ignorehard " + username;
            sendClientPacketAsync(new ServerboundChatPacket(command));

            if (AutoIgnoreHardPlugin.PLUGIN_CONFIG.autoIgnoreHardModule.logToFile) {
                logMessageToFile(username, message);
            }
            if (AutoIgnoreHardPlugin.PLUGIN_CONFIG.autoIgnoreHardModule.logToChat) {
                logMessageToChat(username, message, command);
            }
        }
    }

    private void logMessageToFile(String username, String message) {
        Path path = Path.of("plugins/AutoIgnoreHard/ignored.log");
        String timestamp = LocalDateTime.now().toString();
        try {
            try {
                Files.createDirectory(path.getParent());
            } catch (FileAlreadyExistsException ignored) {
            }
            Files.writeString(path, String.format("%s %s: %s%n", timestamp, username, message), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            AutoIgnoreHardPlugin.LOG.error("Could not create log file!", e);
        }
    }

    private void logMessageToChat(String username, String message, String revertCommand) {
        Component revertAction = Component.text("\n[Revert ignore]").color(NamedTextColor.GRAY)
                .clickEvent(ClickEvent.runCommand(revertCommand))
                .hoverEvent(HoverEvent.showText(Component.text("Click to revert ignore")));

        Component finalMessage = Component.text("Now ignoring '%s' for message: %s".formatted(username, message))
                .color(NamedTextColor.GRAY).append(revertAction);

        for (ServerSession session : Proxy.getInstance().getActiveConnections().getArray()) {
            session.sendAsync(new ClientboundSystemChatPacket(finalMessage, false));
        }
    }
}
