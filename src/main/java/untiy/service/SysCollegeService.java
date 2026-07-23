package untiy.service;

import untiy.entity.SysCollege;

import java.util.List;

/**
 * 学院服务。
 */
public interface SysCollegeService {

    /**
     * 按当前用户权限返回可见学院；支持名称关键词模糊搜索。
     */
    List<SysCollege> listForCurrentUser(String keyword);
}
