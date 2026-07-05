package untiy.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import untiy.entity.NoticeTemplate;
import untiy.entity.dto.NoticeTemplateDTO;

import java.util.Map;

public interface NoticeTemplateService extends com.baomidou.mybatisplus.extension.service.IService<NoticeTemplate> {

    Long saveTemplate(NoticeTemplateDTO dto);

    void updateTemplate(NoticeTemplateDTO dto);

    void deleteTemplate(Long id);

    NoticeTemplate getDetail(Long id);

    IPage<NoticeTemplate> pageQuery(Map<String, Object> param);
}
