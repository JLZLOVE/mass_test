package untiy.security;

import untiy.entity.ActivityApply;
import untiy.entity.ActivityApproveFlow;
import untiy.entity.ClubApplication;
import untiy.entity.SysUser;
import untiy.mapper.SysUserMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API 层用户标识转换：对外暴露 username（学号/工号），内部仍使用 userId。
 */
public final class UserExposeHelper {

    private UserExposeHelper() {
    }

    public static String usernameOf(SysUserMapper mapper, Long userId) {
        if (userId == null || mapper == null) {
            return null;
        }
        SysUser user = mapper.selectById(userId);
        return user != null ? user.getUsername() : null;
    }

    public static Map<Long, String> usernamesOf(SysUserMapper mapper, Collection<Long> userIds) {
        if (mapper == null || userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> ids = userIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return mapper.selectBatchIds(ids).stream()
                .filter(u -> u.getId() != null && u.getUsername() != null)
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername, (a, b) -> a));
    }

    public static void enrichActivityApply(SysUserMapper mapper, ActivityApply apply) {
        if (apply == null) {
            return;
        }
        apply.setApplyUsername(usernameOf(mapper, apply.getApplyUserId()));
    }

    public static void enrichActivityApplyList(SysUserMapper mapper, List<ActivityApply> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, String> cache = usernamesOf(mapper,
                list.stream().map(ActivityApply::getApplyUserId).collect(Collectors.toList()));
        for (ActivityApply apply : list) {
            if (apply != null && apply.getApplyUserId() != null) {
                apply.setApplyUsername(cache.get(apply.getApplyUserId()));
            }
        }
    }

    public static void enrichClubApplication(SysUserMapper mapper, ClubApplication application) {
        if (application == null) {
            return;
        }
        application.setProposedLeaderUsername(usernameOf(mapper, application.getProposedLeaderId()));
        application.setApplicantUsername(usernameOf(mapper, application.getApplicantId()));
        application.setCollegeApproverUsername(usernameOf(mapper, application.getCollegeApproverId()));
        application.setAdminApproverUsername(usernameOf(mapper, application.getAdminApproverId()));
    }

    public static void enrichApproveFlows(SysUserMapper mapper, List<ActivityApproveFlow> flows) {
        if (flows == null || flows.isEmpty()) {
            return;
        }
        Map<Long, String> cache = usernamesOf(mapper,
                flows.stream().map(ActivityApproveFlow::getApproveUserId).collect(Collectors.toList()));
        for (ActivityApproveFlow flow : flows) {
            if (flow != null && flow.getApproveUserId() != null) {
                flow.setApproveUsername(cache.get(flow.getApproveUserId()));
            }
        }
    }
}
