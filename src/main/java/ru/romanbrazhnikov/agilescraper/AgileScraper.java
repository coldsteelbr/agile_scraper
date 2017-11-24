package ru.romanbrazhnikov.agilescraper;

import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.parser.ICommonParser;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;
import ru.romanbrazhnikov.agilescraper.parser.RegExParser;
import ru.romanbrazhnikov.agilescraper.resultsaver.OnSuccessParseConsumerCSV;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpSourceProvider;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AgileScraper {

    private final String mSpranCommPattern =
            "<tr[^>]*>\\s*\n" +
                    "<td[^>]*>\\s*(?<TYPE>.*?)\\s*</td>\\s*\n" +
                    "<td[^>]*>\\s*(?:.*?)</td>\\s*\n" +
                    "<td[^>]*>\\s*<a[^>]*>\\s*(?<ADDRESS>.*?)\\s*</a>\\s*</td>\\s*\n" +
                    "<td[^>]*>\\s*<span[^>]*>\\s*<span[^>]*>\\s*(?<SQUARE>.*?)\\s*</span>\\s*</span>\\s*</td>\\s*\n" +
                    "<td[^>]*>\\s*<span[^>]*>\\s*(?<TOTALPRICE>.*?)\\s*</span>\\s*(?:.*?)</td>\\s*\n" +
                    "<td[^>]*>\\s*(?<CONTACT>.*?)\\s*</td>\\s*";

    private final String mDestinationName = "my_local_db";
    private final String paramPage = "{[PAGE]}";

    class PrimitiveConfiguration {
        // reader
        String baseUrl;
        String clientEcoding = "utf8";
        HttpMethods method = HttpMethods.GET;
        String params;
        long delayInMillis = 334;

        // markers
        Map<String, String> markers = new HashMap<>();


        // max page request
        String maxPageBaseUrl;
        String maxPageParams;
        HttpMethods maxPageMethod = HttpMethods.GET;
    }

    public void run() {

        System.out.println("Agile scraper ran");

        // creating primitive configuration
        //      Spran comm config
        PrimitiveConfiguration spranCommConfig = new PrimitiveConfiguration();
        spranCommConfig.baseUrl = "http://spran.ru/sell/comm.html";
        spranCommConfig.params = "currency=1&costMode=1&cities%5B0%5D=21&page=" + paramPage;
        spranCommConfig.maxPageBaseUrl = "http://spran.ru/sell/comm.html?";
        spranCommConfig.maxPageParams = "currency=1&costMode=1&cities%5B0%5D=21&page=1";
        spranCommConfig.markers.put("DISTRICT", "Октярбрьский");
        spranCommConfig.markers.put("CITY", "Новосибирск");

        HttpSourceProvider spranCommSourceProvider = new HttpSourceProvider();
        spranCommSourceProvider.setBaseUrl(spranCommConfig.baseUrl);
        spranCommSourceProvider.setClientCharset(spranCommConfig.clientEcoding);
        spranCommSourceProvider.setHttpMethod(spranCommConfig.method);
        spranCommSourceProvider.setQueryParamString(spranCommConfig.params.replace(paramPage, "1"));

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
        String maxPagePattern = "page\\s*=\\s*(?<" + pageNumName + ">[0-9]+?)\">\\s*[0-9]+\\s*<";
        // requesting first page for
        HttpSourceProvider pageCountSourceProvider = new HttpSourceProvider();
        pageCountSourceProvider.setBaseUrl(spranCommConfig.maxPageBaseUrl);
        pageCountSourceProvider.setQueryParamString(spranCommConfig.maxPageParams);

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
                        for (Map<String, String> curRow : parseResult.getResult()) {
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
        for (int i = firstPageNum; i <= maxPageValue; i++) {
            spranCommSourceProvider.setQueryParamString(spranCommConfig.params.replace(paramPage, String.valueOf(i)));
            try {
                MILLISECONDS.sleep(spranCommConfig.delayInMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spranCommSourceProvider.requestSource().subscribe(s -> {
                parser.setMatchNames(matchNames);
                parser.setPattern(mSpranCommPattern);
                parser.setSource(s);
                parser.parse()
                        .map(parseResult -> {
                            // ADDING MARKERS
                            for (Map<String, String> curRow : parseResult.getResult()) {
                                for(Map.Entry<String, String> curMarker: spranCommConfig.markers.entrySet()){
                                    curRow.put(curMarker.getKey(), curMarker.getValue());
                                }
                            }
                            return parseResult;
                        })
                        .subscribe(onSuccessConsumer, Throwable::printStackTrace);
            });
        }

    }
}
