package com.kageguard.backend.alert;

import com.kageguard.backend.entity.Reading;
import com.kageguard.backend.event.ReadingSavedEvent;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss", Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault());

    private final List<Notifier> notifiers;
    private final boolean enabled;
    private final long rainCooldownSeconds;
    private final String source;
    private final Executor executor;

    // What the previous reading looked like
    private boolean lastFire = false;
    private boolean lastRain = false;
    private boolean rainStartAlerted = false;
    private Instant lastRainAlert = null;

    @Autowired
    public AlertService(ObjectProvider<Notifier> notifierProvider,
                        @Value("${kageguard.alerts.enabled:true}") boolean enabled,
                        @Value("${kageguard.alerts.rain-cooldown-seconds:300}") long rainCooldownSeconds,
                        @Value("${kageguard.alerts.source:simulated}") String source) {
        this(notifierProvider.orderedStream().toList(), enabled, rainCooldownSeconds, source,
                Executors.newSingleThreadExecutor());
    }

    // Used by tests, where the executor runs the work immediately
    AlertService(List<Notifier> notifiers, boolean enabled, long rainCooldownSeconds,
                 String source, Executor executor) {
        this.notifiers = notifiers;
        this.enabled = enabled;
        this.rainCooldownSeconds = rainCooldownSeconds;
        this.source = source;
        this.executor = executor;
    }

    // Runs every time a reading is saved
    @EventListener
    public synchronized void onReadingSaved(ReadingSavedEvent event) {
        if (!enabled) {
            return;
        }
        Reading r = event.reading();

        // Fire: alert when it starts and when it clears. No cooldown.
        boolean fire = r.isFireDetected();
        if (fire && !lastFire) {
            sendAll("KageGuard FIRE ALERT", fireMessage(r));
        } else if (!fire && lastFire) {
            sendAll("KageGuard: fire cleared", clearedMessage(r));
        }
        lastFire = fire;

        // Rain: alert when it starts (with cooldown) and when it stops
        boolean rain = r.isRainActive();
        if (rain && !lastRain) {
            Instant now = Instant.now();
            boolean allowed = lastRainAlert == null
                    || Duration.between(lastRainAlert, now).getSeconds() >= rainCooldownSeconds;
            if (allowed) {
                sendAll("KageGuard: rain detected", rainMessage(r));
                lastRainAlert = now;
                rainStartAlerted = true;
            } else {
                rainStartAlerted = false;
            }
        } else if (!rain && lastRain && rainStartAlerted) {
            sendAll("KageGuard: rain stopped", rainStoppedMessage(r));
            rainStartAlerted = false;
        }
        lastRain = rain;
    }

    // Sends a test message to every enabled channel and reports what happened
    public List<String> sendTest() {
        List<String> results = new ArrayList<>();
        if (notifiers.isEmpty()) {
            results.add("No alert channels are enabled. Check the kageguard.alerts settings.");
            return results;
        }
        for (Notifier n : notifiers) {
            try {
                n.send("KageGuard test alert",
                        "If you can read this, the " + n.name() + " channel works.\nData source: " + source);
                results.add(n.name() + ": sent");
            } catch (Exception e) {
                results.add(n.name() + ": failed - " + e.getMessage());
            }
        }
        return results;
    }

    private void sendAll(String subject, String message) {
        if (notifiers.isEmpty()) {
            log.warn("Alert '{}' was not sent because no alert channels are enabled", subject);
            return;
        }
        for (Notifier n : notifiers) {
            executor.execute(() -> {
                try {
                    n.send(subject, message);
                    log.info("Alert sent via {}: {}", n.name(), subject);
                } catch (Exception e) {
                    log.warn("Alert via {} failed: {}", n.name(), e.getMessage());
                }
            });
        }
    }

    private String fireMessage(Reading r) {
        return "Fire detected at " + TIME.format(r.getRecordedAt()) + "\n"
                + "Temperature: " + r.getTempC() + " °C\n"
                + "Flame sensor: " + r.getFlameValue() + " / 1023\n"
                + "Curtain position: " + r.getCurtainPosition() + " of 6 (0 = fully open)\n"
                + "Action: the curtain is being opened fully.\n"
                + "Data source: " + source;
    }

    private String clearedMessage(Reading r) {
        return "No fire detected as of " + TIME.format(r.getRecordedAt()) + "\n"
                + "Temperature: " + r.getTempC() + " °C\n"
                + "Flame sensor: " + r.getFlameValue() + " / 1023\n"
                + "Data source: " + source;
    }

    private String rainMessage(Reading r) {
        return "Rain detected at " + TIME.format(r.getRecordedAt()) + "\n"
                + "Curtain position: " + r.getCurtainPosition() + " of 6 (6 = fully closed)\n"
                + "Action: the curtain is being closed fully.\n"
                + "Data source: " + source;
    }

    private String rainStoppedMessage(Reading r) {
        return "Rain stopped at " + TIME.format(r.getRecordedAt()) + "\n"
                + "The curtain returns to automatic light control.\n"
                + "Data source: " + source;
    }

    @PreDestroy
    public void shutdown() {
        if (executor instanceof ExecutorService service) {
            service.shutdown();
        }
    }
}