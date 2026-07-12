package untiy.entity.constants;

public final class ActivitySignConstants {

    /** 签到配置：1定位 2扫码 3两者 */
    public static final int MODE_LOCATION = 1;
    public static final int MODE_QR = 2;
    public static final int MODE_BOTH = 3;

    /** 签到记录类型：1定位 2扫码 3补签 4手动 */
    public static final int TYPE_LOCATION = 1;
    public static final int TYPE_QR = 2;
    public static final int TYPE_MAKEUP = 3;
    public static final int TYPE_MANUAL = 4;

    /** sign_status */
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_LATE = 2;
    public static final int STATUS_EARLY_LEAVE = 3;

    /** 补签原因：1活动方 2个人 3特殊 */
    public static final int MAKEUP_REASON_EVENT = 1;
    public static final int MAKEUP_REASON_PERSONAL = 2;
    public static final int MAKEUP_REASON_SPECIAL = 3;

    /** 补签申请状态 */
    public static final int MAKEUP_PENDING = 1;
    public static final int MAKEUP_APPROVED = 2;
    public static final int MAKEUP_REJECTED = 3;

    public static final int DEFAULT_RADIUS_METERS = 100;

    private ActivitySignConstants() {
    }
}
