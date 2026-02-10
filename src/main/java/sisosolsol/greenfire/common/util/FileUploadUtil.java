package sisosolsol.greenfire.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.common.enums.image.ImageType;
import sisosolsol.greenfire.common.exception.CustomException;
import sisosolsol.greenfire.common.exception.type.ExceptionCode;
import sisosolsol.greenfire.image.model.dto.ImageUploadDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${file.upload.local.base-path}")
    private String basePath;

    @Value("${file.upload.local.post-path}")
    private String postPath;

    @Value("${file.upload.local.store-path}")
    private String storePath;

    @Value("${file.upload.local.notice-path}")
    private String noticePath;

    @Value("${file.upload.local.challenge-path}")
    private String challengePath;

    @Value("${file.upload.max-size}")
    private long maxSize;

    @Value("${file.upload.allowed-types}")
    private String[] allowedTypes;

    /**
     * MultipartFile을 ImageUploadDTO로 변환하고 파일 저장
     *
     * @param file 업로드된 파일
     * @param imageType 이미지 타입 (POST, STORE, NOTICE, CHALLENGE)
     * @return ImageUploadDTO
     */
    public ImageUploadDTO uploadFile(MultipartFile file, ImageType imageType) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ExceptionCode.FILE_UPLOAD_ERROR);
        }

        // 파일 검증
        validateFile(file);

        try {
            // 1. 타입별 경로 결정
            String typePath = getTypePathByImageType(imageType);

            // 2. 파일명 생성: yyyyMMdd/UUID_원본파일명
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;

            // 3. 상대 경로: notice/20240108/uuid_filename.jpg
            String relativePath = typePath + "/" + dateFolder + "/" + fileName;

            // 4. 전체 경로: ./uploads/notice/20240108/uuid_filename.jpg
            String fullPath = basePath + "/" + relativePath;

            // 5. 디렉토리 생성
            Path directory = Paths.get(basePath + "/" + typePath + "/" + dateFolder);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 6. 파일 저장
            Path filePath = Paths.get(fullPath);
            Files.write(filePath, file.getBytes());

            // 7. ImageUploadDTO 생성
            ImageUploadDTO dto = new ImageUploadDTO();
            dto.setPath(relativePath);  // 상대 경로 저장
            dto.setOriginName(originalFilename);
            dto.setFileName(fileName);

            return dto;

        } catch (IOException e) {
            throw new CustomException(ExceptionCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * 파일 삭제
     *
     * @param relativePath 상대 경로
     */
    public void deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(basePath + "/" + relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 파일 삭제 실패는 로그만 남기고 예외는 던지지 않음
            System.err.println("파일 삭제 실패: " + relativePath);
        }
    }

    /**
     * 파일 확장자 추출
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    /**
     * 파일 검증 (크기, 확장자)
     */
    private void validateFile(MultipartFile file) {
        // 파일 크기 검증
        if (file.getSize() > maxSize) {
            throw new CustomException(ExceptionCode.FILE_SIZE_EXCEEDED);
        }

        // 파일 확장자 검증
        String extension = getFileExtension(file.getOriginalFilename());
        boolean isAllowed = Arrays.stream(allowedTypes)
                .anyMatch(allowed -> allowed.equalsIgnoreCase(extension));

        if (!isAllowed) {
            throw new CustomException(ExceptionCode.FILE_TYPE_NOT_ALLOWED);
        }
    }

    /**
     * ImageType에 따른 경로 반환
     */
    private String getTypePathByImageType(ImageType imageType) {
        switch (imageType) {
            case POST:
                return postPath;
            case STORE:
                return storePath;
            case NOTICE:
                return noticePath;
            case CHALLENGE:
                return challengePath;
            default:
                throw new CustomException(ExceptionCode.FILE_UPLOAD_ERROR);
        }
    }
}