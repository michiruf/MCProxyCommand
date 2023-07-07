package com.velocitypowered.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginManager;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.plugin.VelocityPluginManager;
import com.velocitypowered.proxy.plugin.loader.VelocityPluginContainer;
import de.michiruf.proxycommand.velocity.ProxyCommandPlugin;
import io.netty.util.ResourceLeakDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;

/**
 * @author Michael Ruf
 * @since 2022-12-16
 */
public class MyMain {

    private static final Logger logger;


    /**
     * @see Velocity#main(String...)
     */
    public static void main(String[] args) {
        ProxyOptions options = new ProxyOptions(args);
        if (!options.isHelp()) {
            long startTime = System.currentTimeMillis();
            VelocityServer server = new VelocityServer(options);
            loadMyPlugin(server, ProxyCommandPlugin.class);
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.shutdown(false);
            }, "Shutdown thread"));
            double bootTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
            logger.info("Done ({}s)!", (new DecimalFormat("#.##")).format(bootTime));
            server.getConsoleCommandSource().start();
            server.awaitProxyShutdown();
        }
    }
    
    // TODO public to access this from within the velocity source proxy
    public static void loadMyPlugin(VelocityServer server, Class<?> pluginClass) {
        try {
            var pluginManager = (VelocityPluginManager) server.getPluginManager();

            var pluginAnnotation = pluginClass.getDeclaredAnnotation(Plugin.class);
            var pluginContainer = new VelocityPluginContainer(new AnnotationToPluginDescription(pluginAnnotation));
            var plugin = injectAndCreateManually(server, pluginClass, pluginContainer);
            pluginContainer.setInstance(plugin);

            // Register the plugin as well. Why does this have to be private?
            var registerPluginMethod = pluginManager.getClass().getDeclaredMethod("registerPlugin", PluginContainer.class);
            registerPluginMethod.setAccessible(true);
            registerPluginMethod.invoke(pluginManager, pluginContainer);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Object injectAndCreateManually(VelocityServer server, Class<?> pluginClass, PluginContainer container) {
        var pluginManager = (VelocityPluginManager) server.getPluginManager();
        var pluginContainers = pluginManager.getPlugins();

        AbstractModule commonModule = new AbstractModule() {
            protected void configure() {
                this.bind(ProxyServer.class).toInstance(server);
                this.bind(PluginManager.class).toInstance(server.getPluginManager());
                this.bind(EventManager.class).toInstance(server.getEventManager());
                this.bind(CommandManager.class).toInstance(server.getCommandManager());

                for (var container : pluginContainers) {
                    this.bind(PluginContainer.class).annotatedWith(Names.named(container.getDescription().getId())).toInstance(container);
                }
            }
        };

        var pluginModule = new ManualInjectionPluginModule(ProxyCommandPlugin.class, container);
        var modules = new Module[]{pluginModule, commonModule};
        var injector = Guice.createInjector(modules);
        return injector.getInstance(pluginClass);
    }

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        logger = LogManager.getLogger(Velocity.class);
        System.setProperty("java.awt.headless", "true");
        if (System.getProperty("velocity.natives-tmpdir") != null) {
            System.setProperty("io.netty.native.workdir", System.getProperty("velocity.natives-tmpdir"));
        }

        if (System.getProperty("io.netty.leakDetection.level") == null) {
            ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        }
    }
}
