package ru.romanbrazhnikov.agilescraper.parser;

import io.reactivex.Single;

import java.util.Map;
import java.util.Set;

public interface ICommonParser {
    void setPattern(String pattern);

    void setSource(String source);

    void setMatchNames(Set<String> names);

    void setBindings(Map<String, String> bindings);

    Single<ParseResult> parse();
}
