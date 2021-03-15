package ru.gurzhiy.crawler;

import org.junit.jupiter.api.Test;
import ru.gurzhiy.crawler.concurrent.Worker;
import ru.gurzhiy.crawler.model.Pair;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkerTest {


    @Test
    public void testThatNumberOfElementsInListIs2() {

        //Задаем начальные параметры
        List<Pair> pairs = new CopyOnWriteArrayList<>();
        ArrayDeque<Pair> que = new ArrayDeque<Pair>();
        que.add(new Pair(0, "ааа ббб"));
        que.add(new Pair(0, "ааа ббб ссс"));
        que.add(new Pair(0, "ввв ггг дддд"));

        String request = "ааа ббб";

        Worker worker = new Worker(que, pairs, request, new AtomicBoolean(true));
        assertEquals(3, worker.getQueue().size());
        worker.run();

        assertEquals(2, worker.getList().size());
    }
}
