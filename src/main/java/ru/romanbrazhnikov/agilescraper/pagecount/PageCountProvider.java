package ru.romanbrazhnikov.agilescraper.pagecount;

import io.reactivex.Single;
import ru.romanbrazhnikov.agilescraper.FinalBuffer;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.PrimitiveConfiguration;
import ru.romanbrazhnikov.agilescraper.parser.ICommonParser;

import java.util.Map;

public class PageCountProvider {
    private String mSource;
    private ICommonParser mParser;
    private int mFirstPageNum;

    public PageCountProvider(String source, int firstPageNum, ICommonParser parser) {
        mSource = source;
        mParser = parser;
        mFirstPageNum = firstPageNum;
    }

    public Single<Integer> getPageCount() {
        //
        // page count
        //
        FinalBuffer<Integer> finalPageInt = new FinalBuffer<>();
        finalPageInt.value = 0;
        return Single.create(emitter -> {

            mParser.setSource(mSource);
            mParser.parse().map(parseResult -> {
                int curMax;
                int totalMax = mFirstPageNum;
                for (Map<String, String> curRow : parseResult.getResult()) {
                    curMax = Integer.parseInt(curRow.get(PrimitiveConfiguration.PAGE_NUM_NAME));
                    totalMax = curMax > totalMax ? curMax : totalMax;
                }
                return totalMax;
            }).subscribe(integer -> finalPageInt.value = integer);

            emitter.onSuccess(finalPageInt.value);

        });

    }
}
