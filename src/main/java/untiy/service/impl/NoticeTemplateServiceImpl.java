package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.NoticeInfo;
import untiy.entity.NoticeTemplate;
import untiy.entity.constants.TemplateCodePrefix;
import untiy.entity.dto.NoticeTemplateDTO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.NoticeInfoMapper;
import untiy.mapper.NoticeTemplateMapper;
import untiy.service.NoticeTemplateService;
import untiy.utils.MPUtil;
import untiy.utils.TemplateCodeUtil;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NoticeTemplateServiceImpl extends ServiceImpl<NoticeTemplateMapper, NoticeTemplate>
        implements NoticeTemplateService {

    @Autowired
    private NoticeInfoMapper noticeInfoMapper;

    @Transactional
    @Override
    public String saveTemplate(NoticeTemplateDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        NoticeTemplate template = new NoticeTemplate();
        template.setTemplateName(generateUniqueName(now));
        template.setTitle(dto.getTitle());
        template.setContent(dto.getContent());
        template.setCategoryId(dto.getCategoryId());
        template.setStatus(1);
        template.setCreateTime(now);
        template.setUpdateTime(now);
        save(template);
        return template.getTemplateName();
    }

    @Transactional
    @Override
    public void updateTemplate(NoticeTemplateDTO dto) {
        if (StringUtils.isBlank(dto.getTemplateName())) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "更新时 templateName 不能为空");
        }
        NoticeTemplate template = requireByTemplateName(dto.getTemplateName());
        validateTemplateName(template);
        template.setTitle(dto.getTitle());
        template.setContent(dto.getContent());
        template.setCategoryId(dto.getCategoryId());
        template.setUpdateTime(LocalDateTime.now());
        updateById(template);
    }

    @Transactional
    @Override
    public void deleteTemplate(String templateName) {
        NoticeTemplate template = requireByTemplateName(templateName);
        validateTemplateName(template);
        long refCount = noticeInfoMapper.selectCount(new LambdaQueryWrapper<NoticeInfo>()
                .eq(NoticeInfo::getTemplateId, template.getId()));
        if (refCount > 0) {
            template.setStatus(0);
            template.setUpdateTime(LocalDateTime.now());
            updateById(template);
            throw new EIException(ErrorConfig.NOTICE_TEMPLATE_IN_USE_CODE, ErrorConfig.NOTICE_TEMPLATE_IN_USE_MSG);
        }
        removeById(template.getId());
    }

    @Override
    public NoticeTemplate getDetail(String templateName) {
        NoticeTemplate template = requireByTemplateName(templateName);
        validateTemplateName(template);
        return template;
    }

    @Override
    public IPage<NoticeTemplate> pageQuery(Map<String, Object> param) {
        Page<NoticeTemplate> page = MPUtil.getPage(param);
        IPage<NoticeTemplate> result = page(page, new LambdaQueryWrapper<NoticeTemplate>()
                .eq(NoticeTemplate::getStatus, 1)
                .orderByDesc(NoticeTemplate::getCreateTime));
        result.getRecords().forEach(this::validateTemplateName);
        return result;
    }

    private NoticeTemplate requireByTemplateName(String templateName) {
        if (StringUtils.isBlank(templateName)) {
            throw new EIException(ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_CODE, ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_MSG);
        }
        NoticeTemplate template = getOne(new LambdaQueryWrapper<NoticeTemplate>()
                .eq(NoticeTemplate::getTemplateName, templateName.trim()));
        if (template == null) {
            throw new EIException(ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_CODE, ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_MSG);
        }
        return template;
    }

    private void validateTemplateName(NoticeTemplate template) {
        if (template == null || StringUtils.isBlank(template.getTemplateName())) {
            return;
        }
        TemplateCodeUtil.assertMatchesCreateTime(template.getTemplateName(), template.getCreateTime());
    }

    private String generateUniqueName(LocalDateTime createTime) {
        String name;
        do {
            name = TemplateCodeUtil.generate(TemplateCodePrefix.NOTICE, createTime);
        } while (count(new LambdaQueryWrapper<NoticeTemplate>().eq(NoticeTemplate::getTemplateName, name)) > 0);
        return name;
    }
}
