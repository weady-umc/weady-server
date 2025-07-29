package com.weady.weady.common.external.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.weady.weady.common.error.errorCode.S3ErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 이미지 파일, 디렉토리 명을 받아서 S3에 업로드하고, 업로드된 파일의 URL을 반환합니다.
     * 디렉토리명은 board라면 board가 들어오면됩니다.
     * curation이라면 curation이 들어오면 됩니다.
     * @param file 업로드할 파일
     * @param dirName 업로드할 디렉토리 이름 (예: "board", "curation" 등)
     *
     * @return 업로드된 파일의 URL
     * */
    public String upload(MultipartFile file, String dirName) {
        String originalFileName = file.getOriginalFilename();

        //  공백, 한글 인코딩 처리
        String safeFileName = java.net.URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        String fileName = dirName + "/" + UUID.randomUUID() + "_" + safeFileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata));
            log.info("S3Uploader: Upload successful to fileName={}, bucket={}", fileName, bucket);
        } catch (IOException e) {
            log.error("S3Uploader: Upload failed due to IOException", e);
            throw new BusinessException(S3ErrorCode.S3_UPLOAD_FAIL);
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }
}
