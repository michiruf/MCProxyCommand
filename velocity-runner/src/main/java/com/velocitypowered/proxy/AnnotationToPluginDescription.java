package com.velocitypowered.proxy;

import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.meta.PluginDependency;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author Michael Ruf
 * @since 2022-12-16
 */
public class AnnotationToPluginDescription implements PluginDescription {

    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final String url;
    private final String[] authors;
    private final Dependency[] dependencies;

    public AnnotationToPluginDescription(Plugin plugin) {
        this.id = plugin.id();
        this.name = plugin.name();
        this.version = plugin.version();
        this.description = plugin.description();
        this.url = plugin.url();
        this.authors = plugin.authors();
        this.dependencies = plugin.dependencies();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(name);
    }

    @Override
    public Optional<String> getVersion() {
        return Optional.of(version);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of(description);
    }

    @Override
    public Optional<String> getUrl() {
        return Optional.of(url);
    }

    @Override
    public List<String> getAuthors() {
        return Arrays.stream(authors).toList();
    }

    @Override
    public Collection<PluginDependency> getDependencies() {
        // TODO Not supported
        return PluginDescription.super.getDependencies();
    }

    @Override
    public Optional<PluginDependency> getDependency(String id) {
        // TODO Not supported
        return PluginDescription.super.getDependency(id);
    }

    @Override
    public Optional<Path> getSource() {
        // TODO Not supported
        return PluginDescription.super.getSource();
    }
}
