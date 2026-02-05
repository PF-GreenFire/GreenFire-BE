package sisosolsol.greenfire.common.config;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.upload")
public class UploadAllowConfig {
    private String directory;
    private String allowedExtensions; // "jpg,jpeg,png
    private String allowedMimeTypes;  // "image/jpeg,image/png

    public String getDirectory() { return directory; }
    public void setDirectory(String directory) { this.directory = directory; }

    public Set<String> getAllowedExtensionSet() {
        return Arrays.stream(allowedExtensions.split(","))
            .map(String::trim)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }

    public Set<String> getAllowedMimeTypeSet() {
        return Arrays.stream(allowedMimeTypes.split(","))
            .map(String::trim)
            .collect(Collectors.toSet());
    }

    public void setAllowedExtensions(String allowedExtensions) { this.allowedExtensions = allowedExtensions; }
    public void setAllowedMimeTypes(String allowedMimeTypes) { this.allowedMimeTypes = allowedMimeTypes; }
}
