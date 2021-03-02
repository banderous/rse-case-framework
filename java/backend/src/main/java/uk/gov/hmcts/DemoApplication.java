package uk.gov.hmcts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.hmcts.ccf.config.OidcWaiter;

@SpringBootApplication
@SuppressWarnings({"checkstyle:hideutilityclassconstructor"})
public class DemoApplication {

    public static void main(String[] args) {
        // TODO - make OIDC auto config retry without this.
        OidcWaiter.waitForOidcServer();
        SpringApplication.run(DemoApplication.class, args);
    }

}
