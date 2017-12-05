package ru.romanbrazhnikov.agilescraper;

import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.HardcodedConfigFactory;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.PrimitiveConfiguration;
import ru.romanbrazhnikov.agilescraper.pagecount.PageCountProvider;
import ru.romanbrazhnikov.agilescraper.paramstringgenerator.ArgumentedParamString;
import ru.romanbrazhnikov.agilescraper.parser.ICommonParser;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;
import ru.romanbrazhnikov.agilescraper.parser.RegExParser;
import ru.romanbrazhnikov.agilescraper.resultsaver.OnSuccessParseConsumerCSV;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpSourceProvider;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AgileScraper {


    public void run(PrimitiveConfiguration configuration) {

        System.out.println("Agile scraper ran");

        // TODO: make a builder
        HttpSourceProvider mySourceProvider = initHttpSourceProvider(configuration);


        // init FIRST level parser
        // TODO: invert dependency or build
        ICommonParser firstLevelParser = new RegExParser();
        firstLevelParser.setMatchNames(configuration.firstLevelBindings.keySet());
        firstLevelParser.setBindings(configuration.firstLevelBindings);
        firstLevelParser.setPattern(configuration.firstLevelPattern);

        // init SECOND level parser
        ICommonParser secondLevelParser = new RegExParser();
        if(configuration.secondLevelPattern != null) {
            secondLevelParser.setPattern(configuration.secondLevelPattern);
            secondLevelParser.setMatchNames(configuration.secondLevelBindings.keySet());
            secondLevelParser.setBindings(configuration.secondLevelBindings);
        }

        // init PAGE count parser
        ICommonParser pageCountParser = new RegExParser();
        Set<String> maxPageNumName = new HashSet<>();
        maxPageNumName.add(PrimitiveConfiguration.PAGE_NUM_NAME);
        pageCountParser.setMatchNames(maxPageNumName);
        pageCountParser.setPattern(configuration.maxPagePattern);

        // init second level provider
        HttpSourceProvider secondLevelProvider = new HttpSourceProvider();

        //
        // PARSING AND SAVING
        // for all request arguments
        do {
            // getting current argumented string
            ArgumentedParamString currentArgString = configuration.requestArguments.paramProvider.getCurrent();
            String currentParamString = currentArgString.mParamString;

            //
            //  PAGE COUNT
            //
            int maxPageValue = configuration.firstPageNum;


            //
            // READING ALL PAGES
            //

            // init Success consumer
            Consumer<ParseResult> onSuccessConsumer = new OnSuccessParseConsumerCSV(configuration.destinationName);

            // walking through all pages
            for (int i = configuration.firstPageNum; i <= maxPageValue; i += configuration.pageStep) {

                // setting params for source provider
                mySourceProvider.setQueryParamString(currentParamString.replace(HardcodedConfigFactory.PARAM_PAGE, String.valueOf(i)));

                // delay
                delayForAWhile(configuration);

                FinalBuffer<String> finalSource = new FinalBuffer<>();
                // requesting source
                mySourceProvider.requestSource().subscribe(source -> {
                    finalSource.value = source;
                    // setting parser
                    firstLevelParser.setSource(source);

                    // parsing
                    firstLevelParser.parse()
                            // adding markers and arguments
                            .map(parseResult -> addMarkersAndArguments(configuration, currentArgString, parseResult))
                            // getting second level if necessary
                            .map(parseResult -> getSecondLevel(configuration, secondLevelParser, secondLevelProvider, parseResult))
                            // consuming the result
                            .subscribe(onSuccessConsumer, Throwable::printStackTrace);
                });

                // being on the last page - try to refresh the max page value
                if(i == maxPageValue){
                    //  PAGE COUNT
                    maxPageValue = getMaxPageValue(finalSource.value, configuration.firstPageNum, pageCountParser);
                }
            } // for firstPageNum
        }
        while (configuration.requestArguments.paramProvider.generateNext());// while generateNext
    } // run()

    private void delayForAWhile(PrimitiveConfiguration configuration) {
        // delay before starting requests
        try {
            MILLISECONDS.sleep(configuration.delayInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int getMaxPageValue(String source, int firstPageNum, ICommonParser pageCountParser) {
        // init
        int maxPageValue;
        FinalBuffer<Integer> maxPageFinal = new FinalBuffer<>();

        // page count provider
        PageCountProvider pageCountProvider = new PageCountProvider(source, firstPageNum, pageCountParser);
        pageCountProvider.getPageCount().subscribe(
                integer -> maxPageFinal.value = integer,
                throwable -> {
                    // TODO: catch errors
                    maxPageFinal.value = firstPageNum;
                }
        );
        maxPageValue = maxPageFinal.value;
        System.out.println("Page count: " + maxPageValue);
        return maxPageValue;
    }

    private ParseResult getSecondLevel(PrimitiveConfiguration configuration, ICommonParser secondLevelParser, HttpSourceProvider secondLevelProvider, ParseResult parseResult) {
        //
        //  Second level
        //
        // if there's need for second level...
        if (configuration.secondLevelName != null && !configuration.secondLevelName.isEmpty()) {
            String secondLevelURL;
            // getting second level for each first level row
            for (Map<String, String> curRow : parseResult.getResult()) {
                secondLevelURL = curRow.get(configuration.secondLevelName);
                secondLevelProvider.setBaseUrl(configuration.secondLevelBaseUrl + secondLevelURL);

                // requesting second level
                secondLevelProvider.requestSource()
                        .subscribe(secondLevelSource -> {
                            // parsing second level
                            secondLevelParser.setSource(secondLevelSource);
                            secondLevelParser.parse()
                                    .subscribe(secondLevelParseResult -> {
                                        // adding second level result to a total result
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
    }

    private ParseResult addMarkersAndArguments(PrimitiveConfiguration configuration, ArgumentedParamString currentArgString, ParseResult parseResult) {
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
    }

    private HttpSourceProvider initHttpSourceProvider(PrimitiveConfiguration configuration) {
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
                HttpSourceProvider cookieProvider = new HttpSourceProvider();
                cookieProvider.setBaseUrl(configuration.cookies.mCookieRules.mRequestCookiesAddress);
                cookieProvider.setQueryParamString(configuration.cookies.mCookieRules.mRequestCookiesParamString);
                cookieProvider.setHttpMethod(HttpMethods.valueOf(configuration.cookies.mCookieRules.mRequestCookiesMethod));
                cookieProvider.requestSource().subscribe(
                        System.out::println
                ).dispose();

                mySourceProvider.setCookiesHeadersToRequest(cookieProvider.getCookieHeadersFromResponse());
            }
        }
        return mySourceProvider;
    }
}
