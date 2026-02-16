package it.tdlight.client;

public class APIToken {
    public final int apiId;
    public final String apiHash;

    public APIToken(int apiId, String apiHash) {
        this.apiId = apiId;
        this.apiHash = apiHash;
    }
}
