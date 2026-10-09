package com.kageguard.backend.alert;

import com.kageguard.backend.entity.Reading;
import com.kageguard.backend.event.ReadingSavedEvent;
import com.kageguard.backend.model.SensorReading;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertServiceTest {

    private static final String NORMAL = "S:340,L:4,P:3,R:0,T:27,F:320";
    private static final String FIRE = "S:340,L:0,P:0,R:0,T:60,F:800";
    private static final String RAIN = "S:340,L:6,P:6,R:1,T:27,F:320";

    static class FakeNotifier implements Notifier {
        final List<String> subjects = new ArrayList<>();

        @Override
        public String name() {
            return "fake";
        }

        @Override
        public void send(String subject, String message) {
            subjects.add(subject);
        }
    }

    private AlertService service(FakeNotifier notifier, boolean enabled) {
        // Runnable::run makes the work happen immediately, so tests do not need to wait
        return new AlertService(List.of(notifier), enabled, 300, "test", Runnable::run);
    }

    private void feed(AlertService service, String... packets) {
        for (String packet : packets) {
            service.onReadingSaved(new ReadingSavedEvent(Reading.from(SensorReading.parse(packet))));
        }
    }

    @Test
    void normalReadingsSendNothing() {
        FakeNotifier n = new FakeNotifier();
        feed(service(n, true), NORMAL, NORMAL, NORMAL);
        assertEquals(0, n.subjects.size());
    }

    @Test
    void fireSendsOneAlertEvenIfItLastsManyReadings() {
        FakeNotifier n = new FakeNotifier();
        feed(service(n, true), NORMAL, FIRE, FIRE, FIRE);
        assertEquals(1, n.subjects.size());
        assertTrue(n.subjects.get(0).contains("FIRE"));
    }

    @Test
    void fireClearingSendsAFollowUp() {
        FakeNotifier n = new FakeNotifier();
        feed(service(n, true), FIRE, FIRE, NORMAL);
        assertEquals(2, n.subjects.size());
        assertTrue(n.subjects.get(1).contains("cleared"));
    }

    @Test
    void rainStartAndStopSendTwoAlerts() {
        FakeNotifier n = new FakeNotifier();
        feed(service(n, true), NORMAL, RAIN, RAIN, NORMAL);
        assertEquals(2, n.subjects.size());
        assertTrue(n.subjects.get(0).contains("rain detected"));
        assertTrue(n.subjects.get(1).contains("rain stopped"));
    }

    @Test
    void rainCooldownSuppressesQuickRepeats() {
        FakeNotifier n = new FakeNotifier();
        feed(service(n, true), RAIN, NORMAL, RAIN, NORMAL);
        assertEquals(2, n.subjects.size());   // only the first rain start and its stop
    }

    @Test
    void fireIsNeverSuppressedByTheRainCooldown() {
        FakeNotifier n = new FakeNotifier();
        feed(service(n, true), FIRE, NORMAL, FIRE);
        assertEquals(3, n.subjects.size());   // alert, cleared, alert again
    }

    @Test
    void disabledAlertsSendNothing() {
        FakeNotifier n = new FakeNotifier();
        feed(service(n, false), FIRE, RAIN);
        assertEquals(0, n.subjects.size());
    }
}