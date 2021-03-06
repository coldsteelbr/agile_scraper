package ru.romanbrazhnikov.agilescraper.paramstringgenerator;

import ru.romanbrazhnikov.agilescraper.requestarguments.Argument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamStringGenerator {
    private String mParamString;
    private List<Argument> mArgumentList;
    private Integer[] positions;


    public ParamStringGenerator(String paramString, List<Argument> arguments) {
        mParamString = paramString;
        mArgumentList = arguments;
        if (arguments != null) {
            positions = new Integer[mArgumentList.size()];
            for (int i = 0; i < positions.length; i++) {
                positions[i] = 0;
            }
        }
    }

    public boolean generateNext() {
        if (mArgumentList == null)
            return false;
        return rotate(0);
    }

    public ArgumentedParamString getCurrent() {

        String paramToReturn = mParamString;
        Map<String, String> fieldsArguments = new HashMap<>();

        if (mArgumentList != null) {
            String curArgument;
            String curValue;
            for (int i = 0; i < mArgumentList.size(); i++) {
                curArgument = mArgumentList.get(i).name;
                curValue = mArgumentList.get(i).mValues.get(positions[i]).argumentValue;

                paramToReturn = paramToReturn.replace(curArgument, curValue);
                fieldsArguments.put(mArgumentList.get(i).field, mArgumentList.get(i).mValues.get(positions[i]).fieldValue);
            }
        }

        return new ArgumentedParamString(paramToReturn, fieldsArguments);
    }

    private boolean rotate(int listNum) {
        // nothing to rotate
        if (listNum == mArgumentList.size()) {
            return false;
        }

        // if maximum position is reached
        if (positions[listNum] == mArgumentList.get(listNum).mValues.size() - 1) {
            // reset current...
            positions[listNum] = 0;
            // try to rotate next
            return rotate(listNum + 1);
        } else {
            positions[listNum]++;
            return true;
        }
    }

}
