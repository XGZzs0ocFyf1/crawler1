package ru.gurzhiy.crawler;


import org.junit.jupiter.api.Test;
import ru.gurzhiy.crawler.utils.Utils;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLineHandling {

    @Test
    public void thenGetOneSameWordReturn2(){
        int desiredCriteria2 = Utils.calculateRelevanceCriteriaForRequestAndLine("aaa bbb", "aaa ccc");
        assertEquals(2, desiredCriteria2);
    }

    @Test
    public void thenGetOneSameWordReturn5(){
        int desiredCriteria2 = Utils.calculateRelevanceCriteriaForRequestAndLine("ааа ббб", "ааа ббб");
        assertEquals(5, desiredCriteria2);
    }

    @Test
    public void thenGetTwoWordsInSameOrderReturn5(){
        int criteriaSix1 = Utils.calculateRelevanceCriteriaForRequestAndLine("aaa bbb", "aaa bbb ccc");
        assertEquals(5, criteriaSix1);
    }

    @Test
    public void thenGetTwoWordsInDifferentOrderReturn4(){
        int criteriaSix2 = Utils.calculateRelevanceCriteriaForRequestAndLine("aaa bbb", "aaa ccc bbb");
        assertEquals(4, criteriaSix2);
    }


    @Test
    public void testGetForWordsAndDesired10(){
        String line = "съешь еще этих мягких французских булок";
        String request = "съешь еще этих булок";
        int criteria = Utils.calculateRelevanceCriteriaForRequestAndLine(request, line);
        assertEquals(10, criteria);

    }

    @Test
    public void testGetForWordsAndDesired6(){
        String line = "съешь еще этих мягких французских булок";
        String request = "этих булок ты не съешь";
        int criteria = Utils.calculateRelevanceCriteriaForRequestAndLine(request, line);
        assertEquals(6, criteria);

    }

    @Test
    public void testThatInputWordIsPartOfComplexWordInLineAndDesired0(){
        String line = "съешь еще этих мягких французских булок";
        String request = "француз бул";
        int criteria = Utils.calculateRelevanceCriteriaForRequestAndLine(request, line);
        assertEquals(0, criteria);
    }


    //этот тест написан по результатам прогона сгенерированного файла
    //выявляет повторное срабатывание KP  += 1 на то же слово, которое привело к полной проверке строки
    @Test
    public void testThatThisIsNot3(){
        String line = "нависающий засыпавшийся не кто иной ";
        String request = "конь не валялся";
        int criteria = Utils.calculateRelevanceCriteriaForRequestAndLine(request, line);
        assertEquals(2, criteria);
    }

    @Test
    public void testThatThisIs5(){
        String line = "котик не валялся не ";
        String request = "а конь не валялся";
        int criteria = Utils.calculateRelevanceCriteriaForRequestAndLine(request, line);
        assertEquals(5, criteria);
    }

    @Test
    public void testThatThisIs7(){
        String line = "кот ааа ббб ввв не рыба ";
        String request = "кот не рыба";
        int criteria = Utils.calculateRelevanceCriteriaForRequestAndLine(request, line);
        assertEquals(7, criteria);
    }

    @Test
    public void testThatThisIs5Too(){
        String line = "птица и дельфин это не рыба ";
        String request = "кот не рыба";
        int criteria = Utils.calculateRelevanceCriteriaForRequestAndLine(request, line);
        assertEquals(5, criteria);
    }

    @Test
    public void testThatThisIs5WithDifferentCase(){
        String line = "конь не валялся";
        String request = "Не валялся";
        int criteria = Utils.calculateRelevanceCriteriaForRequestAndLine(request, line);
        assertEquals(5, criteria);
    }



}
