package ru.gurzhiy.crawler.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.gurzhiy.crawler.concurrent.DataProducer;
import ru.gurzhiy.crawler.concurrent.Worker;
import ru.gurzhiy.crawler.model.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Сервис, реализующий многопоточную обработку файла,
 * количество потоков которой {@link  ru.gurzhiy.crawler.concurrent.Worker} настраивается в application.properties
 * параметром numberOfWorkerThreads также стоит проверить параметр maxThreadPoolSize, выставляющий
 * верхний предел количества потоков в пуле
 */
@Service
public class DictionaryLookupService {

    @Value("${numberOfWorkerThreads}")
    private int numberOfWorkerThreads;
    private boolean isFileUploaded;
    private static final Logger log = LoggerFactory.getLogger(DictionaryLookupService.class);

    public final ThreadPoolTaskExecutor executor;

    public DictionaryLookupService(@Qualifier("crawlerPool") ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }

    /**
     * Метод читает из filename строки и складывает их в блокирующую очередь одним потоком.
     * другие несколько потоков (зависит от numberOfWorkerThreads) разбирают эту очередь или ждут пока в ней
     * что-то появится. Когда читающий поток заканчивает работу с файлом он выбрасывает флаг EOF. Флаг хранится в
     * локальной переменной типа AtomicBoolean и используется потоками обработчиками очереди (WorkerThread.class)
     * для получения информации о завершении процесса ожидания новых данных в очередь.
     * т.е. если очередь пуста и eof.get() == true, то обработчик прекращает работу.
     *
     * @param request  - поисковый запрос, для которого ищем релевантные результаты
     * @param filename имя файла, сохраненного локально. Поиск производится внутри данного файла.
     * @return список пар, каждая из которых представляет экземпляр класса в полями (критерий релевантности , строка)
     */

    public List<Pair> dictionaryLookup(String request, String filename) {

        //очередь в роли временного буфера т.к. с ней удобно работать элементы забираем из "головы", новые кладем в "хвост"

        /**
         * выбран тип LinkedBlockingQueue т.к. на 1.8 Гб файле (3-6 слов с строке,
         * слова из словаря русского языка в случайном порядке, разделеные пробелами, стрки разделены .n),
         * время чтения (33с) было меньше, чем у ArrayBlockingQueue (60с) и существенно
         * меньше чем у ConcurrentLinkedQueue (минуты).
         */
        Queue<Pair> queue = new LinkedBlockingQueue<>();

        //потокобезопасный список, т.к. в него будут делать записи разные потоки.
        List<Pair> output = new CopyOnWriteArrayList<>();
        AtomicBoolean eof = new AtomicBoolean(false);
        AtomicBoolean hasException = new AtomicBoolean(false);

        long start = System.currentTimeMillis();


        executor.execute(new DataProducer(filename, queue, eof, hasException));

        int i = 0;
        while (i < numberOfWorkerThreads) {
            executor.execute(new Worker(queue, output, request, eof));
            i++;
        }
        log.info("количество активных потоков executor: " + executor.getActiveCount());


        while (true) {

            //проверяем что потоки запустились
            if (executor.getActiveCount() >= 0) {

                if (hasException.get()) {

                    //предполагаем, что сохраненный локально файл не подходит, чистим диск от него
                    try {
                        Files.deleteIfExists(Paths.get(filename));
                        log.info("Файл {} поврежден или неправильно отформатирован. Удаляется.", filename);
                    } catch (IOException e) {
                        log.info("Не удалось удалить файл с именем {}", filename);
                        e.printStackTrace();
                    }

                    //своеобразный флаг того, что файла нет и нужно сказать об этом пользователю
                    return Collections.singletonList(new Pair(10000, "ERROR"));
                }
            }

            //если число потоков = 0 то поиск завершился, считаем время и закрываем цикл
            if (executor.getActiveCount() == 0) {
                long stop = System.currentTimeMillis();
                log.info("Время поиска выражения {} составило: {}", request, (stop - start));
                break;
            }
        }


        //сортируем по результаты по критерию релевантности
        output.sort(Collections.reverseOrder());
        return output;
    }


    /**
     * Вспомогательный метод для копирования файла во временный для обработки.
     *
     * @param inputFile      файл с данными
     * @param outputFileName имя временного файла
     * @return outputFileName - это полное имя файла, которое прописано в application.properties или генерируется,
     * если путь к файлу заданный в application.properties не существует
     */
    public String uploadMultipartFile(MultipartFile inputFile, String outputFileName) {
        long start = System.currentTimeMillis();
        //соглано документации данный размер подходит для решения большинства задач
        int bufferSize = 8 * 1024;

        //проверяем что outputFileName не пуст
        if (!Files.exists(Paths.get(outputFileName))) {
            log.info("Путь к файлу не существует: {}", outputFileName);


            //выбираем ту же папку где мы и находимся (где стартовала jvm)
            String dirAddress = System.getProperty("user.dir");
            String fileName = "tempFile.txt";
            outputFileName = dirAddress + File.separator + fileName;
            log.info("Сгенерирован новый путь {}", outputFileName);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputFile.getInputStream()), bufferSize);
                 BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName), bufferSize);
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    writer.write(line + "\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            long stop = System.currentTimeMillis();
            long processTime = stop - start;
            log.info("Копирование файла завершено за {}", processTime);
        }

        //выставляем флаг, показывающий, что файл успешно загрузился
        isFileUploaded = true;
        return outputFileName;
    }

    public boolean isFileUploaded() {
        return isFileUploaded;
    }

    public void setFileUploaded(boolean fileUploaded) {
        isFileUploaded = fileUploaded;
    }
}
