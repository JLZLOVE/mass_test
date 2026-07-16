package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.NoticeTemplate;
import untiy.entity.dto.NoticeTemplateDTO;

import java.util.Map;

public interface NoticeTemplateService extends com.baomidou.mybatisplus.extension.service.IService<NoticeTemplate> {

    String saveTemplate(NoticeTemplateDTO dto);

    void updateTemplate(NoticeTemplateDTO dto);

    void deleteTemplate(String templateName);

    NoticeTemplate getDetail(String templateName);

    IPage<NoticeTemplate> pageQuery(Map<String, Object> param);
}
