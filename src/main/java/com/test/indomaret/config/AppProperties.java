package com.test.indomaret.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private final Security security = new Security();
    private final Pagination pagination = new Pagination();
    private final Whitelist whitelist = new Whitelist();
    private final Audit audit = new Audit();

    @Getter
    @Setter
    public static class Security {
        private final Jwt jwt = new Jwt();

        @Getter
        @Setter
        public static class Jwt {
            private String secret = "indomaret-backend-technical-assignment-secret-key-32bytes-min!";
            private long expirationMs = 86400000L; // 24 hours
        }
    }

    @Getter
    @Setter
    public static class Pagination {
        private int defaultPageSize = 20;
        private int maxPageSize = 100;
    }

    @Getter
    @Setter
    public static class Whitelist {
        private boolean alwaysIncludeInSearch = true;
    }

    @Getter
    @Setter
    public static class Audit {
        private boolean enabled = true;
    }
}
