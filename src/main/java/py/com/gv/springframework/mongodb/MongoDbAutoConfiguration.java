package py.com.gv.springframework.mongodb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Configuration
public class MongoDbAutoConfiguration {

    public SSLContext sslContext(KeyStore keyStore) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }

    public KeyStore keyStore(String url) throws Exception {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        InputStream pemInputStream = URI.create(url).toURL().openStream();
        if (pemInputStream == null) {
            throw new IllegalArgumentException("CA file not found");
        }
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, "".toCharArray());

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pemInputStream));
        StringBuilder certContent = new StringBuilder();
        String line;
        int certIndex = 0;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains("BEGIN CERTIFICATE")) {
                certContent = new StringBuilder();
            }
            certContent.append(line).append("\n");
            if (line.contains("END CERTIFICATE")) {
                InputStream certInputStream = new ByteArrayInputStream(certContent.toString().getBytes());
                X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(certInputStream);
                keyStore.setCertificateEntry("sslBundle-" + certIndex, certificate);
                certIndex++;
            }
        }
        bufferedReader.close();

        return keyStore;
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer(@Value("${spring.ssl-context.certificate-url}") String url) throws Exception {
        SSLContext sslContext = sslContext(keyStore(url));
        return clientSettingsBuilder -> clientSettingsBuilder.applyToSslSettings(builder -> builder.context(sslContext));
    }
}
