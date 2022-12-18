package com.velocitypowered.proxy;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Ruf
 * @since 2022-12-16
 */
public class ManualInjectionPluginModule implements Module {

    private final Class<?> clazz;
    private final PluginContainer container;

    public ManualInjectionPluginModule(Class<?>clazz, PluginContainer container) {
        this.clazz = clazz;
        this.container = container;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(clazz).in(Scopes.SINGLETON);
        binder.bind(Logger.class).toInstance(LoggerFactory.getLogger(container.getDescription().getId()));
        // TODO What is this for and do we need it?
        //binder.bind(Path.class).annotatedWith(DataDirectory.class).toInstance(this.basePluginPath.resolve(this.description.getId()));
        binder.bind(PluginDescription.class).toInstance(container.getDescription());
        binder.bind(PluginContainer.class).toInstance(container);
    }
}
