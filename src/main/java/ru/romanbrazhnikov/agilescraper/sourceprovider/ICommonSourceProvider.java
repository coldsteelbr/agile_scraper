package ru.romanbrazhnikov.agilescraper.sourceprovider;

import io.reactivex.Single;

public interface ICommonSourceProvider {
    Single<String> requestNext();
    boolean hasMore();
}
