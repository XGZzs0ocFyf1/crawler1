package ru.gurzhiy.crawler.controllers.dto;

import ru.gurzhiy.crawler.model.Pair;

import java.util.List;
import java.util.Objects;

/**
 * класс dto
 * хранит content - содержимое запроса, для которого парсим словарь
 * requestResult - список пар, представляющих собой результаты выполнения запроса
 */
public class RequestDto {

    private String content;
    private List<Pair> requestResult;


    public RequestDto(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Pair> getRequestResult() {
        return requestResult;
    }

    public void setRequestResult(List<Pair> requestResult) {
        this.requestResult = requestResult;
    }

    @Override
    public String toString() {
        return "RequestDto{" +
                "content='" + content + '\'' +
                ", requestResult=" + requestResult +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestDto that = (RequestDto) o;
        return Objects.equals(content, that.content) &&
                Objects.equals(requestResult, that.requestResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, requestResult);
    }
}
