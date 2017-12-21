package ru.romanbrazhnikov.agilescraper.resultsaver;

import io.reactivex.Completable;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;

import java.util.Set;

public interface ICommonSaver {
    void setFields(Set<String> fields);

    Completable save(ParseResult parseResult);
}
