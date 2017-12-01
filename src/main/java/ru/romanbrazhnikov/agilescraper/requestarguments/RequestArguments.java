package ru.romanbrazhnikov.agilescraper.requestarguments;

import ru.romanbrazhnikov.agilescraper.sourcereader.ParamStringProvider;

import java.util.List;

public class RequestArguments {
    public List<Argument> mArgumentList;

    public ParamStringProvider paramProvider;

    public void initProvider(String requestParams) {
        paramProvider = new ParamStringProvider(requestParams, mArgumentList);
    }


}

