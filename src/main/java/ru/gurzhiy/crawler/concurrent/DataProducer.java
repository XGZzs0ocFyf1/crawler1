package ru.gurzhiy.crawler.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gurzhiy.crawler.model.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Класс для чтения файла в потоке.
 */
public class DataProducer implements Runnable{

    private final static Logger log = LoggerFactory.getLogger(DataProducer.class);

    private final AtomicBoolean eofFlag;
    private static final int bufferSize = 8192;
    private final String filename;
    private final Queue<Pair> queue;
    private final AtomicBoolean hasException;

    /**
     *
     * @param filename имя файла откуда читать данные
     * @param queue очередь в которую складываются считанные данные
     * @param eofFlag если true то файл прочитан
     * @param hasException если true то DataProducer выбрасывал исключение
     * логика такая, т.к. нельзя пробросить исключение через метод run в сервис и обработать его там
     */
    public DataProducer(String filename, Queue<Pair> queue, AtomicBoolean eofFlag, AtomicBoolean hasException) {
        this.filename = filename;
        this.queue = queue;
        this.hasException = hasException;
        this.eofFlag = eofFlag;
    }


    private void readFileAndAddToQueue(){

        long t1 = System.currentTimeMillis();
        log.info("Запущен поток DataProducer");
        try (

                FileInputStream fis = new FileInputStream(filename);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8); //UTF-8 соглано заданию
                BufferedReader br = new BufferedReader(isr, bufferSize)) {

            for (String line; (line = br.readLine()) != null;){
                queue.offer(new Pair(0, line));
            }
            long t2 = System.currentTimeMillis();
            eofFlag.set(true);

            log.info("Время чтения файла {} миллисекунд", (t2-t1));
            log.info("Выставлен флаг конца файла: {}", eofFlag.get());
            log.info("размер очереди: {}",queue.size());
        } catch (IOException e) {

            //eofFlag если мы не можем прочитать файл, то нужно не запускать (остановить) потоки, ожидающие заполнение очереди
            //hasException флаг для индикации пользователю о том, что произошла проблема с тем файлам, который он выбрал
            hasException.set(true);
            eofFlag.set(true);

            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        readFileAndAddToQueue();
    }

}
