package untiy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import untiy.entity.ClubApplication;
import untiy.entity.ClubCategory;
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.entity.dto.ClubAdminApproveDTO;
import untiy.entity.dto.ClubCollegeApproveDTO;
import untiy.entity.dto.ClubCreateApplyDTO;
import untiy.entity.dto.ClubDissolveApplyDTO;
import untiy.entity.vo.ClubApplicationDetailVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.exception.Level;
import untiy.exception.UsualRole;
import untiy.mapper.ClubApplicationMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.ClubDissolveExecutor;
import untiy.security.ClubLookupHelper;
import untiy.security.ClubSecurityHelper;
import untiy.security.LoginUserDetails;
import untiy.security.UserExposeHelper;
import untiy.security.UserScopeResolver;
import untiy.security.UserSecurityHelper;
import untiy.service.ClubApplicationService;
import untiy.utils.ClubCodeGeneratorUtil;
import untiy.utils.MPUtil;
import untiy.utils.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ClubApplicationServiceImpl extends ServiceImpl<ClubApplicationMapper, ClubApplication>
        implements ClubApplicationService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysClubMapper sysClubMapper;

    @Autowired
    private SysCollegeMapper sysCollegeMapper;

    @Autowired
    private ClubCodeGeneratorUtil clubCodeGeneratorUtil;

    @Autowired
    private ClubDissolveExecutor clubDissolveExecutor;

    @Transactional
    @Override
    public String createApply(ClubCreateApplyDTO dto) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        if (user.getEffectiveLevel() != Level.SUPER_ADMIN) {
            ClubSecurityHelper.assertHasAdvisorRole(sysUserRoleMapper, sysRoleMapper, user.getUserId());
        }

        if (!ClubCategory.isValid(dto.getCategory())) {
            throw new EIException(ErrorConfig.CLUB_CATEGORY_INVALID_CODE,
                    ErrorConfig.CLUB_CATEGORY_INVALID_MSG + "，仅允许：" + ClubCategory.allowedCategoriesText());
        }

        String leaderUsername = dto.getProposedLeaderUsername();
        SysUser proposedLeader = UserSecurityHelper.findInScope(sysUserMapper, leaderUsername);
        if (proposedLeader == null) {
            throw new EIException(ErrorConfig.CLUB_PROPOSED_LEADER_INVALID_CODE,
                    ErrorConfig.CLUB_PROPOSED_LEADER_INVALID_MSG);
        }
        UserSecurityHelper.assertUserEnabled(proposedLeader);

        if (clubDissolveExecutor.isClubNameDuplicateInCollege(dto.getCollegeId(), dto.getClubName())) {
            throw new EIException(ErrorConfig.CLUB_NAME_DUPLICATE_CODE, ErrorConfig.CLUB_NAME_DUPLICATE_MSG);
        }

        LocalDateTime now = LocalDateTime.now();
        ClubApplication application = new ClubApplication();
        application.setApplicationNo(clubCodeGeneratorUtil.generateApplicationNo());
        application.setApplyType(ClubApplyConstants.APPLY_TYPE_CREATE);
        application.setClubName(dto.getClubName());
        application.setCollegeId(dto.getCollegeId());
        application.setCategory(dto.getCategory());
        application.setDescription(dto.getDescription());
        application.setProposedLeaderId(proposedLeader.getId());
        application.setMaxMembers(dto.getMaxMembers());
        application.setApplicantId(user.getUserId());
        application.setApplicantName(user.getSysUser().getRealName());
        application.setStatus(ClubApplyConstants.STATUS_PENDING_COLLEGE);
        application.setCreateTime(now);
        application.setUpdateTime(now);
        if (!save(application)) {
            throw new EIException(ErrorConfig.CLUB_APPLY_SAVE_FAILED_CODE, ErrorConfig.CLUB_APPLY_SAVE_FAILED_MSG);
        }
        if (application.getId() == null || StringUtils.isBlank(application.getApplicationNo())) {
            log.error("社团申请保存后缺少 id 或 applicationNo");
            throw new EIException(ErrorConfig.CLUB_APPLY_SAVE_FAILED_CODE, ErrorConfig.CLUB_APPLY_SAVE_FAILED_MSG);
        }
        log.info("用户 {} 提交社团创建申请 {}（id={}），拟定社长 {}",
                user.getUsername(), application.getApplicationNo(), application.getId(), leaderUsername);
        return application.getApplicationNo();
    }

    @Override
    public ClubApplicationDetailVO getDetailByApplicationNo(String applicationNo) {
        if (StringUtils.isBlank(applicationNo)) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        ClubApplication application = getOne(new LambdaQueryWrapper<ClubApplication>()
                .eq(ClubApplication::getApplicationNo, applicationNo.trim()));
        if (application == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        return buildDetailVO(application);
    }

    @Override
    public ClubApplicationDetailVO getDetailByUsername(String username) {
        SysUser user = requireUserByUsername(username);
        ClubApplication application = list(new LambdaQueryWrapper<ClubApplication>()
                .eq(ClubApplication::getApplicantId, user.getId())
                .orderByDesc(ClubApplication::getCreateTime)
                .last("LIMIT 1")).stream().findFirst().orElse(null);
        if (application == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        return buildDetailVO(application);
    }

    @Transactional
    @Override
    public void dissolveApply(ClubDissolveApplyDTO dto) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysClub club = ClubLookupHelper.requireNormalByClubCode(sysClubMapper, dto.getClubCode());

        boolean isAdvisor = user.getUserId().equals(club.getAdvisorId());
        boolean isPresident = false;
        if (!isAdvisor && user.getEffectiveLevel() <= Level.CLUB_LEADER) {
            List<SysRole> roles = UserScopeResolver.loadActiveRoles(sysUserRoleMapper, sysRoleMapper, user.getUserId());
            java.util.Set<Long> presidentRoleIds = roles.stream()
                    .filter(r -> ClubApplyConstants.ROLE_CLUB_PRESIDENT.equals(r.getRoleCode()))
                    .map(SysRole::getId)
                    .collect(java.util.stream.Collectors.toSet());
            if (!presidentRoleIds.isEmpty()) {
                isPresident = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getUserId())
                        .eq(SysUserRole::getScopeType, ClubApplyConstants.SCOPE_TYPE_CLUB)
                        .eq(SysUserRole::getScopeId, club.getId())
                        .in(SysUserRole::getRoleId, presidentRoleIds)) > 0;
            }
        }
        if (!isAdvisor && !isPresident) {
            throw new EIException(ErrorConfig.CLUB_NOT_ADVISOR_CODE, ErrorConfig.CLUB_NOT_ADVISOR_MSG);
        }

        // 已有进行中的解散流程则拒绝
        long activeDissolve = count(new LambdaQueryWrapper<ClubApplication>()
                .eq(ClubApplication::getApplyType, ClubApplyConstants.APPLY_TYPE_DISSOLVE)
                .eq(ClubApplication::getClubName, club.getClubName())
                .eq(club.getCollegeId() != null, ClubApplication::getCollegeId, club.getCollegeId())
                .in(ClubApplication::getStatus,
                        ClubApplyConstants.STATUS_PENDING_COLLEGE,
                        ClubApplyConstants.STATUS_COLLEGE_APPROVED));
        if (activeDissolve > 0) {
            throw new EIException(ErrorConfig.CLUB_APPLY_STATUS_INVALID_CODE,
                    "该社团已有进行中的解散流程，请勿重复操作");
        }

        if (clubDissolveExecutor.hasActiveActivities(club.getId())) {
            throw new EIException(ErrorConfig.CLUB_HAS_ACTIVE_ACTIVITY_CODE, ErrorConfig.CLUB_HAS_ACTIVE_ACTIVITY_MSG);
        }

        LocalDateTime now = LocalDateTime.now();
        ClubApplication application = new ClubApplication();
        application.setApplicationNo(clubCodeGeneratorUtil.generateApplicationNo());
        application.setApplyType(ClubApplyConstants.APPLY_TYPE_DISSOLVE);
        application.setClubName(club.getClubName());
        application.setCollegeId(club.getCollegeId());
        application.setCategory(club.getCategory());
        application.setDissolveReason(dto.getDissolveReason());
        application.setApplicantId(user.getUserId());
        application.setApplicantName(user.getSysUser().getRealName());
        application.setStatus(ClubApplyConstants.STATUS_PENDING_COLLEGE);
        application.setCreateTime(now);
        application.setUpdateTime(now);
        save(application);
        log.info("用户 {} 提交社团解散申请 clubCode={}", user.getUsername(), dto.getClubCode());
    }

    @Override
    public IPage<ClubApplication> pageQuery(Map<String, Object> param, ClubApplication query, String username) {
        Page<ClubApplication> page = MPUtil.getPage(param);
        QueryWrapper<ClubApplication> wrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(username)) {
            SysUser applicant = requireUserByUsername(username.trim());
            wrapper.eq("applicant_id", applicant.getId());
        }
        if (query != null) {
            if (query.getStatus() != null) {
                wrapper.eq("status", query.getStatus());
            }
            if (query.getApplyType() != null) {
                wrapper.eq("apply_type", query.getApplyType());
            }
        }
        MPUtil.between(wrapper, param);
        wrapper.orderByDesc("create_time");
        IPage<ClubApplication> result = baseMapper.selectPage(page, wrapper);
        result.getRecords().forEach(app -> UserExposeHelper.enrichClubApplication(sysUserMapper, app));
        return result;
    }

    private ClubApplicationDetailVO buildDetailVO(ClubApplication application) {
        UserExposeHelper.enrichClubApplication(sysUserMapper, application);
        ClubApplicationDetailVO vo = new ClubApplicationDetailVO();
        vo.setApplication(application);
        vo.setCurrentApprover(resolveCurrentApprover(application));
        vo.setQueryTime(LocalDateTime.now());
        return vo;
    }

    @Transactional
    @Override
    public void approveCollege(ClubCollegeApproveDTO dto) {
        ClubApplication application = resolveForCollegeApprove(dto.getApplicationNo(), dto.getUsername());
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        ClubSecurityHelper.assertDeanOfCollege(sysCollegeMapper, user.getUserId(), application.getCollegeId());
        ClubSecurityHelper.assertNotApplicant(user.getUserId(), application.getApplicantId());

        LocalDateTime now = LocalDateTime.now();
        application.setCollegeApproverId(user.getUserId());
        application.setCollegeApproveTime(now);
        application.setCollegeApproveOpinion(dto.getOpinion());

        if (Boolean.TRUE.equals(dto.getApproved())) {
            application.setStatus(ClubApplyConstants.STATUS_COLLEGE_APPROVED);
        } else {
            application.setStatus(ClubApplyConstants.STATUS_REJECTED);
            application.setRejectReason(StringUtils.defaultIfBlank(dto.getOpinion(), "学院审批驳回"));
        }
        application.setUpdateTime(now);
        updateById(application);
        log.info("学院审批申请 applicationNo={} approved={}", application.getApplicationNo(), dto.getApproved());
    }

    @Transactional
    @Override
    public void approveAdmin(ClubAdminApproveDTO dto) {
        SysUser applicant = requireUserByUsername(dto.getUsername());
        ClubApplication application = getOne(new LambdaQueryWrapper<ClubApplication>()
                .eq(ClubApplication::getApplicantId, applicant.getId())
                .eq(ClubApplication::getStatus, ClubApplyConstants.STATUS_COLLEGE_APPROVED)
                .orderByDesc(ClubApplication::getCreateTime)
                .last("LIMIT 1"));
        if (application == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        LoginUserDetails loginUser = SecurityUtils.getCurrentUser();
        ClubSecurityHelper.assertSchoolAdmin(sysUserRoleMapper, sysRoleMapper, loginUser.getUserId());
        ClubSecurityHelper.assertNotApplicant(loginUser.getUserId(), application.getApplicantId());

        LocalDateTime now = LocalDateTime.now();
        application.setAdminApproverId(loginUser.getUserId());
        application.setAdminApproveTime(now);
        application.setAdminApproveOpinion(dto.getOpinion());

        if (Boolean.TRUE.equals(dto.getApproved())) {
            application.setStatus(ClubApplyConstants.STATUS_APPROVED);
            application.setUpdateTime(now);
            updateById(application);
            if (ClubApplyConstants.APPLY_TYPE_CREATE == application.getApplyType()) {
                activateClub(application, now);
            } else if (ClubApplyConstants.APPLY_TYPE_DISSOLVE == application.getApplyType()) {
                SysClub club = findClubForDissolveApplication(application);
                if (club != null) {
                    clubDissolveExecutor.executeDissolve(club.getId());
                }
            }
        } else {
            application.setStatus(ClubApplyConstants.STATUS_REJECTED);
            application.setRejectReason(StringUtils.defaultIfBlank(dto.getOpinion(), "校级审批驳回"));
            application.setUpdateTime(now);
            updateById(application);
        }
        log.info("校级审批申请 username={} approved={}", dto.getUsername(), dto.getApproved());
    }

    private ClubApplication resolveForCollegeApprove(String applicationNo, String username) {
        if (StringUtils.isNotBlank(applicationNo)) {
            ClubApplication application = getOne(new LambdaQueryWrapper<ClubApplication>()
                    .eq(ClubApplication::getApplicationNo, applicationNo.trim()));
            if (application == null) {
                throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
            }
            if (application.getStatus() == null || application.getStatus() != ClubApplyConstants.STATUS_PENDING_COLLEGE) {
                throw new EIException(ErrorConfig.CLUB_APPLY_STATUS_INVALID_CODE, ErrorConfig.CLUB_APPLY_STATUS_INVALID_MSG);
            }
            return application;
        }
        if (StringUtils.isNotBlank(username)) {
            SysUser applicant = requireUserByUsername(username.trim());
            ClubApplication application = getOne(new LambdaQueryWrapper<ClubApplication>()
                    .eq(ClubApplication::getApplicantId, applicant.getId())
                    .eq(ClubApplication::getStatus, ClubApplyConstants.STATUS_PENDING_COLLEGE)
                    .orderByDesc(ClubApplication::getCreateTime)
                    .last("LIMIT 1"));
            if (application == null) {
                throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
            }
            return application;
        }
        throw new EIException(ErrorConfig.BAD_REQUEST_CODE, "请提供 username 或 applicationNo");
    }

    private SysUser requireUserByUsername(String username) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
        if (user == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        return user;
    }

    private void activateClub(ClubApplication application, LocalDateTime now) {
        SysClub club = new SysClub();
        club.setClubName(application.getClubName());
        club.setClubCode(clubCodeGeneratorUtil.generateClubCode(application.getCategory(), now));
        club.setCategory(application.getCategory());
        club.setCollegeId(application.getCollegeId());
        club.setAdvisorId(application.getApplicantId());
        club.setDescription(application.getDescription());
        club.setStatus(ClubApplyConstants.CLUB_STATUS_NORMAL);
        club.setCreateTime(now);
        sysClubMapper.insert(club);

        SysRole presidentRole = ClubSecurityHelper.requireRoleByCode(sysRoleMapper, ClubApplyConstants.ROLE_CLUB_PRESIDENT);
        SysUserRole binding = new SysUserRole();
        binding.setUserId(application.getProposedLeaderId());
        binding.setRoleId(presidentRole.getId());
        binding.setScopeType(ClubApplyConstants.SCOPE_TYPE_CLUB);
        binding.setScopeId(club.getId());
        binding.setCreateTime(now);
        sysUserRoleMapper.insert(binding);
        log.info("社团 {} 已激活，clubCode={}", club.getClubName(), club.getClubCode());
    }

    private SysClub findClubForDissolveApplication(ClubApplication application) {
        if (application.getCollegeId() == null || application.getClubName() == null) {
            return null;
        }
        return sysClubMapper.selectOne(new LambdaQueryWrapper<SysClub>()
                .eq(SysClub::getCollegeId, application.getCollegeId())
                .eq(SysClub::getClubName, application.getClubName())
                .eq(SysClub::getStatus, ClubApplyConstants.CLUB_STATUS_NORMAL)
                .last("LIMIT 1"));
    }

    private String resolveCurrentApprover(ClubApplication application) {
        if (application.getStatus() == null) {
            return null;
        }
        if (application.getStatus() == ClubApplyConstants.STATUS_PENDING_COLLEGE && application.getCollegeId() != null) {
            SysCollege college = sysCollegeMapper.selectById(application.getCollegeId());
            if (college != null && college.getDeanId() != null) {
                SysUser dean = sysUserMapper.selectById(college.getDeanId());
                return dean != null ? dean.getRealName() + UsualRole.COLLEGE_ADMIN_MSG : UsualRole.COLLEGE_ADMIN_MSG;
            }
            return UsualRole.COLLEGE_ADMIN_MSG;
        }
        if (application.getStatus() == ClubApplyConstants.STATUS_COLLEGE_APPROVED) {
            return UsualRole.SCHOOL_ADMIN_MSG;
        }
        return null;
    }
}
