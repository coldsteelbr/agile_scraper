package ru.romanbrazhnikov.agilescraper.resultsaver;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;

import java.util.Set;

public class OnSuccessParseConsumer implements Consumer<ParseResult> {

    @NonNull
    private final ICommonSaver mSaver;


    public OnSuccessParseConsumer(
            @NonNull final ICommonSaver saver) {
        mSaver = saver;
    }

    @Override
    public void accept(ParseResult parseResult) {

        // todo: free resources by complete
        mSaver.save(parseResult).subscribe().dispose();
    }
}
