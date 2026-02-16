package it.tdlight.client;

import java.nio.file.Path;

public class TDLibSettings {
    public Path databaseDirectoryPath;
    public Path downloadedFilesDirectoryPath;
    public final APIToken apiToken;

    private TDLibSettings(APIToken apiToken) {
        this.apiToken = apiToken;
    }

    public static TDLibSettings create(APIToken apiToken) {
        return new TDLibSettings(apiToken);
    }
}
