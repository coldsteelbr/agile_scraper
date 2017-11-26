package ru.romanbrazhnikov.agilescraper;

import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.parser.ICommonParser;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;
import ru.romanbrazhnikov.agilescraper.parser.RegExParser;
import ru.romanbrazhnikov.agilescraper.requestarguments.Argument;
import ru.romanbrazhnikov.agilescraper.requestarguments.RequestArguments;
import ru.romanbrazhnikov.agilescraper.requestarguments.Values;
import ru.romanbrazhnikov.agilescraper.resultsaver.OnSuccessParseConsumerCSV;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpSourceProvider;
import ru.romanbrazhnikov.agilescraper.sourcereader.ArgumentedParamString;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AgileScraper {


    private final String mDestinationName = "my_local_db";
    private final String paramPage = "{[PAGE]}";
    private final String paramDistrict = "{[DISTRICT]}";
    private final String fieldDistrict = "DISTRICT";

    class PrimitiveConfiguration {
        // reader
        long delayInMillis = 334;
        String baseUrl;
        String clientEcoding = "utf8";
        HttpMethods method = HttpMethods.GET;
        // request params
        String requestParams;


        // markers
        Map<String, String> markers = new HashMap<>();

        // request args
        RequestArguments mRequestArguments = new RequestArguments();

        String mFirstLevelPattern;

        String mSecondLevelPattern;
        String secondLevelName = "SECONDLEVEL";
        String secondLevelBaseUrl;
    }

    public void run() {

        System.out.println("Agile scraper ran");

        final String paramString = "areas%5B0%5D=" + paramDistrict + "&currency=1&costMode=1&cities%5B0%5D=21&page=" + paramPage;

        // creating primitive configuration
        //      Spran comm config
        PrimitiveConfiguration spranCommConfig = new PrimitiveConfiguration();
        spranCommConfig.baseUrl = "http://spran.ru/sell/comm.html";
        spranCommConfig.requestParams = paramString;
        spranCommConfig.mFirstLevelPattern =
                "<tr[^>]*>\\s*\n" +
                        "<td[^>]*>\\s*(?<TYPE>.*?)\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*(?:.*?)</td>\\s*\n" +
                        "<td[^>]*>\\s*<a(?:.*?)href\\s*=\\s*\"(?<SECONDLEVEL>.*?)\"[^>]*>\\s*(?<ADDRESS>.*?)\\s*</a>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*<span[^>]*>\\s*<span[^>]*>\\s*(?<SQUARE>.*?)\\s*</span>\\s*</span>\\s*</td>\\s*\n" +
                        "<td[^>]*>\\s*<span[^>]*>\\s*(?<TOTALPRICE>.*?)\\s*</span>\\s*(?:.*?)</td>\\s*\n" +
                        "<td[^>]*>\\s*(?<CONTACT>.*?)\\s*</td>\\s*";
        spranCommConfig.mSecondLevelPattern =
                "комментарий\\s*продавца</h[1-6]+>\\s*\n" +
                        "<p[^>]*>\\s*(?<NOTES>.*?)\\s*</p>";

        spranCommConfig.secondLevelBaseUrl = "http://spran.ru";

        // Request arguments
        spranCommConfig.mRequestArguments.requestArguments = new ArrayList<>();
        Argument argumentDistrict = new Argument();
        argumentDistrict.name = paramDistrict;
        argumentDistrict.field = fieldDistrict;
        argumentDistrict.mValues = new ArrayList<>();
        argumentDistrict.mValues.add(new Values("22", "Дзержинский"));
        argumentDistrict.mValues.add(new Values("23", "Железнодорожный"));
        argumentDistrict.mValues.add(new Values("24", "Заельцовский"));
        argumentDistrict.mValues.add(new Values("25", "Килининский"));
        argumentDistrict.mValues.add(new Values("26", "Кировский"));
        argumentDistrict.mValues.add(new Values("27", "Ленинский"));
        argumentDistrict.mValues.add(new Values("29", "Октябрьский"));
        argumentDistrict.mValues.add(new Values("30", "Первомайский"));
        argumentDistrict.mValues.add(new Values("31", "Советский"));
        argumentDistrict.mValues.add(new Values("32", "Центральный"));
        spranCommConfig.mRequestArguments.requestArguments.add(argumentDistrict);
        spranCommConfig.mRequestArguments.initProvider(spranCommConfig.requestParams);

        // Markers
        spranCommConfig.markers.put("CITY", "Новосибирск");
        spranCommConfig.markers.put("MARKET", "ком. продажа");

        HttpSourceProvider spranCommSourceProvider = new HttpSourceProvider();
        spranCommSourceProvider.setBaseUrl(spranCommConfig.baseUrl);
        spranCommSourceProvider.setClientCharset(spranCommConfig.clientEcoding);
        spranCommSourceProvider.setHttpMethod(spranCommConfig.method);
        spranCommSourceProvider.setQueryParamString(spranCommConfig.requestParams.replace(paramPage, "1"));

        // parser
        ICommonParser parser = new RegExParser();
        List<String> matchNames = new LinkedList<>();
        matchNames.add("TYPE");
        matchNames.add("ADDRESS");
        matchNames.add("SQUARE");
        matchNames.add("TOTALPRICE");
        matchNames.add("CONTACT");
        matchNames.add(spranCommConfig.secondLevelName);

        List<String> secondLevelMatchNames = new LinkedList<>();
        secondLevelMatchNames.add("NOTES");



        //
        // page count
        //
        final String pageNumName = "PAGENUM";
        int firstPageNum = 1;
        int pageStep = 1;
        String maxPagePattern = "page\\s*=\\s*(?<" + pageNumName + ">[0-9]+?)\">\\s*[0-9]+\\s*<";
        // requesting first page for
        HttpSourceProvider pageCountSourceProvider = new HttpSourceProvider();
        pageCountSourceProvider.setBaseUrl(spranCommConfig.baseUrl);
        pageCountSourceProvider.setQueryParamString(
                spranCommConfig.requestParams
                        .replace(paramDistrict, "22")
                        .replace(paramPage, "1"));

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
                        int totalMax = firstPageNum;
                        for (Map<String, String> curRow : parseResult.getResult()) {
                            curMax = Integer.parseInt(curRow.get(pageNumName));
                            totalMax = curMax > totalMax ? curMax : totalMax;
                        }
                        return totalMax;
                    }).subscribe((Consumer<Integer>) tempIntLst::add);
                });
        System.out.println("MaxPage: " + tempIntLst.get(0));
        int maxPageValue = tempIntLst.get(0);

        HttpSourceProvider secondLevelProvider = new HttpSourceProvider();


        //
        // PARSING AND SAVING
        // for all request arguments
        do {
            ArgumentedParamString currentArgString = spranCommConfig.mRequestArguments.paramProvider.getCurrent();
            String currentParamString = currentArgString.mParamString;
            // read all pages
            Consumer<ParseResult> onSuccessConsumer = new OnSuccessParseConsumerCSV(mDestinationName);
            for (int i = firstPageNum; i <= maxPageValue; i += pageStep) {
                spranCommSourceProvider.setQueryParamString(currentParamString.replace(paramPage, String.valueOf(i)));
                try {
                    MILLISECONDS.sleep(spranCommConfig.delayInMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                spranCommSourceProvider.requestSource().subscribe(s -> {
                    parser.setMatchNames(matchNames);
                    parser.setPattern(spranCommConfig.mFirstLevelPattern);
                    parser.setSource(s);
                    parser.parse()
                            .map(parseResult -> {
                                System.out.println("Map 1");
                                // ADDING MARKERS and ARGS
                                for (Map<String, String> curRow : parseResult.getResult()) {
                                    for (Map.Entry<String, String> curArg : currentArgString.mFieldsArguments.entrySet()) {
                                        curRow.put(curArg.getKey(), curArg.getValue());
                                    }

                                    for (Map.Entry<String, String> curMarker : spranCommConfig.markers.entrySet()) {
                                        curRow.put(curMarker.getKey(), curMarker.getValue());
                                    }
                                }
                                return parseResult;
                            })

                            .map(parseResult -> {
                                System.out.println("Map 2");
                                if(spranCommConfig.secondLevelName != null && !spranCommConfig.secondLevelName.isEmpty()){
                                    ICommonParser secondLevelParser = new RegExParser();


                                    for(Map<String, String> curRow : parseResult.getResult()){
                                        String secondLevelURL = curRow.get(spranCommConfig.secondLevelName);
                                        secondLevelProvider.setBaseUrl(spranCommConfig.secondLevelBaseUrl + secondLevelURL);
                                        secondLevelProvider.requestSource()
                                                .subscribe(secondLevelSource ->{
                                                    secondLevelParser.setSource(secondLevelSource);
                                                    secondLevelParser.setPattern(spranCommConfig.mSecondLevelPattern);
                                                    secondLevelParser.setMatchNames(secondLevelMatchNames);
                                                    secondLevelParser.parse()
                                                            .subscribe(secondLevelParseResult -> {
                                                                for(Map<String, String> SL_curRow : secondLevelParseResult.getResult()){
                                                                    for(String curName : secondLevelMatchNames){
                                                                        curRow.put(curName, SL_curRow.get(curName));
                                                                    }
                                                                }
                                                            });
                                                });
                                    }
                                }
                                return parseResult;
                            })

                            .subscribe(onSuccessConsumer, Throwable::printStackTrace);
                });
            } // for firstPageNum
        }
        while (spranCommConfig.mRequestArguments.paramProvider.generateNext());// while generateNext
    } // run()
}
