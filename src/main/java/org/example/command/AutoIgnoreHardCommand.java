package org.example.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.zenith.command.api.Command;
import com.zenith.command.api.CommandCategory;
import com.zenith.command.api.CommandContext;
import com.zenith.command.api.CommandUsage;
import com.zenith.discord.Embed;
import org.example.module.AutoIgnoreHardModule;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.zenith.Globals.MODULE;
import static com.zenith.command.brigadier.ToggleArgumentType.getToggle;
import static com.zenith.command.brigadier.ToggleArgumentType.toggle;
import static org.example.AutoIgnoreHardPlugin.PLUGIN_CONFIG;

public class AutoIgnoreHardCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
                .name("autoIgnoreHard")
                .category(CommandCategory.MODULE)
                .description("""
                        Auto ignores players that spam discord invite links
                        """)
                .usageLines(
                        "on/off",
                        "allowlist <add/del> <username>",
                        "logToChat <on/off>",
                        "logToFile <on/off>"
                )
                .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("autoIgnoreHard")
                .then(argument("toggle", toggle()).executes(c -> {
                    PLUGIN_CONFIG.autoIgnoreHardModule.enabled = getToggle(c, "toggle");
                    // make sure to sync so the module is actually toggled
                    MODULE.get(AutoIgnoreHardModule.class).syncEnabledFromConfig();
                    c.getSource().getEmbed()
                            // if no title is set, no embed response will be sent
                            // other properties like fields can be left unset without issues
                            .title("Example Plugin " + toggleStrCaps(PLUGIN_CONFIG.autoIgnoreHardModule.enabled));
                }))
                .then(literal("allowlist")
                        .then(literal("add").then(argument("username", string()).executes(c -> {
                            String username = getString(c, "username");
                            if (!PLUGIN_CONFIG.autoIgnoreHardModule.allowList.add(username.toLowerCase())) {
                                c.getSource().getEmbed()
                                        .title("Player " + username + " is already on the allow list");
                                return;
                            }

                            c.getSource().getEmbed()
                                    .title("Player " + username + " added to the allow list");
                        })))
                        .then(literal("del").then(argument("username", string()).executes(c -> {
                            String username = getString(c, "username");
                            if (PLUGIN_CONFIG.autoIgnoreHardModule.allowList.remove(username)) {
                                c.getSource().getEmbed()
                                        .title("Player " + username + " is already in the allow list");
                                return;
                            }

                            c.getSource().getEmbed()
                                    .title("Player " + username + " removed from the allow list");
                        })))
                )
                .then(literal("logToFile").then(argument("toggle", toggle()).executes(c -> {
                    PLUGIN_CONFIG.autoIgnoreHardModule.logToFile = getToggle(c, "toggle");

                    c.getSource().getEmbed()
                            .title("Log to File " + toggleStrCaps(PLUGIN_CONFIG.autoIgnoreHardModule.logToFile));
                })))
                .then(literal("logToChat").then(argument("toggle", toggle()).executes(c -> {
                    PLUGIN_CONFIG.autoIgnoreHardModule.logToChat = getToggle(c, "toggle");

                    c.getSource().getEmbed()
                            .title("Log to Chat " + toggleStrCaps(PLUGIN_CONFIG.autoIgnoreHardModule.logToChat));
                })));
    }

    @Override
    public void defaultEmbed(Embed embed) {
        embed
                .primaryColor()
                .addField("Enabled", toggleStr(PLUGIN_CONFIG.autoIgnoreHardModule.enabled))
                .addField("Log to Chat", toggleStr(PLUGIN_CONFIG.autoIgnoreHardModule.logToChat))
                .addField("Log to File", toggleStr(PLUGIN_CONFIG.autoIgnoreHardModule.logToFile))
                .addField("Allow List", String.join(", ", PLUGIN_CONFIG.autoIgnoreHardModule.allowList));
    }
}
