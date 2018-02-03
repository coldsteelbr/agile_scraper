package ru.romanbrazhnikov.agilescraper.resultsaver;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.commonparsers.ParseResult;

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
        mSaver.save(parseResult)
                .subscribe(() -> {
                        }, throwable -> {
                            System.out.println("OnSuccessParserConsumer saving error:");
                            throwable.printStackTrace();
                        }

                ).dispose();
    }
}
