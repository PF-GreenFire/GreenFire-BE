package sisosolsol.greenfire.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 파일 시스템만 쓰는 단위 테스트 (CI 패턴 *ServiceTest 에 포함되도록 이름을 맞춤) */
class UserImageResolverServiceTest {

    @TempDir
    Path storageRoot;   // app.upload.directory (프로필 수정 시 저장)

    @TempDir
    Path uploadRoot;    // file.upload.local.base-path (회원가입 시 저장)

    private UserImageResolver resolver() {
        return new UserImageResolver(List.of(storageRoot, uploadRoot));
    }

    private static Path touch(Path root, String key) throws IOException {
        Path file = root.resolve(key);
        Files.createDirectories(file.getParent());
        return Files.write(file, new byte[]{1});
    }

    @Test
    @DisplayName("프로필 수정으로 저장된 키(users/{id}/profile.jpg)를 찾는다")
    void resolvesStorageKey() throws IOException {
        Path file = touch(storageRoot, "users/abc/profile.jpg");

        assertThat(resolver().resolve("users/abc/profile.jpg")).contains(file.normalize());
    }

    @Test
    @DisplayName("회원가입으로 저장된 상대 경로는 업로드 루트에서 찾는다")
    void resolvesSignupUploadPath() throws IOException {
        Path file = touch(uploadRoot, "profile/2026/10/a.png");

        assertThat(resolver().resolve("profile/2026/10/a.png")).contains(file.normalize());
    }

    @Test
    @DisplayName("키가 비었거나 파일이 없으면 빈 결과")
    void emptyWhenMissing() {
        assertThat(resolver().resolve(null)).isEmpty();
        assertThat(resolver().resolve("")).isEmpty();
        assertThat(resolver().resolve("users/none/profile.jpg")).isEmpty();
    }

    @Test
    @DisplayName("루트 밖을 가리키는 키(../)는 파일이 있어도 거부한다")
    void rejectsPathTraversal(@TempDir Path outside) throws IOException {
        touch(outside, "secret.jpg");
        String escape = storageRoot.relativize(outside.resolve("secret.jpg")).toString();

        assertThat(escape).startsWith("..");
        assertThat(resolver().resolve(escape)).isEmpty();
    }
}
