package sisosolsol.greenfire.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sisosolsol.greenfire.common.config.UploadAllowConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 이미지 저장 키(users.profile_key, users.cover_image_key) → 실제 파일 경로.
 *
 * <p>사용자 이미지는 저장 경로가 두 갈래다.
 * <ul>
 *   <li>회원가입 시: FileUploadUtil → {@code file.upload.local.base-path} 아래 상대 경로</li>
 *   <li>프로필·커버 수정 시: FileStorage → {@code app.upload.directory} 아래
 *       {@code users/{id}/profile.jpg}, {@code users/{id}/cover.jpg}</li>
 * </ul>
 * 두 루트를 순서대로 찾아 실제로 존재하는 파일을 돌려준다. 키가 루트 밖을 가리키면(../ 등) 무시한다.
 */
@Component
public class UserImageResolver {

    private final List<Path> roots;

    public UserImageResolver(UploadAllowConfig uploadAllowConfig,
                                @Value("${file.upload.local.base-path}") String uploadBasePath) {
        this(List.of(Paths.get(uploadAllowConfig.getDirectory()), Paths.get(uploadBasePath)));
    }

    UserImageResolver(List<Path> roots) {
        this.roots = roots.stream().map(root -> root.toAbsolutePath().normalize()).toList();
    }

    public Optional<Path> resolve(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return Optional.empty();
        for (Path root : roots) {
            Path candidate = root.resolve(storageKey).normalize();
            if (candidate.startsWith(root) && Files.isRegularFile(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
