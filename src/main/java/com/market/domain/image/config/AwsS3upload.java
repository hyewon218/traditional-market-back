package com.market.domain.image.config;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3upload {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.region.static}")
    private String region;

    public String upload(MultipartFile file, String name) throws IOException {
        File uploadFile = convert(file).orElseThrow(() ->
                new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return upload(uploadFile, name);
    }

    private String upload(File uploadFile, String name) {
        String fileName = name + "/" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);

        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    private void removeNewFile(File targetFile) {
        if(targetFile.delete()){
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private String putS3(File uploadFile, String name) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, name, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, name).toString();
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    public void delete(String fileUrl) {
        try {
            String fileKey = extractFileKeyFromUrl(fileUrl);
            amazonS3Client.deleteObject(bucket, fileKey);
            log.info("File deleted from S3. URL: {}", fileUrl);
        } catch (Exception e) {
            log.error("Failed to delete file from S3. URL: {}", fileUrl, e);
        }
    }

    private String extractFileKeyFromUrl(String fileUrl) {
        //log.info("fileUrl : {} ", fileUrl);
        String bucketUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/";

        if (!fileUrl.startsWith(bucketUrl)) {
            throw new IllegalArgumentException("File URL does not start with the bucket URL");
        }
        // URL 에서 인코딩된 파일 키 부분을 추출
        String encodedFileKey = fileUrl.substring(bucketUrl.length());
        // 한글 파일키 디코딩
        String fileKey = URLDecoder.decode(encodedFileKey, StandardCharsets.UTF_8);
        //log.info("Extracted file key: {}", fileKey);
        return fileKey;
    }
}
