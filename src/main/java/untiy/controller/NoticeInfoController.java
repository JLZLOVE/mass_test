package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import untiy.annotation.RequiresLevel;
import untiy.entity.NoticeInfo;
import untiy.entity.dto.NoticeSendDTO;
import untiy.exception.Level;
import untiy.service.NoticeInfoService;
import untiy.utils.NoticeFileStorageUtil;
import untiy.utils.R;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Tag(name = "通知", description = "通知发送、撤回、已读确认与收件箱")
@RequestMapping("/notice-info")
public class NoticeInfoController {

    @Autowired
    private NoticeInfoService noticeInfoService;

    @Autowired
    private NoticeFileStorageUtil noticeFileStorageUtil;

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "发送通知", description = "支持立即发送、定时发送或保存草稿")
    @PostMapping("/send")
    public R send(@Valid @RequestBody NoticeSendDTO dto) {
        Long id = noticeInfoService.send(dto);
        return R.ok("操作成功").put("data", id);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "保存草稿")
    @PostMapping("/draft")
    public R draft(@Valid @RequestBody NoticeSendDTO dto) {
        Long id = noticeInfoService.saveDraft(dto);
        return R.ok("草稿已保存").put("data", id);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "手动发布草稿")
    @PostMapping("/publish/{id}")
    public R publish(@PathVariable Long id) {
        noticeInfoService.publishNow(id);
        return R.ok("已发布");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "撤回通知")
    @PostMapping("/withdraw/{id}")
    public R withdraw(@PathVariable Long id) {
        noticeInfoService.withdraw(id);
        return R.ok("已撤回");
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "通知详情", description = "进入详情即记录已读")
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Long id) {
        return R.ok().put("data", noticeInfoService.getDetail(id));
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "确认阅读")
    @PostMapping("/confirm/{id}")
    public R confirm(@PathVariable Long id) {
        noticeInfoService.confirmRead(id);
        return R.ok("已确认");
    }

    @RequiresLevel(minLevel = Level.STUDENT)
    @Operation(summary = "我的收件箱")
    @GetMapping("/inbox")
    public R inbox(@RequestParam Map<String, Object> param) {
        IPage<NoticeInfo> page = noticeInfoService.myInbox(param);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "我发送的通知")
    @GetMapping("/sent")
    public R sent(@RequestParam Map<String, Object> param) {
        IPage<NoticeInfo> page = noticeInfoService.mySent(param);
        return R.ok().put("data", page);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "已读/确认统计", description = "仅发布人或有权限管理员可查看")
    @GetMapping("/stats/{id}")
    public R stats(@PathVariable Long id) {
        return R.ok().put("data", noticeInfoService.readStats(id));
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "上传通知附件")
    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file) {
        String path = noticeFileStorageUtil.store(file);
        return R.ok("上传成功").put("data", path);
    }
}
