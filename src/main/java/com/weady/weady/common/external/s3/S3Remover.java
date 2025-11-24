package com.weady.weady.common.external.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.weady.weady.common.error.errorCode.S3ErrorCode;
import com.weady.weady.common.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Remover {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void remove(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.info("S3Remover: Skip delete - empty url.");
            return;
        }

        final String key;
        try {
            String rawPath = URI.create(fileUrl).getRawPath();
            if (rawPath == null || rawPath.length() <= 1) {
                log.warn("S3Remover: Invalid url path. url={}", fileUrl);
                return;
            }
            key = (rawPath.charAt(0) == '/') ? rawPath.substring(1) : rawPath;
        } catch (Exception e) {
            log.error("S3Remover: URL parse failed. url={}", fileUrl, e);
            throw new BusinessException(S3ErrorCode.S3_REMOVE_FAIL);
        }

        try {
            amazonS3.deleteObject(bucket, key);
            log.info("S3Remover: Delete successful. bucket={}, key={}", bucket, key);
        } catch (SdkClientException e) {
            log.error("S3Remover: Delete failed. bucket={}, key={}", bucket, key, e);
            throw new BusinessException(S3ErrorCode.S3_REMOVE_FAIL);
        }
    }
}
