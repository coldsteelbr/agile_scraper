package ru.romanbrazhnikov.agilescraper;

import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.parser.ICommonParser;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;
import ru.romanbrazhnikov.agilescraper.parser.RegExParser;
import ru.romanbrazhnikov.agilescraper.resultsaver.OnSuccessParseConsumerCSV;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpSourceProvider;
import ru.romanbrazhnikov.agilescraper.sourceprovider.ICommonSourceProvider;

import java.util.ArrayList;
import java.util.LinkedList;
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
        final String paramPage = "{[PAGE]}";
        System.out.println("Agile scraper ran");

        // single source provider
        String stringToParse = sValidSource;
        String baseUrl = "http://spran.ru/sell/comm.html";
        String clientEcoding = "utf8";
        HttpMethods method = HttpMethods.GET;
        String params = "currency=1&costMode=1&cities%5B0%5D=21&page=" + paramPage;
        HttpSourceProvider sourceProvider = new HttpSourceProvider();
        sourceProvider.setBaseUrl(baseUrl);
        sourceProvider.setClientCharset(clientEcoding);
        sourceProvider.setHttpMethod(method);
        sourceProvider.setQueryParamString(params.replace(paramPage, "1"));

        // parser
        ICommonParser parser = new RegExParser();
        List<String> matchNames = new LinkedList<>();
        matchNames.add("TYPE");
        matchNames.add("ADDRESS");
        matchNames.add("SQUARE");
        matchNames.add("TOTALPRICE");
        matchNames.add("CONTACT");

        // page count
        final String pageNumName = "PAGENUM";
        int firstPageNum = 1;
        int pageStep = 1;
        String maxPagePattern = "page\\s*=\\s*(?<PAGENUM>[0-9]+?)\">\\s*[0-9]+\\s*<";
        // requesting first page for
        HttpSourceProvider pageCountSourceProvider = new HttpSourceProvider();
        pageCountSourceProvider.setBaseUrl(baseUrl);
        pageCountSourceProvider.setQueryParamString(params);

        List<Integer> tempIntLst = new ArrayList<>();
        pageCountSourceProvider.requestSource()
                .subscribe(source -> {
                    List<String> maxPageNumName = new ArrayList<>();
                    maxPageNumName.add(pageNumName);
                    parser.setMatchNames(maxPageNumName);
                    parser.setPattern(maxPagePattern);
                    parser.setSource(source);
                    parser.parse().map(parseResult -> {
                        int curMax;
                        int totalMax = 0;
                        for(Map<String, String> curRow : parseResult.getResult()){
                            curMax = Integer.parseInt(curRow.get(pageNumName));
                            totalMax = curMax > totalMax ? curMax : totalMax;
                        }
                        return totalMax;
                    }).subscribe((Consumer<Integer>) tempIntLst::add);
                });
        System.out.println("MaxPage: " + tempIntLst.get(0));
        int maxPageValue = tempIntLst.get(0);

        // read all pages
        Consumer<ParseResult> onSuccessConsumer = new OnSuccessParseConsumerCSV(mDestinationName);
        for(int i = firstPageNum; i <= maxPageValue; i++) {
            sourceProvider.setQueryParamString(params.replace(paramPage,String.valueOf(i)));
            sourceProvider.requestSource().subscribe(s -> {
                parser.setMatchNames(matchNames);
                parser.setPattern(mSpranCommPattern);
                parser.setSource(s);
                parser.parse().subscribe(onSuccessConsumer, Throwable::printStackTrace);
            });
        }

    }
}
