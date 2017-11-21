package ru.romanbrazhnikov.agilescraper;

import ru.romanbrazhnikov.parser.ICommonParser;
import ru.romanbrazhnikov.parser.RegExParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgileScraper {

    private final String sValidPattern
            = "<td[^>]*>\\s*(?<LEFT>.*?)\\s*</td>\\s*"
            + "<td[^>]*>\\s*(?<RIGHT>.*?)\\s*</td>\\s*";

    private final String sValidSource =
            "<table>\n" +
                    "  <tr>\n" +
                    "    <td class=\"left\">Car: </td>\n" +
                    "    <td>Mazda</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td class=\"left\">Glasses: </td>\n" +
                    "    <td>Optic 7</td>\n" +
                    "  </tr>\n" +
                    "</table>\n";


    public void run() {
        System.out.println("Agile scraper ran");

        String stringToParse = sValidSource;

        ICommonParser parser = new RegExParser();
        List<String> matchNames = new ArrayList<>();
        matchNames.add("LEFT");
        matchNames.add("RIGHT");
        parser.setMatchNames(matchNames);
        parser.setPattern(sValidPattern);
        parser.setSource(stringToParse);
        parser.parse().subscribe(parseResult -> {
            for(Map<String, String> currentRow : parseResult.getResult()){
                for(Map.Entry<String, String> curEntry : currentRow.entrySet()){
                    System.out.println(curEntry.getValue() + " ");
                }
                System.out.println();
            }
        });
    }
}
