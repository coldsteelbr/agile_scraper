package ru.romanbrazhnikov.parser;

import io.reactivex.Single;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class RegExParser implements ICommonParser {

    private String mPatternRegEx = null;
    private String mSource = null;
    private List<String> mGroupNames;
    private long mDelayInMillis;

    private Pattern mPattern;

    private ParseResult mResultTable = new ParseResult();


    public RegExParser() {
        mDelayInMillis = 3000;
    }

    @Override
    public void setSource(String source) {
        mSource = source;
    }

    @Override
    public void setPattern(String pattern) {
        mPatternRegEx = pattern;
        mPattern = Pattern.compile(mPatternRegEx,
                Pattern.CASE_INSENSITIVE | // A=a, B=b...
                        Pattern.UNICODE_CASE | // UNICODE mode on
                        Pattern.COMMENTS); // Comments and whitespaces permitted
    }

    @Override
    public void setMatchNames(List<String> names) {
        mGroupNames = names;
    }

    private Single<ParseResult> getResult() {
        return Single.create(emitter -> {

            Matcher m = mPattern.matcher(mSource);
            mResultTable.clear();

            // TODO: REMOVE SLEEPING
            MILLISECONDS.sleep(mDelayInMillis);

            while (m.find()) {
                Map<String, String> currentResultRow = new HashMap<>();
                for (String currentName : mGroupNames) {
                    currentResultRow.put(currentName, m.group(currentName));
                }
                mResultTable.addRow(currentResultRow);
            }

            if (mResultTable.isEmpty()) {
                emitter.onError(new Exception("Empty result"));
            }
            emitter.onSuccess(mResultTable);

        });
    }

    @Override
    public Single<ParseResult> parse() {

        if (mGroupNames == null)
            return Single.error(new Exception("Matching names are not set"));

        if (mGroupNames.size() == 0)
            return Single.error(new Exception("Matching names count is 0 (zero)"));

        if (mSource == null)
            return Single.error(new Exception("No source set"));


        return getResult();

    }


}
