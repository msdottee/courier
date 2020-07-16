package io.github.msdottee.courier.configuration;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonS3Configuration {

    @Value("${aws.s3.endpoint:@null}")
    private String awsS3Endpoint;

    @Value("${aws.s3.region:@null}")
    private String awsS3Region;

    @Value("${aws.s3.accessKeyId:@null}")
    private String awsS3AccessKeyId;

    @Value("${aws.s3.secretKey:@null}")
    private String awsS3SecretKey;

    @Bean
    public AmazonS3 amazonS3() {
        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard();

        amazonS3ClientBuilder.withPathStyleAccessEnabled(true);

        if (null != awsS3Endpoint) {
            if (null == awsS3Region) {
                throw new IllegalArgumentException("Endpoint cannot be configured without configuring a region.");
            }

            amazonS3ClientBuilder.withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(awsS3Endpoint, awsS3Region));
        }

        if (null != awsS3Region) {
            System.setProperty("aws.region", awsS3Region);
        }

        if (null != awsS3AccessKeyId) {
            System.setProperty("aws.accessKeyId", awsS3AccessKeyId);
        }

        if (null != awsS3SecretKey) {
            System.setProperty("aws.secretKey", awsS3SecretKey);
        }

        return amazonS3ClientBuilder.build();
    }
}
