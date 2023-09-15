package it.apeiron.pitagora.core.config;

import it.apeiron.pitagora.core.util.QueryUtils;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SimpleDateFormat simpleDateFormat() {
        return new SimpleDateFormat(QueryUtils.ISO_DATE_PATTERN);
    }

    @Value("${theorems.vedo.dahua.host}")
    private String dahuaHost;
    @Value("${theorems.vedo.dahua.user}")
    private String dahuaUser;
    @Value("${theorems.vedo.dahua.password}")
    private String dahuaPassword;

    @Bean(name = "restTemplateDigest")
    public RestTemplate restTemplateDigest() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(dahuaHost, 80, AuthScope.ANY_REALM),
                new UsernamePasswordCredentials(dahuaUser, dahuaPassword));
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpclient));
        restTemplate.getMessageConverters().set(1,
                new StringHttpMessageConverter(Charset.forName("UTF-8")));

        return restTemplate;
    }

}
