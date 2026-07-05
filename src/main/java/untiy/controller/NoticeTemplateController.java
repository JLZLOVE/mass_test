package untiy.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import untiy.annotation.RequiresLevel;
import untiy.entity.NoticeTemplate;
import untiy.entity.dto.NoticeTemplateDTO;
import untiy.exception.Level;
import untiy.service.NoticeTemplateService;
import untiy.utils.R;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Tag(name = "通知模板", description = "通知模板增删改查")
@RequestMapping("/notice-template")
public class NoticeTemplateController {

    @Autowired
    private NoticeTemplateService noticeTemplateService;

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "新增模板")
    @PostMapping("/save")
    public R save(@Valid @RequestBody NoticeTemplateDTO dto) {
        Long id = noticeTemplateService.saveTemplate(dto);
        return R.ok("保存成功").put("data", id);
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "更新模板")
    @PutMapping("/update")
    public R update(@Valid @RequestBody NoticeTemplateDTO dto) {
        noticeTemplateService.updateTemplate(dto);
        return R.ok("更新成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "删除模板", description = "被引用时改为停用")
    @DeleteMapping("/delete/{id}")
    public R delete(@PathVariable Long id) {
        noticeTemplateService.deleteTemplate(id);
        return R.ok("删除成功");
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "模板详情")
    @GetMapping("/detail/{id}")
    public R detail(@PathVariable Long id) {
        return R.ok().put("data", noticeTemplateService.getDetail(id));
    }

    @RequiresLevel(minLevel = Level.ADMIN)
    @Operation(summary = "模板列表")
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> param) {
        IPage<NoticeTemplate> page = noticeTemplateService.pageQuery(param);
        return R.ok().put("data", page);
    }
}
