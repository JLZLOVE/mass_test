package untiy.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import untiy.advice.GlobalExceptionHandler;
import untiy.entity.ActivityApply;
import untiy.entity.ActivityCategory;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.ActivityApplyMapper;
import untiy.mapper.ActivityCategoryMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
//单一职责工具类
@Component
public class ActivityCodeGeneratorUtil {
    @Autowired
    private ActivityCategoryMapper activityCategoryMapper;
    @Autowired
    private ActivityApplyMapper activityApplyMapper;
    // 默认前缀（当分类无映射时使用）
    private static final String DEFAULT_PREFIX = "ACT";

    //    自动生成编号


    public String generateCode(Long categoryId) {
        // 1. 获取分类对应的后缀
        String suffix = getCategorySuffix(categoryId);
        // 2. 日期部分
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 3. 4位随机数
        int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
        // 4. 组合
        String rawCode = DEFAULT_PREFIX + datePart + randomNum + suffix;
        // 5. 防重复（极小概率，加循环重试）
        return ensureUnique(rawCode);
    }

    private String getCategorySuffix(Long categoryId) {
//        判断后缀
        if (Objects.isNull(categoryId)) {
         throw new EIException(ErrorConfig.ACT_CONTENT_NULL_MSG);
        }
//        检查对应编号
        ActivityCategory activityCategory = activityCategoryMapper.selectById(categoryId);
//        检查是否为空
        if (Objects.isNull(activityCategory) || activityCategory.getCodeSuffix() == null) {
            throw new EIException(ErrorConfig.ACT_CONTENT_NULL_MSG);
        }
        if (activityCategory.getCodeSuffix() == null || activityCategory.getCodeSuffix().trim().isEmpty()) {
            throw new EIException(ErrorConfig.ACT_NO_NULL_MSG);
        }
        // 无映射时返回空，编号变为 ACT202603301234
        return activityCategory.getCodeSuffix();
    }

    private String ensureUnique(String code) {
        // 查询 activity_apply 表 activity_no 是否存在
        LambdaQueryWrapper<ActivityApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityApply::getActivityNo, code);
        if (activityApplyMapper.selectCount(wrapper) == 0) {
            return code;
        }
        // 重新生成随机数部分（简单重试）
        int newRandom = ThreadLocalRandom.current().
                nextInt(1000, 10000);
        return ensureUnique(
                code.substring
                        (0, code.length() - 4) + newRandom);
    }
}
