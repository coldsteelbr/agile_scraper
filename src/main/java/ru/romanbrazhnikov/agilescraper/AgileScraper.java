package ru.romanbrazhnikov.agilescraper;

import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.configuration.PrimitiveConfiguration;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.HardcodedConfigFactory;
import ru.romanbrazhnikov.agilescraper.pagecount.PageCountProvider;
import ru.romanbrazhnikov.agilescraper.paramstringgenerator.ArgumentedParamString;
import ru.romanbrazhnikov.agilescraper.resultsaver.CsvAdvancedSaver;
import ru.romanbrazhnikov.agilescraper.resultsaver.ICommonSaver;
import ru.romanbrazhnikov.agilescraper.resultsaver.MySQLSaver;
import ru.romanbrazhnikov.agilescraper.resultsaver.OnSuccessParseConsumer;
import ru.romanbrazhnikov.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.sourceprovider.HttpSourceProvider;
import ru.romanbrazhnikov.sourceprovider.IpPort;
import ru.romanbrazhnikov.agilescraper.utils.FileUtils;
import ru.romanbrazhnikov.circular_queue.CircularQueue;
import ru.romanbrazhnikov.commonparsers.ICommonParser;
import ru.romanbrazhnikov.commonparsers.JSoupXPathParser;
import ru.romanbrazhnikov.commonparsers.ParseResult;
import ru.romanbrazhnikov.commonparsers.RegExParser;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AgileScraper {
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("YYYY-MM-dd");

    enum Savers {
        CSV,
        MYSQL
    }

    private Savers SaverType = Savers.MYSQL;

    private static final String LOGIN_FILE_NAME = "login.txt";
    private static final String DB_NAME = "db_archive_service";
    private String DB_USER = "";
    private String DB_PASSWORD = "";
    private static final String DB_URL = "jdbc:mysql://localhost/" + DB_NAME + "?useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&jdbcCompliantTruncation=false";

    public void run(PrimitiveConfiguration configuration) {

        System.out.println("Agile scraper ran");

        // Reading passwords
        String loginPassword = null;
        try {
            loginPassword = FileUtils.readFromFileToString(LOGIN_FILE_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (loginPassword != null) {
            if (!loginPassword.isEmpty()) {
                String[] logPwd = loginPassword.split("\\s+");
                DB_USER = logPwd[0];
                DB_PASSWORD = logPwd[1];
            }
        }


        CircularQueue<IpPort> ipPortList = readProxyListFromFile(configuration);
        // TODO: make a builder
        HttpSourceProvider mySourceProvider = initHttpSourceProvider(configuration);
        mySourceProvider.setHeaders(configuration.headers);
        if (ipPortList != null) {
            mySourceProvider.setProxyList(ipPortList);
        }

        // init FIRST level parser
        // TODO: invert dependency or build
        ICommonParser firstLevelParser;
        switch (configuration.firstLevelParserType) {
            default:
            case REGEX:
                firstLevelParser = new RegExParser();
                break;
            case XPATH:
                //firstLevelParser = new XPathParser();
                firstLevelParser = new JSoupXPathParser();
                break;
        }
        firstLevelParser.setMatchNames(configuration.firstLevelBindings.keySet());
        firstLevelParser.setBindings(configuration.firstLevelBindings);
        firstLevelParser.setPattern(configuration.firstLevelPattern);

        // init SECOND level parser
        ICommonParser secondLevelParser;
        switch (configuration.secondLevelParserType) {
            default:
            case REGEX:
                secondLevelParser = new RegExParser();
                break;
            case XPATH:
                //secondLevelParser = new XPathParser();
                secondLevelParser = new JSoupXPathParser();
                break;

        }
        if (configuration.secondLevelPattern != null) {
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
        secondLevelProvider.setSourceEncoding(configuration.sourceEncoding);
        if (ipPortList != null) {
            secondLevelProvider.setProxyList(ipPortList);
        }

        // ACTUAL SAVER
        ICommonSaver ACTUAL_SAVER = null;
        switch (SaverType) {
            case CSV:
                // init CSV saver
                ICommonSaver csvSaver = new CsvAdvancedSaver(configuration.destinationName);
                csvSaver.setFields(configuration.getFields());

                ACTUAL_SAVER = csvSaver;
                break;
            case MYSQL:
                // init MySQL saver
                Connection connection = null;
                try {
                    connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                ICommonSaver mysqlSaver = new MySQLSaver(configuration.destinationName, connection);
                // TODO: refactor this Kostyl
                Set<String> fieldsToSet = configuration.getFields();
                fieldsToSet.add(PrimitiveConfiguration.FIELD_DATE);
                fieldsToSet.add(PrimitiveConfiguration.FIELD_SOURCE_NAME);
                mysqlSaver.setFields(fieldsToSet);

                ACTUAL_SAVER = mysqlSaver;
                break;
        }

        // init Success consumer
        Consumer<ParseResult> onSuccessConsumer
                = new OnSuccessParseConsumer(ACTUAL_SAVER);

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
            FinalBuffer<Boolean> isTerminated = new FinalBuffer<>(false);

            // walking through all pages
            for (int i = configuration.firstPageNum; (i <= maxPageValue) && !isTerminated.value; i += configuration.pageStep) {

                // setting params for source provider
                mySourceProvider.setQueryParamString(currentParamString.replace(HardcodedConfigFactory.PARAM_PAGE, String.valueOf(i)));

                // delay
                delayForAWhile(configuration);

                FinalBuffer<String> finalSource = new FinalBuffer<>();
                // requesting source
                mySourceProvider.requestSource().subscribe(source -> {
                    // getting current date

                    final String currentDateString = mDateFormat.format(Calendar.getInstance().getTime());

                    finalSource.value = source;
                    // setting parser
                    firstLevelParser.setSource(source);

                    // parsing
                    firstLevelParser.parse()
                            .timeout(2000, MILLISECONDS)
                            // adding markers and arguments
                            .map(parseResult -> addMarkersAndArguments(configuration, currentArgString, parseResult))
                            // adding source name and date
                            .map(parseResult -> {

                                for (Map<String, String> currentRow : parseResult.getResult()) {
                                    currentRow.put(PrimitiveConfiguration.FIELD_DATE, currentDateString);
                                    currentRow.put(PrimitiveConfiguration.FIELD_SOURCE_NAME, configuration.configName);
                                }

                                return parseResult;
                            })
                            // getting second level if necessary
                            .map((ParseResult parseResult) -> {
                                // if no second level set
                                if (configuration.secondLevelName.equals("")) {
                                    return parseResult;
                                }
                                // get second level
                                return getSecondLevel(configuration, secondLevelParser, secondLevelProvider, parseResult);
                            })
                            .subscribe(onSuccessConsumer, throwable -> {
                                if (throwable instanceof TimeoutException) {
                                    System.out.println("First level parsing TIME OUT ERROR");
                                    throwable.printStackTrace();
                                    isTerminated.value = true;
                                }
                            }).dispose();

                }).dispose();
                if (isTerminated.value) {
                    return;
                }
                // being on the last page - try to refresh the max page value
                if (i == maxPageValue) {
                    //  PAGE COUNT
                    maxPageValue = getMaxPageValue(finalSource.value, configuration.firstPageNum, pageCountParser);
                }
            } // for firstPageNum
        }
        while (configuration.requestArguments.paramProvider.generateNext());// while generateNext
    } // run()

    private CircularQueue<IpPort> readProxyListFromFile(PrimitiveConfiguration configuration) {
        if(!configuration.useProxy){
            return null;
        }

        String proxyListString;
        CircularQueue<IpPort> ipPortList = new CircularQueue<>();
        try {

            proxyListString = FileUtils.readFromFileToString(configuration.proxyListPath);
            String[] splitByEOL = proxyListString.split("\\r?\\n|\\r");
            for (String currentIpPortString : splitByEOL) {
                ipPortList.add(new IpPort(currentIpPortString));
            }

        } catch (IOException e) {
            System.err.println("Can't read proxy list file");
            return null;
        }
        return ipPortList;
    }

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
        ).dispose();
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
                secondLevelURL = curRow.get(PrimitiveConfiguration.FIELD_SUB_URL);
                secondLevelProvider.setBaseUrl(
                        (configuration.secondLevelBaseUrl == null ?
                                "" :
                                configuration.secondLevelBaseUrl)
                                + secondLevelURL);
                //delay
                delayForAWhile(configuration);
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
                                    }).dispose();
                        }).dispose();
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
        mySourceProvider.setUrlDelimiter(configuration.urlDelimiter);
        mySourceProvider.setSourceEncoding(configuration.sourceEncoding);
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
                cookieProvider.setHttpMethod(HttpMethods.valueOf(configuration.cookies.mCookieRules.mRequestCookiesMethod.toUpperCase()));
                cookieProvider.requestSource().subscribe(
                        System.out::println
                ).dispose();

                mySourceProvider.setCookiesHeadersToRequest(cookieProvider.getCookieHeadersFromResponse());
            }
        }
        return mySourceProvider;
    }
}
