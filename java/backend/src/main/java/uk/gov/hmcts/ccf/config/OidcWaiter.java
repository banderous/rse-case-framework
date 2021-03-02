package uk.gov.hmcts.ccf.config;

import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class OidcWaiter {
    private OidcWaiter() {
    }

    @SneakyThrows
    public static void waitForOidcServer() {
        RetryTemplate template = RetryTemplate.builder()
                .maxAttempts(600)
                .fixedBackoff(1000)
                .retryOn(IOException.class)
                .build();

        template.execute(ctx -> {
            tryOidcServer();
            return true;
        });
    }

    @SneakyThrows
    private static void tryOidcServer() {
        // TODO - make the OIDC client retry nicely.
        String oidcUrl = System.getenv("IDAM_URI");
        if (Strings.isNullOrEmpty(oidcUrl)) {
            oidcUrl = "http://localhost:5000";
        }
        oidcUrl += "/o/.well-known/openid-configuration";
        URL u = new URL(oidcUrl);
        System.out.println("Waiting for " + oidcUrl);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.setRequestMethod("HEAD");
        int code = connection.getResponseCode();
        if (code != 200) {
            throw new IOException(String.valueOf(code));
        }
    }
}
