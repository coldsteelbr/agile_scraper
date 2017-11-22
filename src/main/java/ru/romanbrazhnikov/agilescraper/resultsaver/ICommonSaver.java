package ru.romanbrazhnikov.agilescraper.resultsaver;

import io.reactivex.Completable;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;

public interface ICommonSaver {
    Completable save(ParseResult parseResult);
}
