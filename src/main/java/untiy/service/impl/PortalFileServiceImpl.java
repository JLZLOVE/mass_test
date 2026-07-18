package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import untiy.entity.ActivityApply;
import untiy.entity.ClubCategory;
import untiy.entity.NoticeInfo;
import untiy.entity.SysClub;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.ActivityApplyMapper;
import untiy.mapper.NoticeInfoMapper;
import untiy.mapper.SysClubMapper;
import untiy.service.PortalFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class PortalFileServiceImpl implements PortalFileService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"));

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"));

    @Value("${portal.upload-dir:static/portal}")
    private String uploadDir;

    @Autowired
    private NoticeInfoMapper noticeInfoMapper;

    @Autowired
    private ActivityApplyMapper activityApplyMapper;

    @Autowired
    private SysClubMapper sysClubMapper;

    @Transactional
    @Override
    public String uploadNoticeCover(MultipartFile file, String noticeNo) {
        if (StringUtils.isBlank(noticeNo)) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "通知编号不能为空");
        }
        NoticeInfo notice = noticeInfoMapper.selectOne(new LambdaQueryWrapper<NoticeInfo>()
                .eq(NoticeInfo::getNoticeNo, noticeNo.trim()));
        if (notice == null) {
            throw new EIException(ErrorConfig.NOTICE_NOT_FOUND_CODE, ErrorConfig.NOTICE_NOT_FOUND_MSG);
        }
        String prefix = extractNoPrefix(noticeNo.trim());
        String relativePath = storeFile(file, "notice", prefix);
        String oldPath = notice.getCoverImage();
        notice.setCoverImage(relativePath);
        noticeInfoMapper.updateById(notice);
        deleteOldFileQuietly(oldPath);
        return relativePath;
    }

    @Transactional
    @Override
    public String uploadActivityCover(MultipartFile file, String activityNo) {
        if (StringUtils.isBlank(activityNo)) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "活动编号不能为空");
        }
        ActivityApply apply = activityApplyMapper.selectOne(new LambdaQueryWrapper<ActivityApply>()
                .eq(ActivityApply::getActivityNo, activityNo.trim()));
        if (apply == null) {
            throw new EIException(ErrorConfig.ACT_APPLY_NOT_FOUND_CODE, ErrorConfig.ACT_APPLY_NOT_FOUND_MSG);
        }
        String prefix = extractNoPrefix(activityNo.trim());
        String relativePath = storeFile(file, "activity", prefix);
        String oldPath = apply.getCoverImage();
        apply.setCoverImage(relativePath);
        activityApplyMapper.updateById(apply);
        deleteOldFileQuietly(oldPath);
        return relativePath;
    }

    @Transactional
    @Override
    public String uploadClubLogo(MultipartFile file, Long clubId) {
        if (clubId == null) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "社团ID不能为空");
        }
        SysClub club = sysClubMapper.selectById(clubId);
        if (club == null) {
            throw new EIException(ErrorConfig.CLUB_NOT_FOUND_CODE, ErrorConfig.CLUB_NOT_FOUND_MSG);
        }
        String prefix;
        try {
            prefix = ClubCategory.prefixOf(club.getCategory());
        } catch (IllegalArgumentException e) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "社团类别无效，无法解析存储前缀");
        }
        String relativePath = storeFile(file, "club", prefix);
        String oldPath = club.getLogo();
        club.setLogo(relativePath);
        sysClubMapper.updateById(club);
        deleteOldFileQuietly(oldPath);
        return relativePath;
    }

    private String storeFile(MultipartFile file, String type, String prefix) {
        validateFile(file);
        String ext = resolveExtension(file);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path dir = Paths.get(uploadDir, type, prefix);
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());
            return "/portal/" + type + "/" + prefix + "/" + fileName;
        } catch (IOException e) {
            log.error("门户图片保存失败 type={} prefix={}", type, prefix, e);
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "文件保存失败");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "文件不能为空");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "图片大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "仅支持 jpg/png/gif/webp 格式");
        }
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isBlank(ext) || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "仅支持 jpg/png/gif/webp 格式");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (ext == null) {
            ext = "";
        }
        ext = ext.toLowerCase();
        if ("jpeg".equals(ext)) {
            return "jpg";
        }
        return ext;
    }

    private String extractNoPrefix(String businessNo) {
        if (businessNo.length() < 2) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "业务编号格式无效");
        }
        return businessNo.substring(0, 2).toUpperCase();
    }

    private void deleteOldFileQuietly(String httpPath) {
        if (StringUtils.isBlank(httpPath) || !httpPath.startsWith("/portal/")) {
            return;
        }
        try {
            Path path = Paths.get(uploadDir).resolve(httpPath.substring("/portal/".length()));
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            log.warn("删除旧门户图片失败 path={}", httpPath, e);
        }
    }
}
