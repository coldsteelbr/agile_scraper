package ru.romanbrazhnikov.agilescraper;

import ru.romanbrazhnikov.agilescraper.parser.ICommonParser;
import ru.romanbrazhnikov.agilescraper.parser.RegExParser;
import ru.romanbrazhnikov.agilescraper.resultsaver.OnSuccessParseConsumerCSV;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpSourceProvider;
import ru.romanbrazhnikov.agilescraper.sourceprovider.ICommonSourceProvider;

import java.util.ArrayList;
import java.util.LinkedList;
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
                    "  <tr>\n" +
                    "    <td class=\"left\">Empty: </td>\n" +
                    "    <td></td>\n" +
                    "  </tr>\n" +
                    "</table>\n";

    private final String mSpranCommPattern =
            "<tr[^>]*>\\s*\n" +
                    "<td[^>]*>\\s*(?<TYPE>.*?)\\s*</td>\\s*\n" +
                    "<td[^>]*>\\s*(?:.*?)</td>\\s*\n" +
                    "<td[^>]*>\\s*<a[^>]*>\\s*(?<ADDRESS>.*?)\\s*</a>\\s*</td>\\s*\n" +
                    "<td[^>]*>\\s*<span[^>]*>\\s*<span[^>]*>\\s*(?<SQUARE>.*?)\\s*</span>\\s*</span>\\s*</td>\\s*\n" +
                    "<td[^>]*>\\s*<span[^>]*>\\s*(?<TOTALPRICE>.*?)\\s*</span>\\s*(?:.*?)</td>\\s*\n" +
                    "<td[^>]*>\\s*(?<CONTACT>.*?)\\s*</td>\\s*";

    private final String mDestinationName = "my_local_db";

    public void run() {
        System.out.println("Agile scraper ran");

        String stringToParse = sValidSource;

        String baseUrl = "http://spran.ru/sell/comm.html";
        String clientEcoding = "utf8";
        HttpMethods method = HttpMethods.GET;
        String params = "currency=1&costMode=1&cities%5B0%5D=21&page=2";
        HttpSourceProvider sourceProvider = new HttpSourceProvider(baseUrl, clientEcoding, method,params);

        ICommonParser parser = new RegExParser();
        List<String> matchNames = new LinkedList<>();
        matchNames.add("TYPE");
        matchNames.add("ADDRESS");
        matchNames.add("SQUARE");
        matchNames.add("TOTALPRICE");
        matchNames.add("CONTACT");


        sourceProvider.requestSource().subscribe(s -> {
            parser.setMatchNames(matchNames);
            parser.setPattern(mSpranCommPattern);
            parser.setSource(s);
            parser.parse().subscribe(new OnSuccessParseConsumerCSV(mDestinationName), Throwable::printStackTrace);
        });


    }
}
