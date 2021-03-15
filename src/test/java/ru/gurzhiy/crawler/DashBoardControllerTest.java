package ru.gurzhiy.crawler;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.gurzhiy.crawler.controllers.dto.RequestDto;
import ru.gurzhiy.crawler.model.Pair;
import ru.gurzhiy.crawler.service.DictionaryLookupService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class DashBoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DictionaryLookupService service;


    @Test
    public void shouldReturnOkResponce() throws Exception {
        this.mockMvc.perform(get("/dictionary_search/"))
                .andExpect(status().isOk())
                .andExpect(view().name("userForm"))
                .andExpect(model().attribute("uploadStatus", "Словарь не загружен"))
                .andExpect(model().attribute("requestDto", new RequestDto("")));

    }


    @Test
    public void shouldReturnOkResponceOnPost() throws Exception {

        when(service.isFileUploaded()).thenReturn(true);
        this.mockMvc.perform(post("/dictionary_search/find"))
                .andExpect(status().isOk())
                .andExpect(view().name("searchResult"));
    }

    @Test
    public void shouldReturnOkResponceOnPostAndnextTimePleaseWaitPage() throws Exception {
        when(service.isFileUploaded()).thenReturn(false);
        this.mockMvc.perform(post("/dictionary_search/find"))
                .andExpect(status().isOk())
                .andExpect(view().name("nextTimePleaseWait"));
    }


}
