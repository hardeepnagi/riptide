package org.zalando.riptide.failsafe;

import com.google.common.base.CharMatcher;

import javax.annotation.Nullable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static java.lang.Long.parseLong;
import static java.time.Duration.between;
import static java.time.Instant.now;

final class EpochSecondsDelayParser implements DelayParser {

    private final CharMatcher digit = CharMatcher.inRange('0', '9').precomputed();

    private final Clock clock;

    EpochSecondsDelayParser(final Clock clock) {
        this.clock = clock;
    }

    @Override
    public Duration parse(final String value) {
        return until(parseDate(value));
    }

    @Nullable
    private Instant parseDate(final String value) {
        if (isInteger(value)) {
            final long epochSecond = parseLong(value);
            if (epochSecond > 1_000) {
                return Instant.ofEpochSecond(epochSecond);
            }
        }

        return null;
    }

    private boolean isInteger(final String value) {
        return digit.matchesAllOf(value) && !value.isEmpty();
    }

    @Nullable
    private Duration until(@Nullable final Instant end) {
        return end == null ? null : between(now(clock), end);
    }

}
