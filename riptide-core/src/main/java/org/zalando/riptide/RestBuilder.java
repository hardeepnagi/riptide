package org.zalando.riptide;

import com.google.common.collect.ImmutableList;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public final class RestBuilder {

    // package private so we can trick code coverage
    static class Converters {
        private static final ImmutableList<HttpMessageConverter<?>> DEFAULT =
                ImmutableList.copyOf(new RestTemplate().getMessageConverters());
    }

    static class Plugins {
        private static final ImmutableList<Plugin> DEFAULT =
                ImmutableList.of(new OriginalStackTracePlugin());
    }

    private AsyncClientHttpRequestFactory requestFactory;
    private final List<HttpMessageConverter<?>> converters = new ArrayList<>();
    private URI baseUrl;
    private final List<Plugin> plugins = new ArrayList<>();

    RestBuilder() {

    }

    public RestBuilder requestFactory(final AsyncClientHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        return this;
    }

    public RestBuilder defaultConverters() {
        return converters(Converters.DEFAULT);
    }

    public RestBuilder converters(final Iterable<HttpMessageConverter<?>> converters) {
        converters.forEach(this::converter);
        return this;
    }

    public RestBuilder converter(final HttpMessageConverter<?> converter) {
        this.converters.add(converter);
        return this;
    }

    public RestBuilder baseUrl(@Nullable final String baseUrl) {
        return baseUrl(baseUrl == null ? null : URI.create(baseUrl));
    }

    public RestBuilder baseUrl(@Nullable final URI baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public RestBuilder defaultPlugins() {
        return plugins(Plugins.DEFAULT);
    }

    public RestBuilder plugins(final Iterable<Plugin> plugins) {
        plugins.forEach(this::plugin);
        return this;
    }

    public RestBuilder plugin(final Plugin plugin) {
        this.plugins.add(plugin);
        return this;
    }

    public RestBuilder configure(final RestConfigurer configurer) {
        configurer.configure(this);
        return this;
    }

    public static RestConfigurer simpleRequestFactory(final ExecutorService executor) {
        return builder -> {
            final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setTaskExecutor(new ConcurrentTaskExecutor(executor));
            builder.requestFactory(factory);
        };
    }

    public Rest build() {
        return new Rest(requestFactory, converters(), baseUrl, plugin());
    }

    private List<HttpMessageConverter<?>> converters() {
        return converters.isEmpty() ? Converters.DEFAULT : converters;
    }

    private Plugin plugin() {
        return plugins().stream().reduce(Plugin::merge).orElse(NoopPlugin.INSTANCE);
    }

    private List<Plugin> plugins() {
        return plugins.isEmpty() ? Plugins.DEFAULT : plugins;
    }

}
