package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.ClubCouncil;
import untiy.entity.SysClub;
import untiy.entity.SysRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.entity.dto.CouncilInitiateDTO;
import untiy.entity.vo.CouncilSignRecordVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.mapper.ClubCouncilMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.ClubDissolveExecutor;
import untiy.security.ClubLookupHelper;
import untiy.security.ClubSecurityHelper;
import untiy.security.LoginUserDetails;
import untiy.entity.SysUserRole;
import untiy.service.ClubCouncilService;
import untiy.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ClubCouncilServiceImpl extends ServiceImpl<ClubCouncilMapper, ClubCouncil> implements ClubCouncilService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private ClubDissolveExecutor clubDissolveExecutor;

    @Transactional
    @Override
    public void initiate(CouncilInitiateDTO dto) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        ClubSecurityHelper.assertSuperAdmin(sysUserRoleMapper, sysRoleMapper, user.getUserId());

        SysClub club = ClubLookupHelper.requireNormalByClubCode(sysClubMapper, dto.getClubCode());
        if (club.getCollegeId() == null) {
            throw new EIException(ErrorConfig.CLUB_COLLEGE_OUT_OF_SCOPE_CODE, ErrorConfig.CLUB_COLLEGE_OUT_OF_SCOPE_MSG);
        }
        ClubSecurityHelper.assertCollegeInScope(sysUserRoleMapper, sysRoleMapper, user.getUserId(), club.getCollegeId());

        long inProgress = count(new LambdaQueryWrapper<ClubCouncil>()
                .eq(ClubCouncil::getClubId, club.getId())
                .eq(ClubCouncil::getStatus, ClubApplyConstants.COUNCIL_IN_PROGRESS));
        if (inProgress > 0) {
            throw new EIException(ErrorConfig.CLUB_COUNCIL_IN_PROGRESS_CODE, ErrorConfig.CLUB_COUNCIL_IN_PROGRESS_MSG);
        }
        if (clubDissolveExecutor.hasActiveActivities(club.getId())) {
            throw new EIException(ErrorConfig.CLUB_HAS_ACTIVE_ACTIVITY_CODE, ErrorConfig.CLUB_HAS_ACTIVE_ACTIVITY_MSG);
        }

        ClubCouncil council = new ClubCouncil();
        council.setClubId(club.getId());
        council.setInitiatorId(user.getUserId());
        council.setInitiatorName(user.getSysUser().getRealName());
        council.setCollegeId(club.getCollegeId());
        council.setReason(dto.getReason());
        council.setStatus(ClubApplyConstants.COUNCIL_IN_PROGRESS);
        council.setSignatories("[]");
        council.setCreateTime(LocalDateTime.now());
        save(council);
        log.info("超管 {} 发起社团 clubCode={} 合议解散", user.getUsername(), dto.getClubCode());
    }

    @Transactional
    @Override
    public void sign(Long councilId) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        int level = user.getEffectiveLevel();
        if (level > Level.ADMIN) {
            throw new EIException(ErrorConfig.ROLE_NO_PERMISSION_CODE, ErrorConfig.ROLE_NO_PERMISSION_MSG);
        }

        ClubCouncil council = getById(councilId);
        if (council == null) {
            throw new EIException(ErrorConfig.CLUB_COUNCIL_NOT_FOUND_CODE, ErrorConfig.CLUB_COUNCIL_NOT_FOUND_MSG);
        }
        if (council.getStatus() == null || council.getStatus() != ClubApplyConstants.COUNCIL_IN_PROGRESS) {
            throw new EIException(ErrorConfig.CLUB_COUNCIL_NOT_IN_PROGRESS_CODE, ErrorConfig.CLUB_COUNCIL_NOT_IN_PROGRESS_MSG);
        }
        ClubSecurityHelper.assertCollegeInScope(sysUserRoleMapper, sysRoleMapper, user.getUserId(), council.getCollegeId());

        List<CouncilSignRecordVO> records = parseSignatories(council.getSignatories());
        boolean alreadySigned = records.stream().anyMatch(r ->
                user.getUsername().equals(r.getUsername())
                        || (r.getUserId() != null && user.getUserId().equals(r.getUserId())));
        if (alreadySigned) {
            throw new EIException(ErrorConfig.CLUB_ALREADY_SIGNED_CODE, ErrorConfig.CLUB_ALREADY_SIGNED_MSG);
        }

        List<SysRole> roles = loadUserRoles(user.getUserId());
        String roleCode = roles.isEmpty() ? null : roles.get(0).getRoleCode();
        CouncilSignRecordVO record = new CouncilSignRecordVO();
        record.setUsername(user.getUsername());
        record.setRoleCode(roleCode);
        record.setLevel(level);
        record.setSignTime(LocalDateTime.now());
        records.add(record);

        council.setSignatories(writeSignatories(records));
        if (isCouncilPassed(records)) {
            council.setStatus(ClubApplyConstants.COUNCIL_APPROVED);
            council.setExecutedAt(LocalDateTime.now());
            updateById(council);
            clubDissolveExecutor.executeDissolve(council.getClubId());
            log.info("合议 id={} 达成解散条件，已执行解散", councilId);
        } else {
            updateById(council);
            log.info("用户 {} 完成合议 id={} 签字", user.getUsername(), councilId);
        }
    }

    private boolean isCouncilPassed(List<CouncilSignRecordVO> records) {
        long superCount = records.stream().filter(r -> r.getLevel() != null && r.getLevel() == Level.SUPER_ADMIN).count();
        long adminCount = records.stream().filter(r -> r.getLevel() != null && r.getLevel() == Level.ADMIN).count();
        if (superCount >= 1 && adminCount >= 2) {
            return true;
        }
        return superCount >= 3;
    }

    private List<CouncilSignRecordVO> parseSignatories(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<List<CouncilSignRecordVO>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String writeSignatories(List<CouncilSignRecordVO> records) {
        try {
            return OBJECT_MAPPER.writeValueAsString(records);
        } catch (Exception e) {
            throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "签名记录序列化失败");
        }
    }

    private List<SysRole> loadUserRoles(Long userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(java.util.stream.Collectors.toList());
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getStatus, 1));
    }
}
