package ru.romanbrazhnikov.agilescraper.sourcereader;

import ru.romanbrazhnikov.agilescraper.requestarguments.Argument;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ParamStringProvider {
    String mParamString;
    Queue<String> mParams = new LinkedList<>();
    List<Argument> mArgumentList;

    public ParamStringProvider(String paramString, List<Argument> arguments) {
        mParamString = paramString;
        mArgumentList = arguments;

    }

    public boolean hasNext() {
        return mParams.size() > 0;
    }

    public String getNext() {
        return mParams.poll();
    }
}
