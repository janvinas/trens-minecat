package io.github.janvinas.trensminecat;

import java.time.Duration;

public class Departure {
    public String name;
    public String destination;
    public String platform;
    public String information;
    public Duration delay = Duration.ZERO;
}
