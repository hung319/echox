package it.tdlight.client;

import it.tdlight.jni.TdApi;
import java.util.function.Consumer;

public class SimpleTelegramClient {
    private final TDLibSettings settings;

    public SimpleTelegramClient(TDLibSettings settings) {
        this.settings = settings;
    }

    public void start(AuthenticationSupplier authenticationSupplier) {
        authenticationSupplier.supply(0L);
    }

    public void send(TdApi.Function query, Consumer<TdApi.Object> handler) {
        handler.accept(new TdApi.Error(400, "TDLight SDK stub in use"));
    }
}
