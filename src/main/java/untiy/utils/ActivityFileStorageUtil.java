package untiy.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class ActivityFileStorageUtil {

    @Value("${activity.upload-dir:static/activity}")
    private String uploadDir;

    public String store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new EIException(ErrorConfig.ACT_CONTENT_NULL_CODE, "上传文件不能为空");
        }
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(uploadDir, subDir, datePart);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());
            return "/" + uploadDir.replace("\\", "/") + "/" + subDir + "/" + datePart + "/" + fileName;
        } catch (IOException e) {
            throw new EIException(ErrorConfig.ACT_CONTENT_NULL_CODE, "文件保存失败");
        }
    }
}
