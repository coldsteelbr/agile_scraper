package ru.romanbrazhnikov.agilescraper.hardcodedconfigs;

import ru.romanbrazhnikov.agilescraper.requestarguments.RequestArguments;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpMethods;

import java.util.HashMap;
import java.util.Map;

public     class PrimitiveConfiguration {
    // reader
    public long delayInMillis = 334;
    public String baseUrl;
    public String clientEcoding = "utf8";
    public HttpMethods method = HttpMethods.GET;
    // request params
    public String requestParams;


    // markers
    public Map<String, String> markers = new HashMap<>();

    // request args
    public RequestArguments mRequestArguments = new RequestArguments();

    public String mFirstLevelPattern;

    public String mSecondLevelPattern;
    public String secondLevelName = "SECONDLEVEL";
    public String secondLevelBaseUrl;

    public Map<String, String> mFirstLevelBindings;
    public Map<String, String> mSecondLevelBindings;

}