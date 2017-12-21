package ru.romanbrazhnikov.agilescraper.resultsaver;

import io.reactivex.Completable;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DummySaver implements ICommonSaver {
    @Override
    public void setFields(Set<String> fields) {

    }

    @Override
    public Completable save(final ParseResult parseResult) {
        return Completable.create(emitter -> {
            List<Map<String, String>> rows = parseResult.getResult();

            for (Map<String, String> curRow : rows) {
                for (Map.Entry<String, String> curEntry : curRow.entrySet()) {
                    System.out.println(curEntry.getKey() + ": " + curEntry.getValue());
                }
                System.out.println();
            }
        });

    }
}
