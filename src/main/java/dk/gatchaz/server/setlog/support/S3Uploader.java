package dk.gatchaz.server.setlog.support;

import dk.gatchaz.server.common.exception.CommonException;
import dk.gatchaz.server.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 셋로그 영상 파일을 S3 에 업로드한다.
 * 허용 확장자는 mp4/mov 로 제한한다. (필요 시 조정 가능)
 * 파일 크기 제한은 spring.servlet.multipart.max-file-size 설정으로 프레임워크 단에서 걸린다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("mp4", "mov");

    private final S3Client s3Client;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.region}")
    private String region;

    /**
     * 영상 파일을 S3 에 업로드하고, 접근 가능한 URL 을 반환한다.
     * key 는 setlog/{tripId}/{tripMissionId}/{uuid}.{ext} 형식으로 생성한다.
     * 허용되지 않은 확장자면 CommonException(UNSUPPORTED_MEDIA_TYPE) 을 던진다.
     */
    public String upload(final MultipartFile file, final Long tripId, final Long tripMissionId) {
        final String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new CommonException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        final String key = "setlog/%d/%d/%s.%s".formatted(tripId, tripMissionId, UUID.randomUUID(), extension);

        try {
            final PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (final IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);
    }

    private String extractExtension(final String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
    }
}
