package ru.romanbrazhnikov.agilescraper.pagecount;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.HardcodedConfigFactory;
import ru.romanbrazhnikov.agilescraper.hardcodedconfigs.PrimitiveConfiguration;
import ru.romanbrazhnikov.agilescraper.parser.ICommonParser;
import ru.romanbrazhnikov.agilescraper.sourceprovider.HttpSourceProvider;

import java.util.*;

public class PageCountProvider {
    private PrimitiveConfiguration mConfiguration;
    private ICommonParser mParser;
    private String mParamString;

    public PageCountProvider(PrimitiveConfiguration configuration, ICommonParser parser, String paramString) {
        mConfiguration = configuration;
        mParser = parser;
        mParamString = paramString;
    }

    public Single<Integer> getPageCount() {
        //
        // page count
        //

        // requesting first page for
        HttpSourceProvider pageCountSourceProvider = new HttpSourceProvider();
        pageCountSourceProvider.setBaseUrl(mConfiguration.baseUrl);
        pageCountSourceProvider.setQueryParamString(
                mParamString
                        .replace(HardcodedConfigFactory.PARAM_PAGE, String.valueOf(mConfiguration.firstPageNum)));
        if (mConfiguration.cookies != null) {
            if (mConfiguration.cookies.mCookieList != null) {
                pageCountSourceProvider.setCustomCookies(mConfiguration.cookies.mCookieList);
            }
        }
        List<Integer> tempIntLst = new ArrayList<>();
        return Single.create(emitter -> {
            pageCountSourceProvider.requestSource()
                    .subscribe(source -> {
                        mParser.setSource(source);
                        mParser.parse().map(parseResult -> {
                            int curMax;
                            int totalMax = mConfiguration.firstPageNum;
                            for (Map<String, String> curRow : parseResult.getResult()) {
                                curMax = Integer.parseInt(curRow.get(PrimitiveConfiguration.PAGE_NUM_NAME));
                                totalMax = curMax > totalMax ? curMax : totalMax;
                            }
                            return totalMax;
                        }).subscribe((Consumer<Integer>) tempIntLst::add);
                        if (tempIntLst.size() == 1) {
                            emitter.onSuccess(tempIntLst.get(0));
                        } else {
                            emitter.onSuccess(0);
                        }
                    });
        });

    }
}
