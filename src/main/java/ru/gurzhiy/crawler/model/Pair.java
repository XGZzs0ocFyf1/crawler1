package ru.gurzhiy.crawler.model;


/**
 * Класс хранящий пару критерия релевантности и строки
 *
 */
public class Pair implements Comparable<Pair>{

    private int relevanceCriteria;
    private String line;

    public Pair(int relevanceCriteria, String line) {
        this.relevanceCriteria = relevanceCriteria;
        this.line = line;
    }

    public int getRelevanceCriteria() {
        return relevanceCriteria;
    }

    public void setRelevanceCriteria(int relevanceCriteria) {
        this.relevanceCriteria = relevanceCriteria;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public int compareTo(Pair o) {
        return ((Integer)relevanceCriteria).compareTo(o.getRelevanceCriteria());
    }

    @Override
    public String toString() {
        return "Pair{" +
                "relevanceCriteria=" + relevanceCriteria +
                ", line='" + line + '\'' +
                '}';
    }
}
