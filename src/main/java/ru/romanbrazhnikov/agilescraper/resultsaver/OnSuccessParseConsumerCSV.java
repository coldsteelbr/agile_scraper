package ru.romanbrazhnikov.agilescraper.resultsaver;

import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;

public class OnSuccessParseConsumerCSV implements Consumer<ParseResult> {
    private final String mFileName;

    public OnSuccessParseConsumerCSV(String fileName) {
        mFileName = fileName;
    }

    @Override
    public void accept(ParseResult parseResult) throws Exception {
        ICommonSaver saver = new CsvSaver(mFileName);
        // todo: free resources by complete
        saver.save(parseResult).subscribe();
    }
}
