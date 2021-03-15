package ru.gurzhiy.crawler.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gurzhiy.crawler.model.Pair;
import ru.gurzhiy.crawler.utils.Utils;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Класс предназначенный для разбора очереди
 * создаваемой классом {@link ru.gurzhiy.crawler.concurrent.DataProducer}
 *
 */


public class Worker implements Runnable{

    private final static Logger log = LoggerFactory.getLogger(Worker.class);
    private final Queue<Pair> queue;
    private final List<Pair> list;
    private final AtomicBoolean eof;
    private final String request;


    /**
     *
     * @param queue очередь, хранящая строки в нижнем регистре, разделенные пробелами
     * @param list список, куда собираются результаты поиска в виде пар {@link Pair}
     * @param request поисковый запрос
     * @param eof флаг, показывающий, что поток {@link DataProducer} закончил читать файл и очередь не будет больше пополняться
     */
    public Worker(Queue<Pair> queue, List<Pair> list, String request, AtomicBoolean eof) {
        this.queue = queue;
        this.list = list;
        this.request = request;
        this.eof = eof;
    }

    @Override
    public void run() {

        log.info(" {} запущен ", Thread.currentThread().getName());

        /**
         * разбираем очередь в бесконечном цикле.
         * о том что очередь закончилась и можно больше не ждать новых данных
         * проверяем через размер очереди = 0 и eof флаг  = true
         */
        while (true) {
            if (queue.size() == 0) {
                //очередь пустая ждем пока появятся данные в ней
            } else {
                Pair pair = queue.poll();

                if (pair != null) {
                    String line = pair.getLine();
                    int relevantCriterion = Utils.calculateRelevanceCriteriaForRequestAndLine(request, line);
                    if (relevantCriterion > 2) {

                        //Добавляем все пары, где критерий релевантности > 2
                        list.add(new Pair(relevantCriterion, line));
                    }
                }
            }

            //если выставлен флаг конца файла (т.е. больше нечего читать) и в очереди пусто, то выходим из цикла
            if (eof.get() && queue.size() == 0) {
                log.info("EOF флаг выставлен. Поток закрывается.");
                break;
            }

        }

    }


    public Queue<Pair> getQueue() {
        return queue;
    }

    public List<Pair> getList() {
        return list;
    }
}
