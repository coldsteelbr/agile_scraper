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

/**
 * Saves result to file
 */
public class TextFileSaver implements ICommonSaver {

    private String mFileName;

    public TextFileSaver(String fileName) {
        mFileName = fileName;
    }

    private void saveStringToFile(String stringToSave) throws IOException {
        Path file = Paths.get(mFileName);
        Files.write(file, stringToSave.getBytes(), CREATE, APPEND);
        System.out.println("stringToSave: \n" + stringToSave);

    }

    @Override
    public void setFields(Set<String> fields) {

    }

    @Override
    public Completable save(ParseResult parseResult) {
        return Completable.create(emitter -> {
            StringBuffer buffer = new StringBuffer();
            for (Map<String, String> currentRow : parseResult.getResult()) {
                for (Map.Entry currentValue : currentRow.entrySet()) {
                    buffer
                            .append(currentValue.getValue())
                            .append(" ");
                }
                buffer.append("\n");
            }

            try {
                saveStringToFile(buffer.toString());
            }
            // TODO: Add types of Exception
            catch (Exception ex) {
                emitter.onError(new Exception("TextFileSaver: save: " + ex));
            }
        });

    }
}
