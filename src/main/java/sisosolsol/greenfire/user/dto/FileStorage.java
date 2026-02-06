package sisosolsol.greenfire.user.dto;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import sisosolsol.greenfire.common.config.UploadAllowConfig;

@Component
@RequiredArgsConstructor
public class FileStorage {

    private final UploadAllowConfig config;

    /**
     * 파일을 저장하고 storageKey를 반환
     *
     * @param directory 저장 하위 디렉토리 (예: "profiles", "posts", "reviews")
     * @param filename  저장할 파일명
     * @param file      업로드된 파일
     * @return storageKey (상대 경로)
     */
    public String save(String storageKey, MultipartFile file) {
        Path savePath = Paths.get(config.getDirectory()).resolve(storageKey);
        Path saveDirectory = savePath.getParent();

        try (InputStream is = file.getInputStream()) {
            Files.createDirectories(saveDirectory);
            Files.copy(is, savePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save file: " + savePath, e);
        }

        return storageKey;
    }

    /**
     * storageKey에 해당하는 파일을 삭제
     *
     * @param storageKey 저장 시 반환된 키
     */
    public void delete(String storageKey) {
        Path filePath = Paths.get(config.getDirectory()).resolve(storageKey);

        try {
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + filePath, e);
        }
    }
}
