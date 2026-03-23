package org.example;

import java.util.concurrent.CountDownLatch;

/**
 * Поток вычисления с прогресс-баром.
 */
public class CalculationThread extends Thread {

    private final int threadNumber;
    private final int calculationLength;
    private final CountDownLatch latch;
    private long executionTime;
    private boolean completed;

    public CalculationThread(int threadNumber, int calculationLength, CountDownLatch latch) {
        this.threadNumber = threadNumber;
        this.calculationLength = calculationLength;
        this.latch = latch;
        this.completed = false;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        try {
            // Выводим заголовок потока
            System.out.printf("[Thread %d] ID: %d - Starting calculation...%n", threadNumber, getId());

            // Имитируем расчёт с прогресс-баром
            StringBuilder progressBar = new StringBuilder("[");
            for (int i = 0; i < calculationLength; i++) {
                // Имитация вычислений (случайная задержка 10-50 мс)
                long delay = (long) (Math.random() * 40 + 10);
                Thread.sleep(delay);

                // Обновляем прогресс-бар
                progressBar.append("=");
                while (progressBar.length() < calculationLength + 1) {
                    progressBar.append(" ");
                }
                progressBar.append("] ");
                int percent = (i + 1) * 100 / calculationLength;
                progressBar.append(percent).append("%");

                // Выводим строку прогресса
                System.out.printf("[Thread %d] ID: %d - %s%n", threadNumber, getId(), progressBar.toString());

                // Очищаем строку для следующего вывода (возвращаем курсор)
                progressBar.setLength(0);
            }

            completed = true;
            System.out.printf("[Thread %d] ID: %d - CALCULATION COMPLETED!%n", threadNumber, getId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.printf("[Thread %d] ID: %d - INTERRUPTED!%n", threadNumber, getId());
            completed = false;
        } finally {
            executionTime = System.currentTimeMillis() - startTime;
            latch.countDown();
        }
    }

    public int getThreadNumber() {
        return threadNumber;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public boolean isCompleted() {
        return completed;
    }
}
