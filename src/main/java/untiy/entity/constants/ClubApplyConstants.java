package untiy.entity.constants;

public final class ClubApplyConstants {

    public static final int APPLY_TYPE_CREATE = 1;
    public static final int APPLY_TYPE_DISSOLVE = 2;

    /** 1:待学院审批 2:学院已通过 3:已通过 4:已驳回 5:已撤回 */
    public static final int STATUS_PENDING_COLLEGE = 1;
    public static final int STATUS_COLLEGE_APPROVED = 2;
    public static final int STATUS_APPROVED = 3;
    public static final int STATUS_REJECTED = 4;
    public static final int STATUS_WITHDRAWN = 5;

    /** 合议 1:合议中 2:已通过 3:已驳回 */
    public static final int COUNCIL_IN_PROGRESS = 1;
    public static final int COUNCIL_APPROVED = 2;
    public static final int COUNCIL_REJECTED = 3;

    public static final int CLUB_STATUS_DISSOLVED = 0;
    public static final int CLUB_STATUS_NORMAL = 1;

    public static final int SCOPE_TYPE_COLLEGE = 1;
    public static final int SCOPE_TYPE_CLUB = 2;
    public static final int SCOPE_TYPE_DEPARTMENT = 3;

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_CLUB_PRESIDENT = "CLUB_PRESIDENT";
    public static final String ROLE_CLUB_MINISTER = "CLUB_MINISTER";

    private ClubApplyConstants() {
    }
}
