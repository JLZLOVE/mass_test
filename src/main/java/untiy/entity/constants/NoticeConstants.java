package untiy.entity.constants;

public final class NoticeConstants {

    /** 0草稿 1已发布 2已撤回 */
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_WITHDRAWN = 2;

    /** 1全体学生 2全体老师 3指定角色 4指定社团 5指定人员 */
    public static final int RECEIVER_ALL_STUDENTS = 1;
    public static final int RECEIVER_ALL_TEACHERS = 2;
    public static final int RECEIVER_ROLES = 3;
    public static final int RECEIVER_CLUBS = 4;
    public static final int RECEIVER_USERS = 5;

    /** 重要程度 1高 2中 3低 */
    public static final int IMPORTANCE_HIGH = 1;
    public static final int IMPORTANCE_MEDIUM = 2;
    public static final int IMPORTANCE_LOW = 3;

    /** 紧急程度 1紧急 2不紧急 */
    public static final int URGENCY_URGENT = 1;
    public static final int URGENCY_NORMAL = 2;

    public static final int SOURCE_MANUAL = 0;
    public static final int SOURCE_ACTIVITY_CANCEL = 1;

    public static final int TEMPLATE_STATUS_ACTIVE = 1;
    public static final int TEMPLATE_STATUS_DISABLED = 0;

    public static final String PUBLISHER_ADMIN = "ADMIN";
    public static final String PUBLISHER_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String PUBLISHER_ADVISOR = "ADVISOR";
    public static final String PUBLISHER_PRESIDENT = "PRESIDENT";
    public static final String PUBLISHER_MINISTER = "MINISTER";

    private NoticeConstants() {
    }
}
