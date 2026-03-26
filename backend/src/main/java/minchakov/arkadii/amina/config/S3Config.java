package minchakov.arkadii.amina.config;

import io.awspring.cloud.s3.S3ObjectConverter;
import io.awspring.cloud.s3.S3OutputStreamProvider;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${s3-bucket}")
    private String bucket;

    @Value("${s3-internal-url}")
    private String internalUrl;

    @Value("${s3-external-url}")
    private String externalUrl;

    @Value("${s3-access-key}")
    private String accessKey;

    @Value("${s3-secret-key}")
    private String secretKey;

    @Bean
    @Primary
    public S3Client internalS3Client() {
        return S3Client
            .builder()
            .endpointOverride(URI.create(internalUrl))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.EU_NORTH_1)
            .forcePathStyle(true)
            .build();
    }

    @Bean
    public S3Client externalS3Client() {
        return S3Client
            .builder()
            .endpointOverride(URI.create(externalUrl))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.EU_NORTH_1)
            .forcePathStyle(true)
            .build();
    }

    @Bean
    @Primary
    public S3Presigner internalS3Presigner() {
        return S3Presigner
            .builder()
            .endpointOverride(URI.create(internalUrl))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.EU_NORTH_1)
            .build();
    }

    @Bean
    public S3Presigner externalS3Presigner() {
        return S3Presigner
            .builder()
            .endpointOverride(URI.create(externalUrl))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.EU_NORTH_1)
            .build();
    }

    @Bean
    @Primary
    public S3Template internalS3Template(
        S3OutputStreamProvider s3OutputStreamProvider,
        S3ObjectConverter s3ObjectConverter
    ) {
        return new S3Template(internalS3Client(), s3OutputStreamProvider, s3ObjectConverter, internalS3Presigner());
    }

    @Bean
    public S3Template externalS3Template(
        S3OutputStreamProvider s3OutputStreamProvider,
        S3ObjectConverter s3ObjectConverter
    ) {
        return new S3Template(externalS3Client(), s3OutputStreamProvider, s3ObjectConverter, externalS3Presigner());
    }
}
