package org.zalando.riptide.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apiguardian.api.API;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.zalando.riptide.UrlResolution;
import org.zalando.riptide.autoconfigure.RiptideProperties.Caching.Heuristic;
import org.zalando.riptide.autoconfigure.RiptideProperties.CertificatePinning.Keystore;
import org.zalando.riptide.autoconfigure.RiptideProperties.Retry.Backoff;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = INTERNAL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "riptide")
public final class RiptideProperties {

    private Defaults defaults = new Defaults();
    private Map<String, Client> clients = new LinkedHashMap<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Defaults {

        private UrlResolution urlResolution = UrlResolution.RFC;

        @NestedConfigurationProperty
        private Connections connections = new Connections(
                TimeSpan.of(5, SECONDS),
                TimeSpan.of(5, SECONDS),
                TimeSpan.of(30, SECONDS),
                20,
                20
        );

        @NestedConfigurationProperty
        private Threads threads = new Threads(1, null, TimeSpan.of(1, MINUTES), 0);

        @NestedConfigurationProperty
        private OAuth oauth = new OAuth(false, Paths.get("/meta/credentials"));

        @NestedConfigurationProperty
        private TransientFaultDetection transientFaultDetection = new TransientFaultDetection(false);

        @NestedConfigurationProperty
        private StackTracePreservation stackTracePreservation = new StackTracePreservation(true);

        @NestedConfigurationProperty
        private Metrics metrics = new Metrics(false);

        @NestedConfigurationProperty
        private Retry retry = new Retry(false, null, new Backoff(false, null, null, null), null, null, null, null);

        @NestedConfigurationProperty
        private CircuitBreaker circuitBreaker = new CircuitBreaker(false, null, TimeSpan.of(0, SECONDS), null);

        @NestedConfigurationProperty
        private BackupRequest backupRequest = new BackupRequest(false, null);

        @NestedConfigurationProperty
        private Timeouts timeouts = new Timeouts(false, null);

        @NestedConfigurationProperty
        private RequestCompression requestCompression = new RequestCompression(false);

        @NestedConfigurationProperty
        private CertificatePinning certificatePinning = new CertificatePinning(false, new Keystore());

        @NestedConfigurationProperty
        private Caching caching = new Caching(
                false,
                false,
                null,
                CacheConfig.DEFAULT_MAX_OBJECT_SIZE_BYTES,
                CacheConfig.DEFAULT_MAX_CACHE_ENTRIES,
                new Heuristic(
                        false,
                        CacheConfig.DEFAULT_HEURISTIC_COEFFICIENT,
                        TimeSpan.of(CacheConfig.DEFAULT_HEURISTIC_LIFETIME, SECONDS)
                )
        );

        @NestedConfigurationProperty
        private Soap soap = new Soap(false, "1.1");

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Client {

        private String baseUrl;
        private UrlResolution urlResolution;

        @NestedConfigurationProperty
        private Connections connections;

        @NestedConfigurationProperty
        private Threads threads;

        @NestedConfigurationProperty
        private OAuth oauth;

        @NestedConfigurationProperty
        private TransientFaultDetection transientFaultDetection;

        @NestedConfigurationProperty
        private StackTracePreservation stackTracePreservation;

        @NestedConfigurationProperty
        private Metrics metrics;

        @NestedConfigurationProperty
        private Retry retry;

        @NestedConfigurationProperty
        private CircuitBreaker circuitBreaker;

        @NestedConfigurationProperty
        private BackupRequest backupRequest;

        @NestedConfigurationProperty
        private Timeouts timeouts;

        @NestedConfigurationProperty
        private RequestCompression requestCompression;

        @NestedConfigurationProperty
        private CertificatePinning certificatePinning;

        @NestedConfigurationProperty
        private Caching caching;

        @NestedConfigurationProperty
        private Soap soap;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Connections {
        private TimeSpan connectTimeout;
        private TimeSpan socketTimeout;
        private TimeSpan timeToLive;
        private Integer maxPerRoute;
        private Integer maxTotal;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Threads {
        private Integer minSize;
        private Integer maxSize;
        private TimeSpan keepAlive;
        private Integer queueSize;

        public Threads(final Integer maxSize) {
            this.maxSize = maxSize;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class OAuth {
        private Boolean enabled;
        private Path credentialsDirectory;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class TransientFaultDetection {
        private Boolean enabled;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class StackTracePreservation {
        private Boolean enabled;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Metrics {
        private Boolean enabled;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Retry {
        private Boolean enabled;
        private TimeSpan fixedDelay;
        private Backoff backoff;
        private Integer maxRetries;
        private TimeSpan maxDuration;
        private Double jitterFactor;
        private TimeSpan jitter;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static final class Backoff {
            private Boolean enabled;
            private TimeSpan delay;
            private TimeSpan maxDelay;
            private Double delayFactor;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class CircuitBreaker {
        private Boolean enabled;
        private Ratio failureThreshold;
        private TimeSpan delay;
        private Ratio successThreshold;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class BackupRequest {
        private Boolean enabled;
        private TimeSpan delay;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Timeouts {
        private Boolean enabled;
        private TimeSpan global;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class RequestCompression {
        private Boolean enabled;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class CertificatePinning {
        private Boolean enabled;
        private Keystore keystore;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static final class Keystore {
            private String path;
            private String password;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Caching {
        private Boolean enabled;
        private Boolean shared;
        private Path directory;
        private Integer maxObjectSize;
        private Integer maxCacheEntries;
        private Heuristic heuristic;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static final class Heuristic {
            private Boolean enabled;
            private Float coefficient;
            private TimeSpan defaultLifeTime;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Soap {
        private Boolean enabled;
        private String protocol;
    }

}
