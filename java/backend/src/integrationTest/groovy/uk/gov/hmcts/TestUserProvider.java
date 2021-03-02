package uk.gov.hmcts;

import uk.gov.hmcts.ccf.config.UserProvider;

public class TestUserProvider implements UserProvider {
    private final String id;

    public TestUserProvider(String id) {
        this.id = id;
    }

    @Override
    public String getCurrentUserId() {
        return id;
    }
}
