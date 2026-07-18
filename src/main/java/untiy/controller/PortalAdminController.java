package untiy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import untiy.annotation.RequiresLevel;
import untiy.exception.Level;
import untiy.service.PortalFileService;
import untiy.utils.R;

/**
 * 门户管理端：封面 / Logo 上传（需登录且等级 ≤ ADMIN）。
 * <p>
 * 与公开门户 {@code /portal} 职责分离；静态图访问路径为 {@code /portal/{type}/{prefix}/{file}}。
 */
@RestController
@Tag(name = "门户管理", description = "门户通知封面、活动封面、社团 Logo 上传")
@RequestMapping("/portal/admin")
@RequiresLevel(minLevel = Level.ADMIN)
public class PortalAdminController {

    @Autowired
    private PortalFileService portalFileService;

    @Operation(summary = "上传通知封面")
    @PostMapping("/upload/notice-cover")
    public R uploadNoticeCover(@RequestParam("file") MultipartFile file,
                               @RequestParam String noticeNo) {
        String path = portalFileService.uploadNoticeCover(file, noticeNo);
        return R.ok("success").put("data", path);
    }

    @Operation(summary = "上传活动封面")
    @PostMapping("/upload/activity-cover")
    public R uploadActivityCover(@RequestParam("file") MultipartFile file,
                                 @RequestParam String activityNo) {
        String path = portalFileService.uploadActivityCover(file, activityNo);
        return R.ok("success").put("data", path);
    }

    @Operation(summary = "上传社团 Logo")
    @PostMapping("/upload/club-logo")
    public R uploadClubLogo(@RequestParam("file") MultipartFile file,
                            @RequestParam Long clubId) {
        String path = portalFileService.uploadClubLogo(file, clubId);
        return R.ok("success").put("data", path);
    }
}
