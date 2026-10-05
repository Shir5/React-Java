package com.reactjava.shir;

import com.reactjava.shir.activity.ActivityDetails;
import com.reactjava.shir.activity.ActivitySession;
import com.reactjava.shir.activity.ActivityType;
import com.reactjava.shir.activity.RewardPolicy;
import com.reactjava.shir.activity.TrackingMethod;
import com.reactjava.shir.generator.ActivityDetailsGenerator;
import com.reactjava.shir.generator.ActivitySessionGenerator;
import com.reactjava.shir.generator.ObjectGenerator;
import com.reactjava.shir.generator.RewardPolicyGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.ToLongFunction;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTests {
    @Test
    void allGeneratorsAcceptZeroAndRejectNegativeCounts() {
        List<ObjectGenerator<?>> generators = List.of(
                new ActivityDetailsGenerator(new Random(1)),
                new RewardPolicyGenerator(new Random(2)),
                new ActivitySessionGenerator(new Random(3), List.of(), List.of()));
        for (ObjectGenerator<?> generator : generators) {
            assertEquals(List.of(), generator.generate(0));
            assertThrows(IllegalArgumentException.class, () -> generator.generate(-1));
        }
    }

    @Test
    void idsStayUniqueAcrossNextAndRepeatedGenerateCalls() {
        var details = new ActivityDetailsGenerator(new Random(1));
        var policies = new RewardPolicyGenerator(new Random(2));
        assertContinuousIds(details, detail -> detail.id());
        assertContinuousIds(policies, policy -> policy.id());
        var sessions = new ActivitySessionGenerator(new Random(3), details.generate(10), policies.generate(10));
        assertContinuousIds(sessions, session -> session.id());
    }

    @Test
    void sameSeedsProduceEqualObjectsEvenWhenGenerationIsSplit() {
        var firstDetails = new ActivityDetailsGenerator(new Random(10));
        var secondDetails = new ActivityDetailsGenerator(new Random(10));
        List<ActivityDetails> splitDetails = new ArrayList<>(firstDetails.generate(3));
        splitDetails.add(firstDetails.next());
        splitDetails.addAll(firstDetails.generate(6));
        assertEquals(splitDetails, secondDetails.generate(10));

        List<RewardPolicy> firstPolicies = new RewardPolicyGenerator(new Random(20)).generate(10);
        List<RewardPolicy> secondPolicies = new RewardPolicyGenerator(new Random(20)).generate(10);
        assertEquals(firstPolicies, secondPolicies);

        var first = new ActivitySessionGenerator(new Random(30), splitDetails, firstPolicies);
        var second = new ActivitySessionGenerator(new Random(30), splitDetails, secondPolicies);
        List<ActivitySession> splitSessions = new ArrayList<>(first.generate(7));
        splitSessions.add(first.next());
        splitSessions.addAll(first.generate(12));
        assertEquals(splitSessions, second.generate(20));
    }

    @Test
    void incompatibleAttributesCannotGenerateASession() {
        var details = List.of(new ActivityDetails(1, ActivityType.READING, "Book", TrackingMethod.TIMER));
        var policies = List.of(new RewardPolicy(1, ActivityType.SPORT, 2));
        var generator = new ActivitySessionGenerator(new Random(1), details, policies);

        assertThrows(IllegalStateException.class, () -> generator.next());
        assertThrows(IllegalStateException.class, () -> generator.generate(1));
    }

    @Test
    void generatorUsesOnlyTypesWithBothAttributesAndCopiesInputLists() {
        var details = new ArrayList<>(List.of(
                new ActivityDetails(1, ActivityType.READING, "Book", TrackingMethod.TIMER),
                new ActivityDetails(2, ActivityType.SPORT, "Training", TrackingMethod.CAMERA)));
        var policies = new ArrayList<>(List.of(new RewardPolicy(1, ActivityType.SPORT, 2)));
        var generator = new ActivitySessionGenerator(new Random(1), details, policies);
        details.clear();
        policies.clear();

        assertTrue(generator.generate(20).stream().allMatch(session -> session.type() == ActivityType.SPORT));
    }

    @Test
    void randomCharacteristicsRespectDomainRanges() {
        var details = new ActivityDetailsGenerator(new Random(1)).generate(50);
        var policies = new RewardPolicyGenerator(new Random(2)).generate(50);
        var sessions = new ActivitySessionGenerator(new Random(3), details, policies).generate(5_000);

        assertTrue(details.stream().allMatch(detail -> detail.trackingMethod().supports(detail.type())));
        assertTrue(policies.stream().allMatch(policy -> policy.coinsPerMinute() >= 1 && policy.coinsPerMinute() <= 5));
        for (ActivitySession session : sessions) {
            long minutes = session.duration().toMinutes();
            switch (session.type()) {
                case READING -> assertTrue(minutes >= 10 && minutes <= 120);
                case SPORT, HOBBY -> assertTrue(minutes >= 15 && minutes <= 180);
                case WALKING -> assertTrue(minutes >= 10 && minutes <= 240);
                case SLEEP -> assertTrue(minutes >= 240 && minutes <= 600);
            }
        }
    }

    @Test
    void tagsAreIndependentOfDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            var details = List.of(new ActivityDetails(1, ActivityType.READING, "Book", TrackingMethod.TIMER));
            var policies = List.of(new RewardPolicy(1, ActivityType.READING, 2));
            var generator = new ActivitySessionGenerator(new Random(1), details, policies);
            assertEquals(List.of("reading", "timer"), generator.next().tags());
        } finally {
            Locale.setDefault(previous);
        }
    }

    private <T> void assertContinuousIds(ObjectGenerator<T> generator, ToLongFunction<T> id) {
        List<T> objects = new ArrayList<>(generator.generate(3));
        objects.add(generator.next());
        objects.addAll(generator.generate(7));
        assertEquals(11, new HashSet<>(objects).size());
        for (int i = 0; i < objects.size(); i++) {
            assertEquals(i + 1, id.applyAsLong(objects.get(i)));
        }
    }
}
