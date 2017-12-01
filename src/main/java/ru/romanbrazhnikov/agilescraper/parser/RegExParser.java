package ru.romanbrazhnikov.agilescraper.parser;

import io.reactivex.Single;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExParser implements ICommonParser {

    private String mPatternRegEx = null;
    private String mSource = null;
    private Set<String> mGroupNames;
    private Map<String, String> mBindings;

    private Pattern mPattern;

    private ParseResult mResultTable = new ParseResult();


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
                        Pattern.COMMENTS |
                        Pattern.DOTALL); // Comments and whitespaces permitted
    }

    @Override
    public void setMatchNames(Set<String> names) {
        mGroupNames = names;
    }

    @Override
    public void setBindings(Map<String, String> bindings) {
        mBindings = bindings;
    }

    private Single<ParseResult> getResult() {
        return Single.create(emitter -> {

            Matcher m = mPattern.matcher(mSource);
            mResultTable.clear();

            String currentKeyToPut;
            while (m.find()) {
                Map<String, String> currentResultRow = new HashMap<>();
                for (String currentName : mGroupNames) {
                    try {
                        // checking for bindings (aliases)
                        if (mBindings != null) {
                            currentKeyToPut = mBindings.get(currentName) == null ? currentName : mBindings.get(currentName);
                        } else {
                            currentKeyToPut = currentName;
                        }

                        currentResultRow.put(
                                currentKeyToPut,
                                m.group(currentName));
                    } catch (Exception e) {
                        emitter.onError(
                                new Exception(
                                        "[RegExParser] Curname:" + currentName +
                                                ", Source: " + mSource +
                                                ", mPatternRegEx:" + mPatternRegEx +
                                                ", Ex Class: " + e.getClass() +
                                                ", Err Message: " + e.getMessage()
                                ));
                    }
                }
                mResultTable.addRow(currentResultRow);
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
