package com.example.fixaordering.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderCodeGeneratorConcurrencyTest {

    private static final int THREADS = 8;
    private static final int CALLS_PER_THREAD = 10;

    @Autowired
    private OrderCodeGenerator orderCodeGenerator;

    @Test
    void generatesUniqueCodesUnderConcurrentCalls() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CyclicBarrier barrier = new CyclicBarrier(THREADS);
        List<Future<Set<String>>> futures = new ArrayList<>();
        for (int i = 0; i < THREADS; i++) {
            Callable<Set<String>> worker = () -> {
                barrier.await(5, TimeUnit.SECONDS);
                Set<String> codes = new HashSet<>();
                for (int c = 0; c < CALLS_PER_THREAD; c++) {
                    codes.add(orderCodeGenerator.generate());
                }
                return codes;
            };
            futures.add(pool.submit(worker));
        }
        Set<String> allCodes = new HashSet<>();
        for (Future<Set<String>> future : futures) {
            allCodes.addAll(future.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertThat(allCodes).hasSize(THREADS * CALLS_PER_THREAD);
        assertThat(allCodes).allSatisfy(code -> assertThat(code).matches("\\d{6}-\\d{5}"));
    }
}