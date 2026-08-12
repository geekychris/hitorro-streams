/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LocalEventHub")
class LocalEventHubTest {

    private LocalEventHub hub;

    @BeforeEach
    void setUp() {
        hub = new LocalEventHub();
    }

    private EventListener listener(String name, List<String> received) {
        return new EventListener() {
            @Override
            public boolean event(String topic, String subTopic, Object args) {
                received.add(topic + "/" + subTopic);
                return true;
            }
            @Override public String eventName() { return name; }
            @Override public boolean runAsync() { return false; }
        };
    }

    @Nested
    @DisplayName("Registration and Dispatch")
    class RegistrationAndDispatch {

        @Test
        @DisplayName("Should dispatch event to registered listener")
        void shouldDispatchToRegistered() {
            List<String> received = new ArrayList<>();
            hub.addEventListener(listener("test", received), "myTopic");
            hub.event("myTopic", "sub", null);
            assertThat(received).containsExactly("myTopic/sub");
        }

        @Test
        @DisplayName("Should not dispatch to unregistered topic")
        void shouldNotDispatchToUnregistered() {
            List<String> received = new ArrayList<>();
            hub.addEventListener(listener("test", received), "topicA");
            hub.event("topicB", "sub", null);
            assertThat(received).isEmpty();
        }

        @Test
        @DisplayName("Should dispatch to multiple listeners on same topic")
        void shouldDispatchToMultiple() {
            List<String> received1 = new ArrayList<>();
            List<String> received2 = new ArrayList<>();
            hub.addEventListener(listener("l1", received1), "topic");
            hub.addEventListener(listener("l2", received2), "topic");
            hub.event("topic", "sub", null);
            assertThat(received1).hasSize(1);
            assertThat(received2).hasSize(1);
        }

        @Test
        @DisplayName("Should not add duplicate listener")
        void shouldNotAddDuplicate() {
            List<String> received = new ArrayList<>();
            EventListener el = listener("test", received);
            hub.addEventListener(el, "topic");
            hub.addEventListener(el, "topic");
            hub.event("topic", "sub", null);
            assertThat(received).hasSize(1);
        }
    }

    @Nested
    @DisplayName("fire convenience")
    class FireConvenience {

        @Test
        @DisplayName("Should fire with null args")
        void shouldFireWithNullArgs() {
            List<String> received = new ArrayList<>();
            hub.addEventListener(listener("test", received), "topic");
            hub.fire("topic", "sub");
            assertThat(received).containsExactly("topic/sub");
        }
    }

    @Nested
    @DisplayName("Exception Isolation")
    class ExceptionIsolation {

        @Test
        @DisplayName("Should continue dispatching after listener throws")
        void shouldContinueAfterException() {
            List<String> received = new ArrayList<>();
            EventListener bad = new EventListener() {
                @Override public boolean event(String t, String s, Object a) { throw new RuntimeException("boom"); }
                @Override public String eventName() { return "bad"; }
                @Override public boolean runAsync() { return false; }
            };
            hub.addEventListener(bad, "topic");
            hub.addEventListener(listener("good", received), "topic");
            hub.event("topic", "sub", null);
            assertThat(received).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Async Dispatch")
    class AsyncDispatch {

        @Test
        @DisplayName("Should dispatch async events")
        void shouldDispatchAsync() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            List<String> received = new ArrayList<>();
            EventListener asyncListener = new EventListener() {
                @Override
                public boolean event(String t, String s, Object a) {
                    received.add(t + "/" + s);
                    latch.countDown();
                    return true;
                }
                @Override public String eventName() { return "async"; }
                @Override public boolean runAsync() { return true; }
            };
            hub.addEventListener(asyncListener, "topic");
            hub.event("topic", "sub", null);
            assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
            assertThat(received).containsExactly("topic/sub");
        }
    }

    @Nested
    @DisplayName("removeEventListener")
    class RemoveEventListener {

        @Test
        @DisplayName("Should remove listener and stop receiving events")
        void shouldRemoveListener() {
            List<String> received = new ArrayList<>();
            EventListener el = listener("test", received);
            hub.addEventListener(el, "topic");
            hub.event("topic", "sub1", null);
            assertThat(received).hasSize(1);

            hub.removeEventListener(el, "topic");
            hub.event("topic", "sub2", null);
            assertThat(received).hasSize(1); // no new events
        }

        @Test
        @DisplayName("Should return false for non-existent listener")
        void shouldReturnFalseForNonExistent() {
            assertThat(hub.removeEventListener(listener("x", new ArrayList<>()), "topic")).isFalse();
        }
    }

    @Nested
    @DisplayName("getListenerCount")
    class GetListenerCount {

        @Test
        @DisplayName("Should return correct count")
        void shouldReturnCorrectCount() {
            assertThat(hub.getListenerCount("topic")).isEqualTo(0);
            hub.addEventListener(listener("a", new ArrayList<>()), "topic");
            assertThat(hub.getListenerCount("topic")).isEqualTo(1);
            hub.addEventListener(listener("b", new ArrayList<>()), "topic");
            assertThat(hub.getListenerCount("topic")).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("hasEventRegistered")
    class HasEventRegistered {

        @Test
        @DisplayName("Should detect registered topic")
        void shouldDetectRegistered() {
            assertThat(hub.hasEventRegistered("topic")).isFalse();
            hub.addEventListener(listener("test", new ArrayList<>()), "topic");
            assertThat(hub.hasEventRegistered("topic")).isTrue();
        }
    }

    @Nested
    @DisplayName("eventAll")
    class EventAll {

        @Test
        @DisplayName("Should broadcast to all topics")
        void shouldBroadcastToAllTopics() {
            List<String> received1 = new ArrayList<>();
            List<String> received2 = new ArrayList<>();
            hub.addEventListener(listener("a", received1), "topicA");
            hub.addEventListener(listener("b", received2), "topicB");
            hub.eventAll("flush", null);
            assertThat(received1).containsExactly("topicA/flush");
            assertThat(received2).containsExactly("topicB/flush");
        }
    }

    @Nested
    @DisplayName("eventListingNotified")
    class EventListingNotified {

        @Test
        @DisplayName("Should return list of notified listener names")
        void shouldReturnNotifiedNames() {
            hub.addEventListener(listener("listener1", new ArrayList<>()), "topic");
            hub.addEventListener(listener("listener2", new ArrayList<>()), "topic");
            List<String> notified = hub.eventListingNotified("topic", "sub", null);
            assertThat(notified).containsExactly("listener1", "listener2");
        }
    }

    @Nested
    @DisplayName("getRegisteredListeners")
    class GetRegisteredListeners {

        @Test
        @DisplayName("Should list all registered listeners")
        void shouldListAll() {
            hub.addEventListener(listener("a", new ArrayList<>()), "topic1");
            hub.addEventListener(listener("b", new ArrayList<>()), "topic2");
            var listeners = hub.getRegisteredListeners();
            assertThat(listeners).hasSize(2);
        }
    }
}
