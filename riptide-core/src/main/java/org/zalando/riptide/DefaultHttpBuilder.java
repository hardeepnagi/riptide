package org.zalando.riptide;

import com.google.common.collect.ImmutableList;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.zalando.riptide.Http.ConfigurationStage;
import org.zalando.riptide.Http.ExecutorStage;
import org.zalando.riptide.Http.FinalStage;
import org.zalando.riptide.Http.RequestFactoryStage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.zalando.riptide.Plugin.composite;

final class DefaultHttpBuilder implements ExecutorStage, RequestFactoryStage, ConfigurationStage, FinalStage {

    // package private so we can trick code coverage
    static class Converters {
        private static final ImmutableList<HttpMessageConverter<?>> DEFAULT =
                ImmutableList.copyOf(new RestTemplate().getMessageConverters());

        private Converters() {

        }
    }

    static class Plugins {
        private static final ImmutableList<Plugin> DEFAULT =
                ImmutableList.copyOf(ServiceLoader.load(Plugin.class));

        private Plugins() {

        }
    }

    private static final UrlResolution DEFAULT_RESOLUTION = UrlResolution.RFC;

    private Executor executor;
    private ClientHttpRequestFactory requestFactory;
    private final List<HttpMessageConverter<?>> converters = new ArrayList<>();
    private Supplier<URI> baseUrlProvider = () -> null;
    private UrlResolution resolution = DEFAULT_RESOLUTION;
    private final List<Plugin> plugins = new ArrayList<>();

    DefaultHttpBuilder() {

    }

    @Override
    public RequestFactoryStage executor(final Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public ConfigurationStage requestFactory(final ClientHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        return this;
    }

    @Override
    public ConfigurationStage defaultConverters() {
        return converters(Converters.DEFAULT);
    }

    @Override
    public ConfigurationStage converters(@Nonnull final Iterable<HttpMessageConverter<?>> converters) {
        converters.forEach(this::converter);
        return this;
    }

    @Override
    public ConfigurationStage converter(final HttpMessageConverter<?> converter) {
        this.converters.add(converter);
        return this;
    }

    @Override
    public ConfigurationStage baseUrl(@Nullable final String baseUrl) {
        return baseUrl(baseUrl == null ? null : URI.create(baseUrl));
    }

    @Override
    public ConfigurationStage baseUrl(@Nullable final URI baseUrl) {
        checkAbsoluteBaseUrl(baseUrl);
        return baseUrl(() -> baseUrl);
    }

    @Override
    public ConfigurationStage baseUrl(final Supplier<URI> baseUrlProvider) {
        this.baseUrlProvider = () -> checkAbsoluteBaseUrl(baseUrlProvider.get());
        return this;
    }

    private URI checkAbsoluteBaseUrl(@Nullable final URI baseUrl) {
        checkArgument(baseUrl == null || baseUrl.isAbsolute(), "Base URL is not absolute");
        return baseUrl;
    }

    @Override
    public ConfigurationStage urlResolution(@Nullable final UrlResolution resolution) {
        this.resolution = firstNonNull(resolution, DEFAULT_RESOLUTION);
        return this;
    }

    @Override
    public ConfigurationStage defaultPlugins() {
        return plugins(Plugins.DEFAULT);
    }

    @Override
    public ConfigurationStage plugins(final Iterable<Plugin> plugins) {
        plugins.forEach(this::plugin);
        return this;
    }

    @Override
    public ConfigurationStage plugin(final Plugin plugin) {
        this.plugins.add(plugin);
        return this;
    }

    @Override
    public Http build() {
        final List<HttpMessageConverter<?>> converters = converters();

        final List<Plugin> plugins = new ArrayList<>();
        plugins.add(new AsyncPlugin(executor));
        plugins.add(new SerializationPlugin(new DefaultMessageWriter(converters)));
        plugins.addAll(plugins());

        return new DefaultHttp(requestFactory, converters, baseUrlProvider, resolution, composite(plugins));
    }

    private List<HttpMessageConverter<?>> converters() {
        return converters.isEmpty() ? Converters.DEFAULT : converters;
    }

    private List<Plugin> plugins() {
        return plugins.isEmpty() ? Plugins.DEFAULT : plugins;
    }

}
