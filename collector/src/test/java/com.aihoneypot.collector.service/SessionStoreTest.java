package com.aihoneypot.collector.service;

import com.aihoneypot.core.model.RawRequestSignals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SessionStore.
 *
 * No Spring context — SessionStore has no Spring dependencies,
 * so we instantiate it directly.
 */
@DisplayName("SessionStore")
class SessionStoreTest {

    private SessionStore store;

    private RawRequestSignals signal(String sessionId) {
        return RawRequestSignals.builder()
                .sessionId(sessionId)
                .ipAddress("1.2.3.4")
                .method("GET")
                .uri("/page")
                .userAgent("Mozilla/5.0")
                .timestamp(Instant.now())
                .build();
    }

    @BeforeEach
    void setUp() {
        store = new SessionStore();
    }

    // ── addRequest / getSessionRequests ─────────────────────────────────────────

    @Nested
    @DisplayName("addRequest / getSessionRequests")
    class AddAndGet {

        @Test
        @DisplayName("First request is stored and retrievable")
        void firstRequestStored() {
            store.addRequest("s1", signal("s1"));
            List<RawRequestSignals> requests = store.getSessionRequests("s1");
            assertEquals(1, requests.size());
        }

        @Test
        @DisplayName("Multiple requests for the same session are all stored in order")
        void multipleRequestsInOrder() {
            RawRequestSignals r1 = signal("s1");
            RawRequestSignals r2 = RawRequestSignals.builder()
                    .sessionId("s1").ipAddress("1.2.3.4").method("POST")
                    .uri("/submit").userAgent("Mozilla/5.0").timestamp(Instant.now()).build();

            store.addRequest("s1", r1);
            store.addRequest("s1", r2);

            List<RawRequestSignals> requests = store.getSessionRequests("s1");
            assertEquals(2, requests.size());
            assertEquals("GET", requests.get(0).getMethod());
            assertEquals("POST", requests.get(1).getMethod());
        }

        @Test
        @DisplayName("Requests for different sessions are stored independently")
        void differentSessionsIndependent() {
            store.addRequest("s1", signal("s1"));
            store.addRequest("s1", signal("s1"));
            store.addRequest("s2", signal("s2"));

            assertEquals(2, store.getSessionRequests("s1").size());
            assertEquals(1, store.getSessionRequests("s2").size());
        }

        @Test
        @DisplayName("Returned list is a defensive copy (modifying it doesn't affect store)")
        void returnedListIsDefensiveCopy() {
            store.addRequest("s1", signal("s1"));
            List<RawRequestSignals> list = store.getSessionRequests("s1");
            list.clear(); // mutate the returned list
            assertEquals(1, store.getSessionRequests("s1").size()); // store unchanged
        }
    }

    // ── getSessionRequests for unknown session ──────────────────────────────────

    @Nested
    @DisplayName("Unknown sessions")
    class UnknownSession {

        @Test
        @DisplayName("getSessionRequests on unknown session returns empty list, not null")
        void unknownSessionEmptyList() {
            List<RawRequestSignals> result = store.getSessionRequests("nonexistent");
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getPreviousRequest on unknown session returns null")
        void unknownSessionPreviousNull() {
            assertNull(store.getPreviousRequest("nonexistent"));
        }

        @Test
        @DisplayName("getRequestCount on unknown session returns 0")
        void unknownSessionCountZero() {
            assertEquals(0, store.getRequestCount("nonexistent"));
        }
    }

    // ── getPreviousRequest ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getPreviousRequest")
    class GetPreviousRequest {

        @Test
        @DisplayName("Returns null when session has only one request")
        void oneRequestNoPrevious() {
            store.addRequest("s1", signal("s1"));
            assertNull(store.getPreviousRequest("s1"));
        }

