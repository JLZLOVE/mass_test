package untiy.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.NoticeCategory;
import untiy.entity.NoticeInfo;
import untiy.entity.NoticeReadRecord;
import untiy.entity.SysUser;
import untiy.entity.constants.NoticeConstants;
import untiy.entity.dto.NoticeSendDTO;
import untiy.entity.vo.NoticeDetailVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.NoticeCategoryMapper;
import untiy.mapper.NoticeInfoMapper;
import untiy.mapper.NoticeReadRecordMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysDepartmentMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.LoginUserDetails;
import untiy.security.NoticeScopeHelper;
import untiy.service.NoticeInfoService;
import untiy.utils.MPUtil;
import untiy.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NoticeInfoServiceImpl extends ServiceImpl<NoticeInfoMapper, NoticeInfo> implements NoticeInfoService {

    @Autowired
    private NoticeCategoryMapper noticeCategoryMapper;

    @Autowired
    private NoticeReadRecordMapper noticeReadRecordMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysDepartmentMapper sysDepartmentMapper;

    @Transactional
    @Override
    public Long send(NoticeSendDTO dto) {
        NoticeInfo notice = buildNoticeFromDto(dto);
        if (Boolean.TRUE.equals(dto.getDraft())) {
            notice.setStatus(NoticeConstants.STATUS_DRAFT);
            save(notice);
            return notice.getId();
        }
        if (notice.getScheduledPublishTime() != null && notice.getScheduledPublishTime().isAfter(LocalDateTime.now())) {
            notice.setStatus(NoticeConstants.STATUS_DRAFT);
            save(notice);
            return notice.getId();
        }
        doPublish(notice);
        return notice.getId();
    }

    @Transactional
    @Override
    public Long saveDraft(NoticeSendDTO dto) {
        dto.setDraft(true);
        return send(dto);
    }

    @Transactional
    @Override
    public void publishNow(Long id) {
        NoticeInfo notice = requireNotice(id);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        assertPublisherOrAdmin(notice, user);
        if (notice.getStatus() == null || notice.getStatus() != NoticeConstants.STATUS_DRAFT) {
            throw new EIException(ErrorConfig.NOTICE_STATUS_INVALID_CODE, ErrorConfig.NOTICE_STATUS_INVALID_MSG);
        }
        doPublish(notice);
    }

    @Transactional
    @Override
    public void withdraw(Long id) {
        NoticeInfo notice = requireNotice(id);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        if (notice.getRevocable() != null && notice.getRevocable() == 0) {
            throw new EIException(ErrorConfig.NOTICE_NOT_REVOCABLE_CODE, ErrorConfig.NOTICE_NOT_REVOCABLE_MSG);
        }
        if (notice.getStatus() == null || notice.getStatus() != NoticeConstants.STATUS_PUBLISHED) {
            throw new EIException(ErrorConfig.NOTICE_STATUS_INVALID_CODE, ErrorConfig.NOTICE_STATUS_INVALID_MSG);
        }
        assertCanWithdraw(notice, user);
        notice.setStatus(NoticeConstants.STATUS_WITHDRAWN);
        notice.setLongTermVisible(0);
        notice.setIsPinned(0);
        notice.setUpdateTime(LocalDateTime.now());
        updateById(notice);
        log.info("通知 {} 已撤回", notice.getId());
    }

    @Transactional
    @Override
    public NoticeDetailVO getDetail(Long id) {
        NoticeInfo notice = requireNotice(id);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        assertVisible(notice, user);
        assertIsReceiver(notice, user.getUserId());
        markRead(notice.getId(), user.getUserId());

        NoticeDetailVO vo = buildDetailVO(notice, user);
        vo.setRead(true);
        NoticeReadRecord record = findReadRecord(notice.getId(), user.getUserId());
        vo.setConfirmed(record != null && record.getIsConfirmed() != null && record.getIsConfirmed() == 1);
        return vo;
    }

    @Transactional
    @Override
    public void confirmRead(Long id) {
        NoticeInfo notice = requireNotice(id);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        assertVisible(notice, user);
        assertIsReceiver(notice, user.getUserId());
        if (notice.getNeedConfirm() == null || notice.getNeedConfirm() != 1) {
            throw new EIException(ErrorConfig.NOTICE_STATUS_INVALID_CODE, "该通知无需确认阅读");
        }
        NoticeReadRecord record = findReadRecord(notice.getId(), user.getUserId());
        if (record == null) {
            record = new NoticeReadRecord();
            record.setNoticeId(notice.getId());
            record.setUserId(user.getUserId());
            record.setReadTime(LocalDateTime.now());
            record.setIsConfirmed(1);
            record.setConfirmTime(LocalDateTime.now());
            noticeReadRecordMapper.insert(record);
            return;
        }
        if (record.getIsConfirmed() != null && record.getIsConfirmed() == 1) {
            throw new EIException(ErrorConfig.NOTICE_ALREADY_CONFIRMED_CODE, ErrorConfig.NOTICE_ALREADY_CONFIRMED_MSG);
        }
        record.setIsConfirmed(1);
        record.setConfirmTime(LocalDateTime.now());
        if (record.getReadTime() == null) {
            record.setReadTime(LocalDateTime.now());
        }
        noticeReadRecordMapper.updateById(record);
    }

    @Override
    public IPage<NoticeInfo> myInbox(Map<String, Object> param) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        List<NoticeInfo> all = list(new LambdaQueryWrapper<NoticeInfo>()
                .in(NoticeInfo::getStatus, NoticeConstants.STATUS_PUBLISHED, NoticeConstants.STATUS_WITHDRAWN));
        List<NoticeInfo> visible = all.stream()
                .filter(n -> isVisibleToUser(n, user))
                .filter(n -> isReceiverForUser(n, user.getUserId()))
                .sorted(inboxComparator())
                .collect(Collectors.toList());
        return manualPage(visible, param);
    }

    @Override
    public IPage<NoticeInfo> mySent(Map<String, Object> param) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        Page<NoticeInfo> page = MPUtil.getPage(param);
        LambdaQueryWrapper<NoticeInfo> wrapper = new LambdaQueryWrapper<NoticeInfo>()
                .eq(NoticeInfo::getPublisherId, user.getUserId())
                .orderByDesc(NoticeInfo::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public NoticeDetailVO readStats(Long id) {
        NoticeInfo notice = requireNotice(id);
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        assertPublisherOrAdmin(notice, user);
        NoticeDetailVO vo = buildDetailVO(notice, user);
        long readCount = noticeReadRecordMapper.selectCount(new LambdaQueryWrapper<NoticeReadRecord>()
                .eq(NoticeReadRecord::getNoticeId, id)
                .isNotNull(NoticeReadRecord::getReadTime));
        long confirmCount = noticeReadRecordMapper.selectCount(new LambdaQueryWrapper<NoticeReadRecord>()
                .eq(NoticeReadRecord::getNoticeId, id)
                .eq(NoticeReadRecord::getIsConfirmed, 1));
        vo.setReadCount(readCount);
        vo.setConfirmCount(confirmCount);
        if (notice.getLongTermVisible() != null && notice.getLongTermVisible() == 0) {
            vo.setReceiverCount(noticeReadRecordMapper.selectCount(new LambdaQueryWrapper<NoticeReadRecord>()
                    .eq(NoticeReadRecord::getNoticeId, id)).longValue());
        } else {
            vo.setReceiverCount((long) NoticeScopeHelper.resolveReceiverUserIds(
                    sysUserMapper, sysUserRoleMapper, sysRoleMapper,
                    notice.getReceiverType(), notice.getReceiverValues()).size());
        }
        return vo;
    }

    /** 定时任务：发布到期草稿 */
    @Transactional
    public void publishScheduledDrafts() {
        LocalDateTime now = LocalDateTime.now();
        List<NoticeInfo> drafts = list(new LambdaQueryWrapper<NoticeInfo>()
                .eq(NoticeInfo::getStatus, NoticeConstants.STATUS_DRAFT)
                .isNotNull(NoticeInfo::getScheduledPublishTime)
                .le(NoticeInfo::getScheduledPublishTime, now));
        for (NoticeInfo notice : drafts) {
            doPublish(notice);
        }
    }

    /** 定时任务：取消过期置顶 */
    @Transactional
    public void expirePinnedNotices() {
        LocalDateTime now = LocalDateTime.now();
        List<NoticeInfo> pinned = list(new LambdaQueryWrapper<NoticeInfo>()
                .eq(NoticeInfo::getIsPinned, 1)
                .isNotNull(NoticeInfo::getPinExpireAt)
                .lt(NoticeInfo::getPinExpireAt, now));
        for (NoticeInfo notice : pinned) {
            notice.setIsPinned(0);
            notice.setUpdateTime(now);
            updateById(notice);
        }
    }

    private void doPublish(NoticeInfo notice) {
        LocalDateTime now = LocalDateTime.now();
        notice.setStatus(NoticeConstants.STATUS_PUBLISHED);
        notice.setPublishTime(now);
        notice.setUpdateTime(now);

        boolean pin = NoticeScopeHelper.shouldPin(notice.getImportance(), notice.getUrgency(),
                notice.getIsPinned() != null && notice.getIsPinned() == 1);
        notice.setIsPinned(pin ? 1 : 0);

        if (notice.getId() == null) {
            save(notice);
        } else {
            updateById(notice);
        }

        if (notice.getLongTermVisible() != null && notice.getLongTermVisible() == 0) {
            Set<Long> receivers = NoticeScopeHelper.resolveReceiverUserIds(
                    sysUserMapper, sysUserRoleMapper, sysRoleMapper,
                    notice.getReceiverType(), notice.getReceiverValues());
            noticeReadRecordMapper.delete(new LambdaQueryWrapper<NoticeReadRecord>()
                    .eq(NoticeReadRecord::getNoticeId, notice.getId()));
            for (Long uid : receivers) {
                NoticeReadRecord record = new NoticeReadRecord();
                record.setNoticeId(notice.getId());
                record.setUserId(uid);
                record.setIsConfirmed(0);
                noticeReadRecordMapper.insert(record);
            }
        }
        log.info("通知 {} 已发布", notice.getId());
    }

    private NoticeInfo buildNoticeFromDto(NoticeSendDTO dto) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysUser publisher = sysUserMapper.selectById(user.getUserId());
        validateCategory(dto.getCategoryId());

        String publisherType = NoticeScopeHelper.resolvePublisherType(
                sysUserRoleMapper, sysRoleMapper, sysClubMapper, user.getUserId());
        NoticeScopeHelper.assertCanSendToScope(publisherType, user.getUserId(),
                sysUserRoleMapper, sysRoleMapper, sysClubMapper, sysDepartmentMapper,
                dto.getReceiverType(), dto.getReceiverValues());

        if (Boolean.TRUE.equals(dto.getPinned()) && dto.getPinExpireAt() == null) {
            throw new EIException(ErrorConfig.NOTICE_PIN_EXPIRE_REQUIRED_CODE,
                    ErrorConfig.NOTICE_PIN_EXPIRE_REQUIRED_MSG);
        }

        LocalDateTime now = LocalDateTime.now();
        NoticeInfo notice = new NoticeInfo();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setCategoryId(dto.getCategoryId());
        notice.setPublisherId(user.getUserId());
        notice.setPublisherName(publisher != null ? publisher.getRealName() : user.getUsername());
        notice.setImportance(dto.getImportance() != null ? dto.getImportance() : NoticeConstants.IMPORTANCE_LOW);
        notice.setUrgency(dto.getUrgency() != null ? dto.getUrgency() : NoticeConstants.URGENCY_NORMAL);
        notice.setReceiverType(dto.getReceiverType());
        notice.setReceiverValues(dto.getReceiverValues());
        notice.setNeedConfirm(Boolean.TRUE.equals(dto.getNeedConfirm()) ? 1 : 0);
        notice.setScheduledPublishTime(dto.getScheduledPublishTime());
        notice.setLongTermVisible(Boolean.TRUE.equals(dto.getLongTermVisible()) ? 1 : 0);
        notice.setRevocable(1);
        notice.setSourceType(NoticeConstants.SOURCE_MANUAL);
        notice.setTemplateId(dto.getTemplateId());
        notice.setAttachmentMinLevel(dto.getAttachmentMinLevel() != null ? dto.getAttachmentMinLevel() : 4);
        if (dto.getAttachments() != null && !dto.getAttachments().isEmpty()) {
            notice.setAttachments(JSON.toJSONString(dto.getAttachments()));
        }
        if (Boolean.TRUE.equals(dto.getPinned())) {
            notice.setIsPinned(1);
            notice.setPinExpireAt(dto.getPinExpireAt());
        } else {
            notice.setIsPinned(NoticeScopeHelper.shouldPin(notice.getImportance(), notice.getUrgency(), false) ? 1 : 0);
        }
        notice.setCreateTime(now);
        notice.setUpdateTime(now);
        return notice;
    }

    private NoticeDetailVO buildDetailVO(NoticeInfo notice, LoginUserDetails user) {
        NoticeDetailVO vo = new NoticeDetailVO();
        vo.setNotice(notice);
        vo.setHighlight(NoticeScopeHelper.shouldHighlight(notice.getImportance(), notice.getUrgency()));
        vo.setVisibleAttachments(filterAttachments(notice, user.getEffectiveLevel()));
        vo.setQueryTime(LocalDateTime.now());
        return vo;
    }

    private List<String> filterAttachments(NoticeInfo notice, int userLevel) {
        if (StringUtils.isBlank(notice.getAttachments())) {
            return new ArrayList<>();
        }
        int minLevel = notice.getAttachmentMinLevel() != null ? notice.getAttachmentMinLevel() : 4;
        if (userLevel > minLevel) {
            return new ArrayList<>();
        }
        return JSON.parseArray(notice.getAttachments(), String.class);
    }

    private void markRead(Long noticeId, Long userId) {
        NoticeReadRecord record = findReadRecord(noticeId, userId);
        if (record == null) {
            record = new NoticeReadRecord();
            record.setNoticeId(noticeId);
            record.setUserId(userId);
            record.setReadTime(LocalDateTime.now());
            record.setIsConfirmed(0);
            noticeReadRecordMapper.insert(record);
        } else if (record.getReadTime() == null) {
            record.setReadTime(LocalDateTime.now());
            noticeReadRecordMapper.updateById(record);
        }
    }

    private NoticeReadRecord findReadRecord(Long noticeId, Long userId) {
        return noticeReadRecordMapper.selectOne(new LambdaQueryWrapper<NoticeReadRecord>()
                .eq(NoticeReadRecord::getNoticeId, noticeId)
                .eq(NoticeReadRecord::getUserId, userId));
    }

    private boolean isReceiverForUser(NoticeInfo notice, Long userId) {
        if (notice.getLongTermVisible() != null && notice.getLongTermVisible() == 0) {
            return findReadRecord(notice.getId(), userId) != null;
        }
        return NoticeScopeHelper.resolveReceiverUserIds(
                sysUserMapper, sysUserRoleMapper, sysRoleMapper,
                notice.getReceiverType(), notice.getReceiverValues()).contains(userId);
    }

    private boolean isVisibleToUser(NoticeInfo notice, LoginUserDetails user) {
        if (notice.getStatus() == NoticeConstants.STATUS_PUBLISHED) {
            return notice.getExpireTime() == null || !notice.getExpireTime().isBefore(LocalDateTime.now());
        }
        if (notice.getStatus() == NoticeConstants.STATUS_WITHDRAWN) {
            return user.getEffectiveLevel() <= Level.ADMIN
                    || Objects.equals(notice.getPublisherId(), user.getUserId());
        }
        return false;
    }

    private void assertVisible(NoticeInfo notice, LoginUserDetails user) {
        if (!isVisibleToUser(notice, user)) {
            throw new EIException(ErrorConfig.NOTICE_NOT_FOUND_CODE, ErrorConfig.NOTICE_NOT_FOUND_MSG);
        }
    }

    private void assertIsReceiver(NoticeInfo notice, Long userId) {
        if (userId.equals(notice.getPublisherId())) {
            return;
        }
        if (!isReceiverForUser(notice, userId) && SecurityUtils.getCurrentUser().getEffectiveLevel() > Level.ADMIN) {
            throw new EIException(ErrorConfig.NOTICE_NOT_RECEIVER_CODE, ErrorConfig.NOTICE_NOT_RECEIVER_MSG);
        }
    }

    private void assertPublisherOrAdmin(NoticeInfo notice, LoginUserDetails user) {
        if (Objects.equals(notice.getPublisherId(), user.getUserId())) {
            return;
        }
        if (user.getEffectiveLevel() <= Level.ADMIN) {
            return;
        }
        throw new EIException(ErrorConfig.NOTICE_NO_PERMISSION_CODE, ErrorConfig.NOTICE_NO_PERMISSION_MSG);
    }

    private void assertCanWithdraw(NoticeInfo notice, LoginUserDetails user) {
        if (Objects.equals(notice.getPublisherId(), user.getUserId())) {
            return;
        }
        if (user.getEffectiveLevel() <= Level.ADMIN) {
            return;
        }
        throw new EIException(ErrorConfig.NOTICE_NO_PERMISSION_CODE, ErrorConfig.NOTICE_NO_PERMISSION_MSG);
    }

    private void validateCategory(Long categoryId) {
        NoticeCategory category = noticeCategoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new EIException(ErrorConfig.NOTICE_CATEGORY_NOT_FOUND_CODE, ErrorConfig.NOTICE_CATEGORY_NOT_FOUND_MSG);
        }
    }

    private NoticeInfo requireNotice(Long id) {
        NoticeInfo notice = getById(id);
        if (notice == null) {
            throw new EIException(ErrorConfig.NOTICE_NOT_FOUND_CODE, ErrorConfig.NOTICE_NOT_FOUND_MSG);
        }
        return notice;
    }

    private Comparator<NoticeInfo> inboxComparator() {
        return Comparator
                .comparing((NoticeInfo n) -> n.getIsPinned() != null && n.getIsPinned() == 1 ? 0 : 1)
                .thenComparing(NoticeInfo::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private IPage<NoticeInfo> manualPage(List<NoticeInfo> list, Map<String, Object> param) {
        Page<NoticeInfo> page = MPUtil.getPage(param);
        int from = (int) ((page.getCurrent() - 1) * page.getSize());
        int to = Math.min(from + (int) page.getSize(), list.size());
        page.setTotal(list.size());
        if (from >= list.size()) {
            page.setRecords(new ArrayList<>());
        } else {
            page.setRecords(list.subList(from, to));
        }
        return page;
    }
}
