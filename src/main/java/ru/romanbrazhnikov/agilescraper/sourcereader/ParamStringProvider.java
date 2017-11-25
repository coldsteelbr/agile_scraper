package ru.romanbrazhnikov.agilescraper.sourcereader;

import ru.romanbrazhnikov.agilescraper.requestarguments.Argument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParamStringProvider {
    private String mParamString;
    private List<Argument> mArgumentList;
    private Integer[] positions;


    public ParamStringProvider(String paramString, List<Argument> arguments) {
        mParamString = paramString;
        mArgumentList = arguments;
        positions = new Integer[mArgumentList.size()];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = 0;
        }
    }

    public boolean generateNext() {
        return rotate(0);
    }

    public ArgumentedParamString getCurrent() {

        String paramToReturn = mParamString;
        Map<String, String> fieldsArguments = new HashMap<>();

        String curArgument;
        String curValue;
        for (int i = 0; i < mArgumentList.size(); i++) {
            curArgument = mArgumentList.get(i).name;
            curValue = mArgumentList.get(i).mValues.get(positions[i]).argumentValue;

            paramToReturn = paramToReturn.replace(curArgument, curValue);
            fieldsArguments.put(mArgumentList.get(i).field, mArgumentList.get(i).mValues.get(positions[i]).fieldValue);
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
