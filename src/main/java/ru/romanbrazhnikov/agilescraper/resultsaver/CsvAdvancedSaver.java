package ru.romanbrazhnikov.agilescraper.resultsaver;

import io.reactivex.Completable;
import ru.romanbrazhnikov.commonparsers.ParseResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class CsvAdvancedSaver implements ICommonSaver {
    private String mFileName;
    private File mCSVFile;
    private Path mFilePath;
    private Set<String> mFields;
    private final String mDelimiter = ";";
    private final String mDelimiterReplacement = ",";
    private final String mEndOfLine = "\n";

    public CsvAdvancedSaver(String fileName) {
        mFileName = fileName + ".csv";
        mFilePath = Paths.get(mFileName);
        mCSVFile = mFilePath.toFile();
    }

    private void saveStringToFile(String stringToSave) throws IOException {

        Files.write(mFilePath, stringToSave.getBytes(), CREATE, APPEND);
        System.out.println("stringToSave: \n" + stringToSave);

    }

    @Override
    public void setFields(Set<String> fields) {
        mFields = fields;
    }

    @Override
    public Completable save(ParseResult parseResult) {

        return Completable.create(emitter -> {
            StringBuilder builder = new StringBuilder();
            String currentValue;

            // creating the header of .CSV file if the file does not exist yet
            if (!mCSVFile.exists()) {
                for (String currentField : mFields) {
                    builder.append(currentField).append(mDelimiter);
                }
            }

            // creating rows of .CSV file
            for (Map<String, String> currentRow : parseResult.getResult()) {
                // starting a new row
                builder.append(mEndOfLine);
                // creating a row by appending values separated with ";"
                for (String currentField : mFields) {
                    // getting next value
                    currentValue = currentRow.get(currentField);
                    // appending to the string builder replacing ";" with ","
                    builder
                            .append(currentValue != null ?
                                    currentValue
                                            .replace(mDelimiter, mDelimiterReplacement)
                                            .replaceAll("\\s+", " ")

                                    : "")
                            .append(mDelimiter);
                }
            }

            try {
                saveStringToFile(builder.toString());
            }
            // TODO: Add types of Exception
            catch (Exception ex) {
                emitter.onError(new Exception("TextFileSaver: save: " + ex));
            }
        });

    }
}
