package org.example;

import com.zenith.plugin.api.Plugin;
import com.zenith.plugin.api.PluginAPI;
import com.zenith.plugin.api.ZenithProxyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.example.command.AutoIgnoreHardCommand;
import org.example.module.AutoIgnoreHardModule;

@Plugin(
    id = "auto-ignore-hard-plugin",
    version = BuildConstants.VERSION,
    description = "ZenithProxy Auto Ignore Hard Plugin - Auto ignores players that spam discord invite links",
    url = "https://github.com/IceTank/ZenithProxyAutoIgnoreHard",
    authors = {"IceTank"},
    mcVersions = {"*"} // to indicate any MC version: @Plugin(mcVersions = "*")
                            // if you touch packet classes, you almost certainly need to pin to a single mc version
)
public class AutoIgnoreHardPlugin implements ZenithProxyPlugin {
    public static AutoIgnoreHardConfig PLUGIN_CONFIG;
    public static ComponentLogger LOG;

    @Override
    public void onLoad(PluginAPI pluginAPI) {
        LOG = pluginAPI.getLogger();
        LOG.info("Auto Ignore Hard plugin loading...");
        // initialize any configurations before modules or commands might need to read them
        PLUGIN_CONFIG = pluginAPI.registerConfig("auto-ignore-hard-plugin", AutoIgnoreHardConfig.class);
        pluginAPI.registerModule(new AutoIgnoreHardModule());
        pluginAPI.registerCommand(new AutoIgnoreHardCommand());
        LOG.info("Auto Ignore Hard plugin loaded!");
    }
}
