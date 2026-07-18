package untiy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import untiy.annotation.IgnoreAuth;
import untiy.entity.vo.PortalActivityDetailVO;
import untiy.entity.vo.PortalActivityListVO;
import untiy.entity.vo.PortalClubVO;
import untiy.entity.vo.PortalNoticeDetailVO;
import untiy.entity.vo.PortalNoticeListVO;
import untiy.service.ActivityApplyService;
import untiy.service.NoticeInfoService;
import untiy.service.SysClubService;
import untiy.utils.R;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公开门户（免登录）。
 * <p>
 * 通知列表/详情、活动列表/详情、社团列表。
 * 管理端图片上传见 {@link PortalAdminController}。
 */
@Slf4j
@RestController
@Tag(name = "公开门户", description = "免登录门户：通知、活动、社团")
@RequestMapping("/portal")
@IgnoreAuth
public class PortalController {

    private final NoticeInfoService noticeInfoService;
    private final ActivityApplyService activityApplyService;
    private final SysClubService sysClubService;

    public PortalController(NoticeInfoService noticeInfoService,
                            ActivityApplyService activityApplyService,
                            SysClubService sysClubService) {
        this.noticeInfoService = noticeInfoService;
        this.activityApplyService = activityApplyService;
        this.sysClubService = sysClubService;
    }

    @Operation(summary = "通知列表")
    @GetMapping("/notices")
    public R listNotices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return R.ok().put("data", noticeInfoService.portalList(page, size));
    }

    @Operation(summary = "通知详情")
    @GetMapping("/notices/{noticeNo}")
    public R getNoticeDetail(
            @PathVariable String noticeNo) {
        return R.ok().put("data", noticeInfoService.portalDetail(noticeNo));
    }

    @Operation(summary = "活动列表")
    @GetMapping("/activities")
    public R listActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime freezeTime) {
        return R.ok().put("data", activityApplyService.portalList(page, size, freezeTime));
    }

    @Operation(summary = "活动详情")
    @GetMapping("/activities/{activityNo}")
    public R getActivityDetail(
            @PathVariable String activityNo) {
        return R.ok().put("data", activityApplyService.portalDetail(activityNo));
    }

    @Operation(summary = "社团列表")
    @GetMapping("/clubs")
    public R listClubs(
            @RequestParam(required = false) String category) {
        return R.ok().put("data", sysClubService.portalList(category));
    }
}