        @Test
        @DisplayName("Returns the second-to-last request after two requests")
        void twoRequestsReturnFirst() {
            RawRequestSignals first = signal("s1");
            RawRequestSignals second = RawRequestSignals.builder()
                    .sessionId("s1").ipAddress("1.2.3.4").method("POST")
                    .uri("/other").userAgent("UA").timestamp(Instant.now()).build();

            store.addRequest("s1", first);
            store.addRequest("s1", second);

            RawRequestSignals previous = store.getPreviousRequest("s1");
            assertNotNull(previous);
            assertEquals("GET", previous.getMethod()); // first request
        }

        @Test
        @DisplayName("After three requests, previous is the second one")
        void threeRequestsPreviousIsSecond() {
            for (int i = 0; i < 3; i++) {
                store.addRequest("s1", RawRequestSignals.builder()
                        .sessionId("s1").ipAddress("1.2.3.4").method("GET")
                        .uri("/page" + i).userAgent("UA").timestamp(Instant.now()).build());
            }
            RawRequestSignals previous = store.getPreviousRequest("s1");
            assertEquals("/page1", previous.getUri());
        }
    }

    // ── getRequestCount ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getRequestCount")
    class GetRequestCount {

        @Test
        @DisplayName("Count starts at 1 after first request")
        void countAfterFirstRequest() {
            store.addRequest("s1", signal("s1"));
            assertEquals(1, store.getRequestCount("s1"));
        }

        @Test
        @DisplayName("Count increments with each request")
        void countIncrements() {
            store.addRequest("s1", signal("s1"));
            store.addRequest("s1", signal("s1"));
            store.addRequest("s1", signal("s1"));
            assertEquals(3, store.getRequestCount("s1"));
        }
    }

    // ── removeSession ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeSession")
    class RemoveSession {

        @Test
        @DisplayName("After removal, getSessionRequests returns empty list")
        void removedSessionEmpty() {
            store.addRequest("s1", signal("s1"));
            store.removeSession("s1");
            assertTrue(store.getSessionRequests("s1").isEmpty());
        }

        @Test
        @DisplayName("After removal, getRequestCount returns 0")
        void removedSessionCountZero() {
            store.addRequest("s1", signal("s1"));
            store.removeSession("s1");
            assertEquals(0, store.getRequestCount("s1"));
        }

        @Test
        @DisplayName("Removing a non-existent session doesn't throw")
        void removingNonexistentSessionSafe() {
            assertDoesNotThrow(() -> store.removeSession("does-not-exist"));
        }

        @Test
        @DisplayName("Removing one session doesn't affect others")
        void removeOneSessionKeepsOthers() {
            store.addRequest("s1", signal("s1"));
            store.addRequest("s2", signal("s2"));
            store.removeSession("s1");
            assertEquals(1, store.getRequestCount("s2"));
        }
    }

    // ── getActiveSessionCount ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getActiveSessionCount")
    class ActiveSessionCount {

        @Test
        @DisplayName("Zero sessions initially")
        void initiallyZero() {
            assertEquals(0, store.getActiveSessionCount());
        }

        @Test
        @DisplayName("Count increases with distinct sessions")
        void countIncreases() {
            store.addRequest("s1", signal("s1"));
            store.addRequest("s2", signal("s2"));
            assertEquals(2, store.getActiveSessionCount());
        }

        @Test
        @DisplayName("Multiple requests to same session count as one")
        void sameSessionCountedOnce() {
            store.addRequest("s1", signal("s1"));
            store.addRequest("s1", signal("s1"));
            assertEquals(1, store.getActiveSessionCount());
        }

        @Test
        @DisplayName("Decreases after removal")
        void decreasesAfterRemoval() {
            store.addRequest("s1", signal("s1"));
            store.addRequest("s2", signal("s2"));
            store.removeSession("s1");
            assertEquals(1, store.getActiveSessionCount());
        }
    }

    // ── MAX_REQUESTS_PER_SESSION cap (100 requests) ─────────────────────────────

    @Nested
    @DisplayName("Session request cap (MAX=100)")
    class RequestCap {

