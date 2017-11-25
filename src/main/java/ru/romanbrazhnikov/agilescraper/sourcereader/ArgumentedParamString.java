package ru.romanbrazhnikov.agilescraper.sourcereader;

import java.util.Map;

public class ArgumentedParamString {
    public final String mParamString;
    public final Map<String, String> mFieldsArguments;

    public ArgumentedParamString(String paramString, Map<String, String> fieldsArguments) {
        mParamString = paramString;
        mFieldsArguments = fieldsArguments;
    }
}
