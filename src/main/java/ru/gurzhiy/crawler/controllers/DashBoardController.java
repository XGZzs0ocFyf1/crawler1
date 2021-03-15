package ru.gurzhiy.crawler.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.gurzhiy.crawler.controllers.dto.RequestDto;
import ru.gurzhiy.crawler.model.Pair;
import ru.gurzhiy.crawler.service.DictionaryLookupService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * для локального запуска URL http://localhost:9080/dictionary_search/
 */


@Controller
@RequestMapping("dictionary_search")
public class DashBoardController {


    @Value("${outputFilename}")
    private String outputFileName;
    private final static Logger log = LoggerFactory.getLogger(DashBoardController.class);


    private DictionaryLookupService service;


    public DashBoardController(DictionaryLookupService service) {
        this.service = service;
    }

    /**
     * @param isDictionaryUploaded upload или false - статус загрузки словаря. Если true то словарь загружен.
     * @return
     */
    @GetMapping(value = "/", produces = "text/html; charset=utf-8")
    public String getDashBoard(
            @RequestParam(name = "isDictionaryUploaded", required = false, defaultValue = "false") boolean isDictionaryUploaded,
            Model model) {

        //обработка информации
        service.setFileUploaded(isDictionaryUploaded);
        String uploadStatus = isDictionaryUploaded ? "Словарь загружен" : "Словарь не загружен";
        model.addAttribute("uploadStatus", uploadStatus);
        model.addAttribute("requestDto", new RequestDto(""));
        return "userForm";
    }


    /**
     * разбирает загруженный файл, см сервисный класс {@link   ru.gurzhiy.crawler.service.DictionaryLookupService}
     *
     * @param requestDto объект хранилище запроса и списка результатов с критериями релевантности
     * @return страницу с результами запросов
     */
    @PostMapping("/find")
    public String findStringInFile(Model model, @ModelAttribute RequestDto requestDto) {

        if (service.isFileUploaded()) {
            long start = System.currentTimeMillis();

            model.addAttribute("requestDto", requestDto);
            List<Pair> results = service.dictionaryLookup(requestDto.getContent(), outputFileName);

//        проверяем что не было ошибки чтения файла, а если она была, отдаем пользователю сообщение об этом
            if (results.size() == 1 && results.get(0).getRelevanceCriteria() == 10000 && results.get(0).getLine().equals("ERROR")) {
                return "fileReadErrorPage";
            }

            requestDto.setRequestResult(results);
            long stop = System.currentTimeMillis();
            log.info("время выполнения запроса: {}", (stop - start));

            return "searchResult";
        } else {
            return "nextTimePleaseWait";
        }

    }

    /**
     * загружает словарь и возвращает на ту же страницу, добавляя параметр isDictionaryUploaded = true
     * среднее время загрузки словаря весом 1.3 Гб - 60..70 секунд
     *
     * @param file текстоый файл, отформатированный в UFT-8 в соответствии с с заданием
     * @return
     */
    @PostMapping(value = "/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {

        //словарь пустой
        if (file.isEmpty()) {
            return "redirect:/dictionary_search/";
        }

        //сервис проверяет наличие пути к файлу, если его нет, создает новый, поэтому переприсваиваем путь
        outputFileName = service.uploadMultipartFile(file, outputFileName);

        //возвращаемся на ту же страницу с флагом статуса загрузки
        return "redirect:/dictionary_search/?isDictionaryUploaded=true";
    }

}
