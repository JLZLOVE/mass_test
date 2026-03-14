package untiy.utils;

import java.time.Year;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudentIdGeneratorUtil {
    private static int getCurrentYear() {
        return Year.now().getValue();

    }

    // 学院代码映射（可根据需要改为配置文件或数据库配置）
    private static String getCollegeCode(int year) {
        int lastTwo = year % 100;
        // 示例：23、24届用20，25届及以后用32
        if (lastTwo <= 24) {
            return "20";
        } else {
            return "32";
        }
    }

    // 专业代码映射
    private static String getMajorCode(String className) {
        if (className.contains("智")) {
            return "14";
        } else if (className.contains("数")) {
            return "24";
        } else if (className.contains("融")) {
            return "04";
        } else if (className.contains("软工")) {
            return "16";
        } else {
            throw new IllegalArgumentException("无法识别的专业类型：" + className);
        }
    }

    // 从班级名称中提取三位班级数字（如 "RB软工智254" → 254）
    private static int extractClassNumber(String className) {
        Pattern pattern = Pattern.compile("(\\d{3})");
        Matcher matcher = pattern.matcher(className);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new IllegalArgumentException("班级名称中未找到三位数字：" + className);
        }
    }

    // 计算班级代码：班级数字 - 基础值
    private static String calcClassCode(int classNumber, int year) {
        int lastTwo = year % 100;
        int base = lastTwo * 10 - 40;          // 基础值公式
        int code = classNumber - base;
        if (code < 0 || code > 99) {
            throw new IllegalArgumentException("班级代码超出范围（0-99）：" + code);
        }
        return String.format("%02d", code);
    }

    /**
     * 生成学号（指定年份，便于测试或处理历史数据）
     */
    public static String generateStudentId(int year, String className, int rank) {
        if (rank < 1 || rank > 99) {
            throw new IllegalArgumentException("排名必须在1~99之间");
        }
        String collegeCode = getCollegeCode(year);
        String majorCode = getMajorCode(className);
        int classNumber = extractClassNumber(className);
        String classCode = calcClassCode(classNumber, year);
        String rankStr = String.format("%02d", rank);
        return year + collegeCode + majorCode + classCode + rankStr;
    }

    /**
     * 生成学号（自动使用当前年份）
     * @param className 班级全称（如 "RB软工智254"）
     * @param rank      班级内排名（1~99）
     * @return 12位学号字符串
     */
    public static String generateStudentId(String className, int rank) {
        int year = getCurrentYear();
        return generateStudentId(year, className, rank);
    }


}
