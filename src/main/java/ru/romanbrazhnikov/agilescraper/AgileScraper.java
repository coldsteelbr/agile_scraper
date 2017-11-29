package ru.romanbrazhnikov.agilescraper;

import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.HardcodesConfigFactory;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.PrimitiveConfiguration;
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




    public void run() {

        System.out.println("Agile scraper ran");
        HardcodesConfigFactory configFactory = new HardcodesConfigFactory();
        // creating primitive configuration
        //      Spran comm config
        PrimitiveConfiguration spranCommConfig = configFactory.getSpranCommSell();


        // source provider
        HttpSourceProvider spranCommSourceProvider = new HttpSourceProvider();
        spranCommSourceProvider.setBaseUrl(spranCommConfig.baseUrl);
        spranCommSourceProvider.setClientCharset(spranCommConfig.clientEcoding);
        spranCommSourceProvider.setHttpMethod(spranCommConfig.method);
        // FIXME: Param string must be built for every combination!!!!!!!!!!!!!!!
        spranCommSourceProvider.setQueryParamString(spranCommConfig.requestParams.replace(HardcodesConfigFactory.paramPage, "1"));

        // parser
        ICommonParser parser = new RegExParser();

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
        // FIXME: Param string must be built for every combination!!!!!!!!!!!!!!!
        pageCountSourceProvider.setQueryParamString(
                spranCommConfig.requestParams
                        .replace(HardcodesConfigFactory.paramDistrict, "22")
                        .replace(HardcodesConfigFactory.paramPage, "1"));

        List<Integer> tempIntLst = new ArrayList<>();
        pageCountSourceProvider.requestSource()
                .subscribe(source -> {
                    Set<String> maxPageNumName = new HashSet<>();
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
                spranCommSourceProvider.setQueryParamString(currentParamString.replace(HardcodesConfigFactory.paramPage, String.valueOf(i)));
                try {
                    MILLISECONDS.sleep(spranCommConfig.delayInMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                spranCommSourceProvider.requestSource().subscribe(s -> {
                    parser.setMatchNames(spranCommConfig.mFirstLevelBindings.keySet());
                    parser.setBindings(spranCommConfig.mFirstLevelBindings);
                    parser.setPattern(spranCommConfig.mFirstLevelPattern);
                    parser.setSource(s);
                    parser.parse()
                            .map(parseResult -> {
                                //
                                // ADDING MARKERS and ARGS
                                //
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
                                //
                                //  Second level
                                //
                                if (spranCommConfig.secondLevelName != null && !spranCommConfig.secondLevelName.isEmpty()) {
                                    ICommonParser secondLevelParser = new RegExParser();


                                    for (Map<String, String> curRow : parseResult.getResult()) {
                                        String secondLevelURL = curRow.get(spranCommConfig.secondLevelName);
                                        secondLevelProvider.setBaseUrl(spranCommConfig.secondLevelBaseUrl + secondLevelURL);
                                        secondLevelProvider.requestSource()
                                                .subscribe(secondLevelSource -> {
                                                    secondLevelParser.setSource(secondLevelSource);
                                                    secondLevelParser.setPattern(spranCommConfig.mSecondLevelPattern);
                                                    secondLevelParser.setMatchNames(spranCommConfig.mSecondLevelBindings.keySet());
                                                    secondLevelParser.setBindings(spranCommConfig.mSecondLevelBindings);
                                                    secondLevelParser.parse()
                                                            .subscribe(secondLevelParseResult -> {
                                                                for (Map<String, String> SL_curRow : secondLevelParseResult.getResult()) {
                                                                    for (String curName : spranCommConfig.mSecondLevelBindings.values()) {
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
