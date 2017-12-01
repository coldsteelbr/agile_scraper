package ru.romanbrazhnikov.agilescraper;

import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.HardcodedConfigFactory;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.PrimitiveConfiguration;
import ru.romanbrazhnikov.agilescraper.parser.ICommonParser;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;
import ru.romanbrazhnikov.agilescraper.parser.RegExParser;
import ru.romanbrazhnikov.agilescraper.resultsaver.OnSuccessParseConsumerCSV;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpSourceProvider;
import ru.romanbrazhnikov.agilescraper.sourcereader.ArgumentedParamString;

import java.util.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AgileScraper {


    public void run(PrimitiveConfiguration configuration) {

        System.out.println("Agile scraper ran");


        //
        // source provider
        //
        HttpSourceProvider mySourceProvider = new HttpSourceProvider();
        mySourceProvider.setBaseUrl(configuration.baseUrl);
        mySourceProvider.setClientCharset(configuration.clientEcoding);
        mySourceProvider.setHttpMethod(configuration.method);
        // ... cookies
        if (configuration.cookies != null) {
            // custom
            if (configuration.cookies.mCookieList != null) {
                mySourceProvider.setCustomCookies(configuration.cookies.mCookieList);
            }

            // auto
            if (configuration.cookies.mCookieRules != null) {
                // request necessary page and get cookie headers from response
            }
        }

        //
        // parser
        //
        ICommonParser parser = new RegExParser();


        HttpSourceProvider secondLevelProvider = new HttpSourceProvider();


        //
        // PARSING AND SAVING
        // for all request arguments
        do {
            ArgumentedParamString currentArgString = configuration.requestArguments.paramProvider.getCurrent();
            String currentParamString = currentArgString.mParamString;

            //
            // page count
            //
            final String pageNumName = "PAGENUM";

            // requesting first page for
            HttpSourceProvider pageCountSourceProvider = new HttpSourceProvider();
            pageCountSourceProvider.setBaseUrl(configuration.baseUrl);
            // FIXME: Param string must be built for every combination!!!!!!!!!!!!!!!
            pageCountSourceProvider.setQueryParamString(
                    currentParamString
                            .replace(HardcodedConfigFactory.PARAM_PAGE, String.valueOf(configuration.firstPageNum)));
            if (configuration.cookies != null) {
                if (configuration.cookies.mCookieList != null) {
                    pageCountSourceProvider.setCustomCookies(configuration.cookies.mCookieList);
                }
            }
            List<Integer> tempIntLst = new ArrayList<>();
            pageCountSourceProvider.requestSource()
                    .subscribe(source -> {
                        Set<String> maxPageNumName = new HashSet<>();
                        maxPageNumName.add(pageNumName);
                        parser.setMatchNames(maxPageNumName);
                        parser.setPattern(configuration.maxPagePattern);
                        parser.setSource(source);
                        parser.parse().map(parseResult -> {
                            int curMax;
                            int totalMax = configuration.firstPageNum;
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
            Consumer<ParseResult> onSuccessConsumer = new OnSuccessParseConsumerCSV(configuration.destinationName);
            for (int i = configuration.firstPageNum; i <= maxPageValue; i += configuration.pageStep) {
                mySourceProvider.setQueryParamString(currentParamString.replace(HardcodedConfigFactory.PARAM_PAGE, String.valueOf(i)));
                try {
                    MILLISECONDS.sleep(configuration.delayInMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mySourceProvider.requestSource().subscribe(s -> {
                    parser.setMatchNames(configuration.firstLevelBindings.keySet());
                    parser.setBindings(configuration.firstLevelBindings);
                    parser.setPattern(configuration.firstLevelPattern);
                    parser.setSource(s);
                    parser.parse()
                            .map(parseResult -> {
                                //
                                // ADDING MARKERS and ARGS
                                //
                                for (Map<String, String> curRow : parseResult.getResult()) {
                                    if (currentArgString.mFieldsArguments != null) {
                                        for (Map.Entry<String, String> curArg : currentArgString.mFieldsArguments.entrySet()) {
                                            curRow.put(curArg.getKey(), curArg.getValue());
                                        }
                                    }
                                    if (configuration.markers != null) {
                                        for (Map.Entry<String, String> curMarker : configuration.markers.entrySet()) {
                                            curRow.put(curMarker.getKey(), curMarker.getValue());
                                        }
                                    }
                                }
                                return parseResult;
                            })

                            .map(parseResult -> {
                                //
                                //  Second level
                                //
                                if (configuration.secondLevelName != null && !configuration.secondLevelName.isEmpty()) {
                                    ICommonParser secondLevelParser = new RegExParser();


                                    for (Map<String, String> curRow : parseResult.getResult()) {
                                        String secondLevelURL = curRow.get(configuration.secondLevelName);
                                        secondLevelProvider.setBaseUrl(configuration.secondLevelBaseUrl + secondLevelURL);
                                        secondLevelProvider.requestSource()
                                                .subscribe(secondLevelSource -> {
                                                    secondLevelParser.setSource(secondLevelSource);
                                                    secondLevelParser.setPattern(configuration.secondLevelPattern);
                                                    secondLevelParser.setMatchNames(configuration.secondLevelBindings.keySet());
                                                    secondLevelParser.setBindings(configuration.secondLevelBindings);
                                                    secondLevelParser.parse()
                                                            .subscribe(secondLevelParseResult -> {
                                                                for (Map<String, String> SL_curRow : secondLevelParseResult.getResult()) {
                                                                    for (String curName : configuration.secondLevelBindings.values()) {
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
        while (configuration.requestArguments.paramProvider.generateNext());// while generateNext
    } // run()
}
