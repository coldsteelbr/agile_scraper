package ru.romanbrazhnikov.agilescraper.configuration;

import ru.romanbrazhnikov.agilescraper.requestarguments.Argument;
import ru.romanbrazhnikov.agilescraper.requestarguments.RequestArguments;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.agilescraper.sourceprovider.cookies.Cookies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrimitiveConfiguration {
    public static final String PAGE_NUM_NAME = "PAGENUM";
    public static final String SECOND_LEVEL_NAME = "SECONDLEVEL";
    public static final String FIELD_SUB_URL = "sub_url";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_SOURCE_NAME = "source_name";

    public enum Parsers {
        REGEX,
        XPATH
    }

    // config name
    public String configName;

    // reader
    public long delayInMillis = 334;
    public String baseUrl;
    public String urlDelimiter = "";
    public String sourceEncoding = "utf8";
    public HttpMethods method = HttpMethods.GET;

    // request params
    public String requestParams;

    // http headers
    public Map<String, String> httpHeaders;

    // request args
    public RequestArguments requestArguments = new RequestArguments();

    // pages
    public int firstPageNum = 1;
    public int pageStep = 1;
    public String maxPagePattern = "page\\s*=\\s*(?<" + PAGE_NUM_NAME + ">[0-9]+?)\">\\s*[0-9]+\\s*<";

    // markers: field|value pairs
    public Map<String, String> markers = new HashMap<>();

    // http headers
    public Map<String, String> headers;

    // cookies
    public Cookies cookies;

    // destination
    public String destinationName;

    // level patterns
    public String firstLevelPattern;
    public String secondLevelPattern;

    // second level specifics
    public String secondLevelName = null;
    public String secondLevelBaseUrl;

    // parser types
    public Parsers firstLevelParserType = Parsers.REGEX;
    public Parsers secondLevelParserType = Parsers.REGEX;


    // level bindings (aliases)
    public Map<String, String> firstLevelBindings;
    public Map<String, String> secondLevelBindings;

    public PrimitiveConfiguration() {
    }

    public PrimitiveConfiguration(
            String configName,
            String baseUrl,
            String urlDelimiter,
            HttpMethods method,
            String requestParams,
            int firstPageNum,
            int pageStep,
            String maxPagePattern,
            String destinationName,
            String firstLevelPattern,
            String secondLevelPattern,
            String secondLevelName,
            String secondLevelBaseUrl,
            String encoding,
            int delayInMillis) {
        this.configName = configName;
        this.baseUrl = baseUrl;
        this.urlDelimiter = urlDelimiter;
        this.method = method;
        this.requestParams = requestParams;
        this.firstPageNum = firstPageNum;
        this.pageStep = pageStep;
        this.maxPagePattern = maxPagePattern;
        this.destinationName = destinationName;
        this.firstLevelPattern = firstLevelPattern;
        this.secondLevelPattern = secondLevelPattern;
        this.secondLevelName = secondLevelName;
        this.secondLevelBaseUrl = secondLevelBaseUrl;
        this.sourceEncoding = encoding;
        this.delayInMillis = delayInMillis;
    }

    public Set<String> getFields() {
        // init
        Set<String> fields = new HashSet<>();

        // adding 1st level bindings
        fields.addAll(firstLevelBindings.values());

        // adding 2nd level bindings
        if (secondLevelBindings != null) {
            fields.addAll(secondLevelBindings.values());
        }

        // adding markers
        fields.addAll(markers.keySet());

        // adding arguments
        for (Argument currentArg : requestArguments.mArgumentList) {
            fields.add(currentArg.field);
        }

        return fields;
    }
}