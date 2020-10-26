package org.luolun.ConcurrentLinkedQueueDemo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class BaseTest {
    public static final int RANGE = 10000;
    private static Logger logger = LogManager.getLogger(BaseTest.class);

    Queue<Integer> queue;

    public BaseTest() {}

    // public BaseTest(Queue<Integer> queue) {
    //     this.queue = queue;
    // }

    public void test() throws InterruptedException {
        // System.out.println("start");

        logger.info("创建测试数据");
        long startGenerateData = System.currentTimeMillis();
        Set<Integer> set = new ConcurrentSkipListSet<>();
        for (int i = 0; i < RANGE; i++) {
            logger.info("adding " + i);
            queue.add(i);
            set.add(i);
        }
        long endGenerateData = System.currentTimeMillis();
        logger.info("创建测试数据完毕, 耗时: " + (endGenerateData - startGenerateData) + " 毫秒");

        logger.info("Create threads");
        List<Thread> threads = new ArrayList<>(1000);
        long startThreads = System.currentTimeMillis();
        for (int i = 0; i < 15; i++) {
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                logger.info(String.format("线程%s开始", Thread.currentThread().getName()));
                long threadStart = System.currentTimeMillis();
                Integer num = null;
                while ((num = queue.poll()) != null) {
                    set.remove(num);
                }
                logger.info(
                        String.format("线程%s结束，耗时: %d 毫秒",
                        Thread.currentThread().getName(),
                        System.currentTimeMillis() - threadStart)
                );
            });
            t.setName("Test thread " + i);
            threads.add(t);
        }

        for (Thread thread : threads) {
            thread.start();
        }


        logger.info("等待所有线程结束...");
        for (Thread thread : threads) {
            thread.join();
        }
        long endStartThreads = System.currentTimeMillis();
        logger.info("所有线程结束，耗时: " + (endStartThreads - startThreads) + " millis");

        Object[] setArray = set.toArray();
        Arrays.sort(setArray);
        Assert.assertTrue("set不为空: " + Arrays.deepToString(setArray),
                set.isEmpty());
    }

    public static class DemoTest extends BaseTest {
        public DemoTest() {
            this.queue = new ConcurrentLinkedQueue<>();
        }

        // 6s196ms
        @Test
        @Override
        public void test() throws InterruptedException {
            super.test();
        }
    }

    public static class JDKTest extends BaseTest {
        public JDKTest() {
            this.queue = new java.util.concurrent.ConcurrentLinkedQueue<>();
        }

        // 1s145ms
        @Test
        @Override
        public void test() throws InterruptedException {
            super.test();
        }
    }
}
