package untiy.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.ActivityApply;
import untiy.entity.ActivitySign;
import untiy.entity.NoticeInfo;
import untiy.entity.NoticeReadRecord;
import untiy.entity.SysCollege;
import untiy.entity.SysClub;
import untiy.entity.SysRole;
import untiy.entity.constants.NoticeConstants;
import untiy.mapper.ActivitySignMapper;
import untiy.mapper.NoticeInfoMapper;
import untiy.mapper.NoticeReadRecordMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 系统自动发布通知（如活动取消）。
 */
@Slf4j
@Component
public class NoticeAutoPublisher {

    @Autowired
    private NoticeInfoMapper noticeInfoMapper;

    @Autowired
    private NoticeReadRecordMapper noticeReadRecordMapper;

    @Autowired
    private ActivitySignMapper activitySignMapper;

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Transactional
    public void publishActivityCancelNotice(ActivityApply apply) {
        Set<Long> userIds = new HashSet<>();
        if (apply.getApplyUserId() != null) {
            userIds.add(apply.getApplyUserId());
        }
        List<ActivitySign> signs = activitySignMapper.selectList(
                new LambdaQueryWrapper<ActivitySign>().eq(ActivitySign::getActivityId, apply.getId()));
        signs.stream().map(ActivitySign::getUserId).forEach(userIds::add);

        if (userIds.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        NoticeInfo notice = new NoticeInfo();
        notice.setTitle("【活动取消通知】" + apply.getActivityName() + "活动已取消");
        notice.setContent("<p>活动「" + apply.getActivityName() + "」已取消。"
                + (apply.getRejectReason() != null ? "原因：" + apply.getRejectReason() : "") + "</p>");
        notice.setCategoryId(2L);
        notice.setPublisherId(apply.getApplyUserId());
        notice.setImportance(NoticeConstants.IMPORTANCE_HIGH);
        notice.setUrgency(NoticeConstants.URGENCY_URGENT);
        notice.setReceiverType(NoticeConstants.RECEIVER_USERS);
        notice.setReceiverValues(JSON.toJSONString(new ArrayList<>(userIds)));
        notice.setNeedConfirm(0);
        notice.setPublishTime(now);
        notice.setStatus(NoticeConstants.STATUS_PUBLISHED);
        notice.setIsPinned(1);
        notice.setLongTermVisible(0);
        notice.setRevocable(0);
        notice.setSourceType(NoticeConstants.SOURCE_ACTIVITY_CANCEL);
        notice.setAttachmentMinLevel(4);
        notice.setCreateTime(now);
        notice.setUpdateTime(now);
        noticeInfoMapper.insert(notice);

        for (Long uid : userIds) {
            NoticeReadRecord record = new NoticeReadRecord();
            record.setNoticeId(notice.getId());
            record.setUserId(uid);
            record.setIsConfirmed(0);
            noticeReadRecordMapper.insert(record);
        }
        log.info("活动 {} 取消，已自动通知 {} 人", apply.getActivityNo(), userIds.size());
    }

    /**
     * 活动编号被篡改时封锁活动并通知管理员
     */
    @Transactional
    public void publishBlockedNotice(ActivityApply apply) {
        Set<Long> userIds = new HashSet<>();

        if (apply.getActivityLevel() != null && apply.getActivityLevel() == 1) {
            // 院级活动 → 通知学院院长
            SysClub club = sysClubMapper.selectById(apply.getClubId());
            if (club != null && club.getCollegeId() != null) {
                SysCollege college = sysCollegeMapper.selectById(club.getCollegeId());
                if (college != null && college.getDeanId() != null) {
                    userIds.add(college.getDeanId());
                }
            }
        } else {
            // 校级活动 → 通知所有 SUPER_ADMIN
            List<SysRole> superAdminRoles = sysRoleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleLevel, 0));
            if (!superAdminRoles.isEmpty()) {
                List<Long> roleIds = superAdminRoles.stream().map(SysRole::getId).collect(Collectors.toList());
                List<untiy.entity.SysUserRole> userRoles = sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<untiy.entity.SysUserRole>().in(untiy.entity.SysUserRole::getRoleId, roleIds));
                for (untiy.entity.SysUserRole ur : userRoles) {
                    if (ur.getUserId() != null) {
                        userIds.add(ur.getUserId());
                    }
                }
            }
        }

        if (userIds.isEmpty()) {
            log.warn("活动 {} 被篡改，但无法确定管理员通知对象", apply.getActivityNo());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        NoticeInfo notice = new NoticeInfo();
        notice.setTitle("【安全警告】活动编号异常 - " + apply.getActivityNo());
        notice.setContent("<p>活动「" + apply.getActivityName() + "」编号（" + apply.getActivityNo()
                + "）与创建时间不一致，疑似数据被篡改，已自动封锁。</p>");
        notice.setCategoryId(2L);
        notice.setPublisherId(apply.getApplyUserId());
        notice.setImportance(NoticeConstants.IMPORTANCE_HIGH);
        notice.setUrgency(NoticeConstants.URGENCY_URGENT);
        notice.setReceiverType(NoticeConstants.RECEIVER_USERS);
        notice.setReceiverValues(JSON.toJSONString(new ArrayList<>(userIds)));
        notice.setNeedConfirm(0);
        notice.setPublishTime(now);
        notice.setStatus(NoticeConstants.STATUS_PUBLISHED);
        notice.setIsPinned(1);
        notice.setLongTermVisible(0);
        notice.setRevocable(0);
        notice.setSourceType(NoticeConstants.SOURCE_ACTIVITY_CANCEL);
        notice.setAttachmentMinLevel(4);
        notice.setCreateTime(now);
        notice.setUpdateTime(now);
        noticeInfoMapper.insert(notice);

        for (Long uid : userIds) {
            NoticeReadRecord record = new NoticeReadRecord();
            record.setNoticeId(notice.getId());
            record.setUserId(uid);
            record.setIsConfirmed(0);
            noticeReadRecordMapper.insert(record);
        }
        log.info("活动 {} 被篡改封锁，已通知管理员共 {} 人", apply.getActivityNo(), userIds.size());
    }
}
