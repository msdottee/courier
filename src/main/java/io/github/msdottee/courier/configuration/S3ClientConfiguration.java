package io.github.msdottee.courier.configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3ClientConfiguration {

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

        /*
        S3ClientBuilder s3ClientBuilder = S3Client.builder();
        if (null != awsS3Endpoint) {
            S3Configuration s3Configuration = S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build();

            s3ClientBuilder.region(Region.of(awsS3Region));
            s3ClientBuilder.serviceConfiguration(s3Configuration);
            s3ClientBuilder.endpointOverride(URI.create(awsS3Endpoint));
        }

        return s3ClientBuilder.build();
        */
    }
}
