package ru.gurzhiy.crawler.utils;

/**
 * Вспомогательный класс
 */
public class Utils {

    /**
     * Данный метод предназначен для обработки одной строки. Он возвращает значение критерия релевантности. Т.е.
     * показывает насколько строка релевантна запросу.
     *
     * @param request поисковый запрос (без разделительных символов)
     * @param line    строка без разделительных символов, отформатированная в нижний регистр
     * @return relevantCriteria - значение критерия релевантности строки к запросу в соответствии с правилами:
     * <p>
     * Критерии релевантности:
     * совпадение одного слова - 2 балла
     * совпадение N слов - 2*N баллов
     * совпадение следования друг за другом двух слов - плюс балл
     * N слов идут друг за другом как в запросе пользователя, плюс N-1 балл
     * Например если в словаре есть одно выражение "съешь еще этих мягких французских булок":
     * поисковый запрос "съешь еще этих булок" - 10 баллов (совпадение одного слова 4*2 + совпадение следования - 2*1)
     * поисковый запрос "этих булок ты не съешь" - 6 баллов (совпадение одного слова 3*2)
     */
    public static int calculateRelevanceCriteriaForRequestAndLine(String request, String line) {
        request = request.toLowerCase();
        line = line.toLowerCase();
        int relevantCriteria = 0;

        //здесь предполагаем что слова разделены одним пробелом
        //превращаем строку запроса в массив строк
        String[] desiredArr = request.split(" ");

        //превращаем строку в которой будем производить поиск в массив строк
        String[] lineArr = line.split(" ");


        //если запрос содержит, добавляем индекс
        //он нужен для вычисления порядка слов в строке. хранит прошлый индекс j из lineArr
        // при котором слова совпали в прошлый раз
        int previousHitWordPosition = -1;

        int desiredIdx = 0;
        while (desiredIdx < desiredArr.length) {


            int i = desiredIdx;
            while (i < desiredArr.length) {

                int j = 0;
                while (j < lineArr.length) {

//                     первое вхождение первого поискового слова += 2
                    if (desiredArr[i].equals(lineArr[j])) {
                        relevantCriteria += 2;


                        //первое совпадение слова, переключаем значение previousHitWordPosition
                        if (previousHitWordPosition < 0) {
                            previousHitWordPosition = j;
                        }

                        //здесь проверяем что слова идут друг за другом и стоят в соседних ячейках массива
                        if (previousHitWordPosition < j && previousHitWordPosition == j - 1) {
                            relevantCriteria += 1;
                        }

                        previousHitWordPosition = j;
                        if (i < desiredArr.length - 1) {
                            //мы нашли нужное нам слово и переходим к следующему
                            i++;
                        }
                    }
                    j++;
                }

                //если перебрали все слова из поискового запрос то завершаем работу с этим запросом и этой строкой
                if (i == desiredArr.length - 1) {
                    return relevantCriteria;
                }
                i++;
            }
            desiredIdx++;
        }

        return relevantCriteria;
    }
}
