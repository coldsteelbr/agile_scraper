package ru.romanbrazhnikov.agilescraper;

import ru.romanbrazhnikov.agilescraper.parser.ICommonParser;
import ru.romanbrazhnikov.agilescraper.parser.RegExParser;
import ru.romanbrazhnikov.agilescraper.resultsaver.OnSuccessParseConsumerCSV;

import java.util.ArrayList;
import java.util.List;

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

    private final String mDestinationName = "my_local_db";

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
        parser.parse().subscribe(new OnSuccessParseConsumerCSV(mDestinationName), Throwable::printStackTrace);
    }
}
