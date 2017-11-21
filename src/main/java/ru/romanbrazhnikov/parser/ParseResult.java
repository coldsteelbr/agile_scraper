package ru.romanbrazhnikov.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParseResult {
    private List<Map<String, String>> mResult = new ArrayList<>();

    public List<Map<String, String>> getResult() {
        return mResult;
    }

    public void addRow(Map<String, String> row){
        mResult.add(row);
    }

    public void clear(){
        mResult.clear();
    }

    public boolean isEmpty() {
        return mResult.size() == 0;
    }
}
