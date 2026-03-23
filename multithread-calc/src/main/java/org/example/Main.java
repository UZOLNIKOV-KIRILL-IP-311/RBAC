package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Многопоточный калькулятор с прогресс-барами.
 */
public class Main {

    // Количество потоков и длина расчёта
    private static final int THREAD_COUNT = 4;
    private static final int CALCULATION_LENGTH = 50;

    public static void main(String[] args) {
        System.out.println("=== Multithreaded Calculation Simulator ===\n");
        System.out.println("Thread count: " + THREAD_COUNT);
        System.out.println("Calculation length: " + CALCULATION_LENGTH);
        System.out.println("\nStarting calculation...\n");

        long startTime = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        List<CalculationThread> threads = new ArrayList<>();

        // Создаём и запускаем потоки
        for (int i = 0; i < THREAD_COUNT; i++) {
            CalculationThread thread = new CalculationThread(i, CALCULATION_LENGTH, latch);
            threads.add(thread);
            thread.start();
        }

        // Ждём завершения всех потоков
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted!");
        }

        long endTime = System.currentTimeMillis();

        // Выводим результаты
        System.out.println("\n=== Results ===\n");
        for (CalculationThread thread : threads) {
            try {
                thread.join();
                System.out.printf("Thread %d (ID: %d): %s - Time: %d ms%n",
                        thread.getThreadNumber(),
                        thread.getId(),
                        thread.isCompleted() ? "COMPLETED" : "FAILED",
                        thread.getExecutionTime());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("\nTotal execution time: " + (endTime - startTime) + " ms");
        System.out.println("\n=== Calculation Complete ===");
    }
}