        @Test
        @DisplayName("Storing 101 requests keeps only the last 100")
        void capAt100Requests() {
            for (int i = 0; i < 101; i++) {
                store.addRequest("s1", RawRequestSignals.builder()
                        .sessionId("s1").ipAddress("1.2.3.4").method("GET")
                        .uri("/r" + i).userAgent("UA").timestamp(Instant.now()).build());
            }
            // Must not exceed 100
            assertTrue(store.getRequestCount("s1") <= 100);
        }

        @Test
        @DisplayName("After cap, oldest request is dropped (sliding window)")
        void oldestDroppedWhenCapped() {
            // First request has URI /r0 — it should be gone after 101 requests
            for (int i = 0; i < 101; i++) {
                store.addRequest("s1", RawRequestSignals.builder()
                        .sessionId("s1").ipAddress("1.2.3.4").method("GET")
                        .uri("/r" + i).userAgent("UA").timestamp(Instant.now()).build());
            }
            List<RawRequestSignals> requests = store.getSessionRequests("s1");
            assertTrue(requests.stream().noneMatch(r -> "/r0".equals(r.getUri())),
                    "The very first request should have been evicted");
        }
    }

    @Nested
    @DisplayName("getAverageTimeBetweenRequests")
    class AverageTime {

        @Test
        @DisplayName("Returns null for single request")
        void singleRequestNullAvg() {
            store.addRequest("s1", signal("s1"));
            assertNull(store.getAverageTimeBetweenRequests("s1"));
        }

        @Test
        @DisplayName("Returns correct average for multiple requests")
        void multipleRequestsAvg() {
            RawRequestSignals r1 = RawRequestSignals.builder()
                    .sessionId("s1").timestamp(Instant.now()).build();
            RawRequestSignals r2 = RawRequestSignals.builder()
                    .sessionId("s1").timestamp(Instant.now())
                    .timeSincePreviousRequest(100L).build();
            RawRequestSignals r3 = RawRequestSignals.builder()
                    .sessionId("s1").timestamp(Instant.now())
                    .timeSincePreviousRequest(200L).build();

            store.addRequest("s1", r1);
            store.addRequest("s1", r2);
            store.addRequest("s1", r3);

            assertEquals(150.0, store.getAverageTimeBetweenRequests("s1"));
        }
    }

    // ── Thread safety ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafety {

        @Test
        @DisplayName("Concurrent writes to same session don't throw and are consistent")
        void concurrentWritesSameSession() throws InterruptedException {
            int threads = 10;
            int requestsPerThread = 20;
            CountDownLatch ready = new CountDownLatch(threads);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threads);

            ExecutorService executor = Executors.newFixedThreadPool(threads);
            for (int t = 0; t < threads; t++) {
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        for (int i = 0; i < requestsPerThread; i++) {
                            store.addRequest("shared-session", signal("shared-session"));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await();
            start.countDown();
            assertTrue(done.await(5, TimeUnit.SECONDS), "Threads should finish within 5s");

            // Should not throw and count should be capped at 100
            assertTrue(store.getRequestCount("shared-session") <= 100);
            executor.shutdown();
        }

        @Test
        @DisplayName("Concurrent writes to different sessions don't interfere")
        void concurrentWritesDifferentSessions() throws InterruptedException {
            int sessions = 20;
            CountDownLatch done = new CountDownLatch(sessions);
            ExecutorService executor = Executors.newFixedThreadPool(sessions);

            for (int i = 0; i < sessions; i++) {
                final String sessionId = "session-" + i;
                executor.submit(() -> {
                    try {
                        store.addRequest(sessionId, signal(sessionId));
                        store.addRequest(sessionId, signal(sessionId));
                    } finally {
                        done.countDown();
                    }
                });
            }

            assertTrue(done.await(5, TimeUnit.SECONDS));
            assertEquals(sessions, store.getActiveSessionCount());
            executor.shutdown();
        }
    }
}