package ru.romanbrazhnikov.agilescraper.requestarguments;

import ru.romanbrazhnikov.agilescraper.paramstringgenerator.ParamStringGenerator;

import java.util.List;

public class RequestArguments {
    public List<Argument> mArgumentList;

    public ParamStringGenerator paramProvider;

    public void initProvider(String requestParams) {
        paramProvider = new ParamStringGenerator(requestParams, mArgumentList);
    }


}

