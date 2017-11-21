package ru.romanbrazhnikov.parser;

import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.resultsaver.ICommonSaver;

public class SourceSuccessConsumer implements Consumer<String> {

    private final ICommonParser mParser;
    private final ICommonSaver mSaver;

    public SourceSuccessConsumer(ICommonParser parser, ICommonSaver saver) {
        mParser = parser;
        mSaver = saver;
    }

    @Override
    public void accept(String source) throws Exception {
        mParser.setSource(source);
        mParser.parse().subscribe(mSaver::save, System.out::println);
    }
}
