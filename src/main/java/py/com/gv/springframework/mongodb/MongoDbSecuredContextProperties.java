package py.com.gv.springframework.mongodb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "spring.data.mongodb.ssl-context")
public class MongoDbSecuredContextProperties {
    private String certificateUrl;

    @ConstructorBinding
    public MongoDbSecuredContextProperties(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }
}
