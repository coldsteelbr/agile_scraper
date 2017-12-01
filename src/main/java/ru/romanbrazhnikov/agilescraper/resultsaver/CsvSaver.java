package ru.romanbrazhnikov.agilescraper.resultsaver;

import io.reactivex.Completable;
import ru.romanbrazhnikov.agilescraper.parser.ParseResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class CsvSaver implements ICommonSaver {
    private String mFileName;
    private final String mDelimiter = ";";
    private final String mDelimiterReplacement = ",";
    private final String mEndOfLine = "\n";

    public CsvSaver(String fileName) {
        mFileName = fileName + ".csv";
    }

    private void saveStringToFile(String stringToSave) throws IOException {
        Path file = Paths.get(mFileName);
        Files.write(file, stringToSave.getBytes(), CREATE, APPEND);
        System.out.println("stringToSave: \n" + stringToSave);

    }

    @Override
    public Completable save(ParseResult parseResult) {

        // get list of matching names
        final Set<String> fields = parseResult.getMatchingNames();


        return Completable.create(emitter -> {
            StringBuilder builder = new StringBuilder();
            String currentValue;

            // creating the header of .CSV file
            for (String currentField : fields) {
                builder.append(currentField).append(mDelimiter);
            }
            builder.append(mEndOfLine);
            // creating rows of .CSV file
            for (Map<String, String> currentRow : parseResult.getResult()) {
                // creating a row by appending values separated with ";"
                for (String currentField : fields) {
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
                builder.append(mEndOfLine);
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
