package dk.gatchaz.server.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * S3 클라이언트 설정.
 * 자격 증명(access key 등)은 코드/설정 파일에 직접 넣지 않고, AWS SDK 기본 자격 증명 체인
 * (환경 변수 AWS_ACCESS_KEY_ID/AWS_SECRET_ACCESS_KEY, ~/.aws/credentials, IAM 역할 등)을 사용한다.
 * 버킷 이름과 리전만 app.s3.* 설정으로 관리한다.
 */
@Configuration
public class S3Config {

    @Value("${app.s3.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
