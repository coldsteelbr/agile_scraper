package ru.romanbrazhnikov.agilescraper.hardcodedconfigs;

import ru.romanbrazhnikov.agilescraper.requestarguments.RequestArguments;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;

import java.util.HashMap;
import java.util.Map;

public     class PrimitiveConfiguration {
    public static final String PAGE_NUM_NAME = "PAGENUM";

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

    public String destinationName;
    public String firstLevelPattern;

    public String secondLevelPattern;
    public String secondLevelName = "SECONDLEVEL";
    public String secondLevelBaseUrl;

    public Map<String, String> firstLevelBindings;
    public Map<String, String> secondLevelBindings;

}