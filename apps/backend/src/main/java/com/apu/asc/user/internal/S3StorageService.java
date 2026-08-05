package com.apu.asc.user.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
class S3StorageService {

  @Value("${s3.endpoint:http://localhost:9000}")
  private String s3Endpoint;

  @Value("${s3.region:us-east-1}")
  private String region;

  @Value("${s3.access-key:minioadmin}")
  private String accessKey;

  @Value("${s3.secret-key:minioadminpassword}")
  private String secretKey;

  @Value("${s3.bucket:apu-asc-assets}")
  private String bucket;

  private S3Client s3Client;

  @PostConstruct
  public void init() {
    try {
      this.s3Client =
          S3Client.builder()
              .endpointOverride(URI.create(s3Endpoint))
              .region(Region.of(region))
              .credentialsProvider(
                  StaticCredentialsProvider.create(
                      AwsBasicCredentials.create(accessKey, secretKey)))
              // Required for Cloudflare R2 and remains compatible with MinIO.
              .serviceConfiguration(
                  S3Configuration.builder()
                      .pathStyleAccessEnabled(true)
                      .chunkedEncodingEnabled(false)
                      .build())
              .build();
      ensureBucketExists();
    } catch (Exception e) {
      log.warn("S3 storage initialization warning: {}", e.getMessage());
    }
  }

  private void ensureBucketExists() {
    try {
      s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
    } catch (NoSuchBucketException e) {
      log.info("Bucket {} does not exist. Creating now...", bucket);
      s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
    } catch (Exception e) {
      log.warn("Could not check/create S3 bucket: {}", e.getMessage());
    }
  }

  public String uploadAvatar(MultipartFile file, String userId) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be empty");
    }
    String originalFilename = file.getOriginalFilename();
    String ext = "png";
    if (originalFilename != null && originalFilename.contains(".")) {
      ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
    String filename =
        "avatar-" + userId + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
    String key = "avatars/" + filename;

    try {
      PutObjectRequest putReq =
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType(file.getContentType() != null ? file.getContentType() : "image/png")
              .build();

      s3Client.putObject(
          putReq, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
      log.info("Successfully uploaded avatar for user {} to key {}", userId, key);
      return "/api/v1/users/avatar/file/" + filename;
    } catch (Exception e) {
      log.error("Failed to upload avatar to S3: {}", e.getMessage(), e);
      throw new RuntimeException("Avatar upload failed: " + e.getMessage(), e);
    }
  }

  public InputStream getAvatarFile(String filename) {
    String key = "avatars/" + filename;
    return s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
  }
}
