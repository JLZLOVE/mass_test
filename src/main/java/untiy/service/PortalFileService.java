package untiy.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 门户图像上传：通知封面、活动封面、社团 Logo。
 */
public interface PortalFileService {

    /** 上传通知封面，返回 HTTP 可访问的相对路径 */
    String uploadNoticeCover(MultipartFile file, String noticeNo);

    /** 上传活动封面，返回 HTTP 可访问的相对路径 */
    String uploadActivityCover(MultipartFile file, String activityNo);

    /** 上传社团 Logo，返回 HTTP 可访问的相对路径 */
    String uploadClubLogo(MultipartFile file, Long clubId);
}
