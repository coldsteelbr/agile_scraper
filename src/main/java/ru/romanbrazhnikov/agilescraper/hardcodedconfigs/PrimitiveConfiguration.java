package ru.romanbrazhnikov.agilescraper.hardcodedconfigs;

import ru.romanbrazhnikov.agilescraper.requestarguments.RequestArguments;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;
import ru.romanbrazhnikov.agilescraper.sourceprovider.cookies.Cookies;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveConfiguration {
    public static final String PAGE_NUM_NAME = "PAGENUM";
    public static final String SECOND_LEVEL_NAME = "SECONDLEVEL";
    // reader
    public long delayInMillis = 334;
    public String baseUrl;
    public String clientEcoding = "utf8";
    public HttpMethods method = HttpMethods.GET;
    // request params
    public String requestParams;
    // request args
    public RequestArguments requestArguments = new RequestArguments();

    // pages
    public int firstPageNum = 1;
    public int pageStep = 1;
    public String maxPagePattern = "page\\s*=\\s*(?<" + PAGE_NUM_NAME + ">[0-9]+?)\">\\s*[0-9]+\\s*<";

    // markers
    public Map<String, String> markers = new HashMap<>();

    // cookies
    public Cookies cookies;

    // destination
    public String destinationName;

    // level patterns
    public String firstLevelPattern;
    public String secondLevelPattern;

    // second level specifics
    public String secondLevelName;
    public String secondLevelBaseUrl;

    // level bindings (aliases)
    public Map<String, String> firstLevelBindings;
    public Map<String, String> secondLevelBindings;

}