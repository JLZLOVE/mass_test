package untiy.security;

import untiy.entity.constants.ActivityApplyConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 根据发起人角色与活动级别生成审批链步骤（审批人类型序列）。
 */
public final class ActivityApprovalChainHelper {

    private ActivityApprovalChainHelper() {
    }

    public static List<String> buildNormalChain(String initiatorType, int activityLevel) {
        List<String> chain = new ArrayList<>();
        switch (initiatorType) {
            case ActivityApplyConstants.INITIATOR_MINISTER:
                chain.addAll(Arrays.asList(
                        ActivityApplyConstants.APPROVER_PRESIDENT,
                        ActivityApplyConstants.APPROVER_ADVISOR,
                        ActivityApplyConstants.APPROVER_COLLEGE_DEAN));
                break;
            case ActivityApplyConstants.INITIATOR_PRESIDENT:
                chain.addAll(Arrays.asList(
                        ActivityApplyConstants.APPROVER_ADVISOR,
                        ActivityApplyConstants.APPROVER_COLLEGE_DEAN));
                break;
            case ActivityApplyConstants.INITIATOR_ADVISOR:
                chain.add(ActivityApplyConstants.APPROVER_COLLEGE_DEAN);
                break;
            case ActivityApplyConstants.INITIATOR_DEAN_OR_ADMIN:
                chain.addAll(Arrays.asList(
                        ActivityApplyConstants.APPROVER_ADVISOR,
                        ActivityApplyConstants.APPROVER_COLLEGE_DEAN));
                break;
            default:
                return Collections.emptyList();
        }
        if (activityLevel == ActivityApplyConstants.LEVEL_SCHOOL) {
            chain.add(ActivityApplyConstants.APPROVER_SUPER_ADMIN);
        }
        return chain;
    }

    /** 变更审批：社长 → 指导老师 → 学院书记 */
    public static List<String> buildChangeChain() {
        return Arrays.asList(
                ActivityApplyConstants.APPROVER_PRESIDENT,
                ActivityApplyConstants.APPROVER_ADVISOR,
                ActivityApplyConstants.APPROVER_COLLEGE_DEAN);
    }
}
