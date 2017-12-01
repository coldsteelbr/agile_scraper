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


    public void run() {

        System.out.println("Agile scraper ran");

        // creating primitive configuration
        // todo: inverse dependency: get as an argument
        HardcodedConfigFactory configFactory = new HardcodedConfigFactory();
        PrimitiveConfiguration scraperConfig = configFactory.getSpranCommSell();
        //PrimitiveConfiguration scraperConfig = configFactory.getProstoTomskCommSell();

        //
        // source provider
        //
        HttpSourceProvider mySourceProvider = new HttpSourceProvider();
        mySourceProvider.setBaseUrl(scraperConfig.baseUrl);
        mySourceProvider.setClientCharset(scraperConfig.clientEcoding);
        mySourceProvider.setHttpMethod(scraperConfig.method);
        // ... cookies
        if (scraperConfig.cookies != null) {
            // custom
            if (scraperConfig.cookies.mCookieList != null) {
                mySourceProvider.setCustomCookies(scraperConfig.cookies.mCookieList);
            }

            // auto
            if (scraperConfig.cookies.mCookieRules != null) {
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
            ArgumentedParamString currentArgString = scraperConfig.requestArguments.paramProvider.getCurrent();
            String currentParamString = currentArgString.mParamString;

            //
            // page count
            //
            final String pageNumName = "PAGENUM";

            // requesting first page for
            HttpSourceProvider pageCountSourceProvider = new HttpSourceProvider();
            pageCountSourceProvider.setBaseUrl(scraperConfig.baseUrl);
            // FIXME: Param string must be built for every combination!!!!!!!!!!!!!!!
            pageCountSourceProvider.setQueryParamString(
                    currentParamString
                            .replace(HardcodedConfigFactory.PARAM_PAGE, String.valueOf(scraperConfig.firstPageNum)));
            if (scraperConfig.cookies != null) {
                if (scraperConfig.cookies.mCookieList != null) {
                    pageCountSourceProvider.setCustomCookies(scraperConfig.cookies.mCookieList);
                }
            }
            List<Integer> tempIntLst = new ArrayList<>();
            pageCountSourceProvider.requestSource()
                    .subscribe(source -> {
                        Set<String> maxPageNumName = new HashSet<>();
                        maxPageNumName.add(pageNumName);
                        parser.setMatchNames(maxPageNumName);
                        parser.setPattern(scraperConfig.maxPagePattern);
                        parser.setSource(source);
                        parser.parse().map(parseResult -> {
                            int curMax;
                            int totalMax = scraperConfig.firstPageNum;
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
            Consumer<ParseResult> onSuccessConsumer = new OnSuccessParseConsumerCSV(scraperConfig.destinationName);
            for (int i = scraperConfig.firstPageNum; i <= maxPageValue; i += scraperConfig.pageStep) {
                mySourceProvider.setQueryParamString(currentParamString.replace(HardcodedConfigFactory.PARAM_PAGE, String.valueOf(i)));
                try {
                    MILLISECONDS.sleep(scraperConfig.delayInMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mySourceProvider.requestSource().subscribe(s -> {
                    parser.setMatchNames(scraperConfig.firstLevelBindings.keySet());
                    parser.setBindings(scraperConfig.firstLevelBindings);
                    parser.setPattern(scraperConfig.firstLevelPattern);
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
                                    if (scraperConfig.markers != null) {
                                        for (Map.Entry<String, String> curMarker : scraperConfig.markers.entrySet()) {
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
                                if (scraperConfig.secondLevelName != null && !scraperConfig.secondLevelName.isEmpty()) {
                                    ICommonParser secondLevelParser = new RegExParser();


                                    for (Map<String, String> curRow : parseResult.getResult()) {
                                        String secondLevelURL = curRow.get(scraperConfig.secondLevelName);
                                        secondLevelProvider.setBaseUrl(scraperConfig.secondLevelBaseUrl + secondLevelURL);
                                        secondLevelProvider.requestSource()
                                                .subscribe(secondLevelSource -> {
                                                    secondLevelParser.setSource(secondLevelSource);
                                                    secondLevelParser.setPattern(scraperConfig.secondLevelPattern);
                                                    secondLevelParser.setMatchNames(scraperConfig.secondLevelBindings.keySet());
                                                    secondLevelParser.setBindings(scraperConfig.secondLevelBindings);
                                                    secondLevelParser.parse()
                                                            .subscribe(secondLevelParseResult -> {
                                                                for (Map<String, String> SL_curRow : secondLevelParseResult.getResult()) {
                                                                    for (String curName : scraperConfig.secondLevelBindings.values()) {
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
        while (scraperConfig.requestArguments.paramProvider.generateNext());// while generateNext
    } // run()
}
