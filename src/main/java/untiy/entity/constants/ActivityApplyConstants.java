package untiy.entity.constants;

public final class ActivityApplyConstants {

    /** 1草稿 2待审批 3审批中 4已通过 5已驳回 6已取消 7变更审批中 8已封锁（防篡改触发） */
    public static final int STATUS_DRAFT = 1;
    public static final int STATUS_PENDING = 2;
    public static final int STATUS_IN_PROGRESS = 3;
    public static final int STATUS_APPROVED = 4;
    public static final int STATUS_REJECTED = 5;
    public static final int STATUS_CANCELLED = 6;
    public static final int STATUS_CHANGE_PENDING = 7;
    public static final int STATUS_BLOCKED = 8;

    /** 1院级 2校级 */
    public static final int LEVEL_COLLEGE = 1;
    public static final int LEVEL_SCHOOL = 2;

    public static final int FLOW_RESULT_PASS = 1;
    public static final int FLOW_RESULT_REJECT = 2;

    public static final int FLOW_TYPE_NORMAL = 1;
    public static final int FLOW_TYPE_CHANGE = 2;

    public static final int HISTORY_PENDING = 1;
    public static final int HISTORY_APPLIED = 2;
    public static final int HISTORY_REJECTED = 3;

    public static final String INITIATOR_MINISTER = "MINISTER";
    public static final String INITIATOR_PRESIDENT = "PRESIDENT";
    public static final String INITIATOR_ADVISOR = "ADVISOR";
    public static final String INITIATOR_DEAN_OR_ADMIN = "DEAN_OR_ADMIN";

    public static final String APPROVER_PRESIDENT = "PRESIDENT";
    public static final String APPROVER_ADVISOR = "ADVISOR";
    public static final String APPROVER_COLLEGE_DEAN = "COLLEGE_DEAN";
    public static final String APPROVER_SUPER_ADMIN = "SUPER_ADMIN";

    private ActivityApplyConstants() {
    }
}
