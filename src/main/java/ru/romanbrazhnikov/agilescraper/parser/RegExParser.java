package ru.romanbrazhnikov.agilescraper.parser;

import io.reactivex.Single;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
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
                        Pattern.COMMENTS |// Comments and whitespaces permitted
                        Pattern.DOTALL); // ignore ends of lines
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
            InterruptibleCharSequence ICS = new InterruptibleCharSequence(mSource);
            Matcher m = mPattern.matcher(ICS);
            mResultTable.clear();

            // Heavy task handling init
            HeavyCallable heavyMatcher = new HeavyCallable(m);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            boolean value = false;
            Future<Boolean> futureValue;

            String currentKeyToPut;
            String valueToPut;
            while (true) {
                // init future value for matcher
                futureValue = executorService.submit(heavyMatcher);

                // trying to get future result
                try {

                    value = futureValue.get(4, TimeUnit.SECONDS);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    executorService.shutdownNow();
                    futureValue.cancel(true);
                    emitter.onError(e);
                    break;
                } finally {
                    if(!value){
                        executorService.shutdown();
                    }
                }

                if(!value) {
                    break;
                }

                Map<String, String> currentResultRow = new HashMap<>();
                for (String currentName : mGroupNames) {
                    try {
                        // checking for bindings (aliases)
                        if (mBindings != null) {
                            currentKeyToPut = mBindings.get(currentName) == null ? currentName : mBindings.get(currentName);
                        } else {
                            currentKeyToPut = currentName;
                        }
                        valueToPut = m.group(currentName);
                        currentResultRow.put(
                                currentKeyToPut,
                                valueToPut==null?"":valueToPut);
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

    class HeavyCallable implements Callable<Boolean>
    {

        private Matcher mMatcher;

        public HeavyCallable(Matcher matcher) {
            mMatcher = matcher;
        }

        @Override
        public Boolean call() throws Exception {
            return mMatcher.find();
        }
    }


}
