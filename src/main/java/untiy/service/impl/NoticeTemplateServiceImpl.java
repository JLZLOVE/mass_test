package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.NoticeInfo;
import untiy.entity.NoticeTemplate;
import untiy.entity.dto.NoticeTemplateDTO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.NoticeInfoMapper;
import untiy.mapper.NoticeTemplateMapper;
import untiy.service.NoticeTemplateService;
import untiy.utils.MPUtil;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class NoticeTemplateServiceImpl extends ServiceImpl<NoticeTemplateMapper, NoticeTemplate>
        implements NoticeTemplateService {

    @Autowired
    private NoticeInfoMapper noticeInfoMapper;

    @Transactional
    @Override
    public Long saveTemplate(NoticeTemplateDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        NoticeTemplate template = new NoticeTemplate();
        template.setTemplateName(dto.getTemplateName());
        template.setTitle(dto.getTitle());
        template.setContent(dto.getContent());
        template.setCategoryId(dto.getCategoryId());
        template.setStatus(1);
        template.setCreateTime(now);
        template.setUpdateTime(now);
        save(template);
        return template.getId();
    }

    @Transactional
    @Override
    public void updateTemplate(NoticeTemplateDTO dto) {
        if (dto.getId() == null) {
            throw new EIException(ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_CODE, ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_MSG);
        }
        NoticeTemplate template = getById(dto.getId());
        if (template == null) {
            throw new EIException(ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_CODE, ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_MSG);
        }
        template.setTemplateName(dto.getTemplateName());
        template.setTitle(dto.getTitle());
        template.setContent(dto.getContent());
        template.setCategoryId(dto.getCategoryId());
        template.setUpdateTime(LocalDateTime.now());
        updateById(template);
    }

    @Transactional
    @Override
    public void deleteTemplate(Long id) {
        NoticeTemplate template = getById(id);
        if (template == null) {
            throw new EIException(ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_CODE, ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_MSG);
        }
        long refCount = noticeInfoMapper.selectCount(new LambdaQueryWrapper<NoticeInfo>()
                .eq(NoticeInfo::getTemplateId, id));
        if (refCount > 0) {
            template.setStatus(0);
            template.setUpdateTime(LocalDateTime.now());
            updateById(template);
            throw new EIException(ErrorConfig.NOTICE_TEMPLATE_IN_USE_CODE, ErrorConfig.NOTICE_TEMPLATE_IN_USE_MSG);
        }
        removeById(id);
    }

    @Override
    public NoticeTemplate getDetail(Long id) {
        NoticeTemplate template = getById(id);
        if (template == null) {
            throw new EIException(ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_CODE, ErrorConfig.NOTICE_TEMPLATE_NOT_FOUND_MSG);
        }
        return template;
    }

    @Override
    public IPage<NoticeTemplate> pageQuery(Map<String, Object> param) {
        Page<NoticeTemplate> page = MPUtil.getPage(param);
        return page(page, new LambdaQueryWrapper<NoticeTemplate>()
                .eq(NoticeTemplate::getStatus, 1)
                .orderByDesc(NoticeTemplate::getCreateTime));
    }
}
