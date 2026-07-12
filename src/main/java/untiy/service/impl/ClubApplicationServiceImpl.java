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
import untiy.entity.SysClub;
import untiy.entity.SysCollege;
import untiy.entity.SysRole;
import untiy.entity.SysUser;
import untiy.entity.SysUserRole;
import untiy.entity.constants.ClubApplyConstants;
import untiy.entity.dto.ClubApproveDTO;
import untiy.entity.dto.ClubCreateApplyDTO;
import untiy.entity.dto.ClubDissolveApplyDTO;
import untiy.entity.vo.ClubApplicationDetailVO;
import untiy.exception.EIException;
import untiy.exception.ErrorConfig;
import untiy.mapper.ClubApplicationMapper;
import untiy.mapper.SysClubMapper;
import untiy.mapper.SysCollegeMapper;
import untiy.mapper.SysRoleMapper;
import untiy.mapper.SysUserMapper;
import untiy.mapper.SysUserRoleMapper;
import untiy.security.ClubDissolveExecutor;
import untiy.security.ClubSecurityHelper;
import untiy.security.LoginUserDetails;
import untiy.security.UserExposeHelper;
import untiy.security.UserSecurityHelper;
import untiy.service.ClubApplicationService;
import untiy.utils.ClubCodeGeneratorUtil;
import untiy.utils.MPUtil;
import untiy.utils.SecurityUtils;

import java.time.LocalDateTime;
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
    public void createApply(ClubCreateApplyDTO dto) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        ClubSecurityHelper.assertHasAdvisorRole(sysUserRoleMapper, sysRoleMapper, user.getUserId());
        SysUser proposedLeader = UserSecurityHelper.findActiveInScopeByUsername(sysUserMapper, dto.getProposedLeaderUsername());

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
        save(application);
        log.info("用户 {} 提交社团创建申请 {}", user.getUsername(), application.getApplicationNo());
    }

    @Transactional
    @Override
    public void dissolveApply(ClubDissolveApplyDTO dto) {
        LoginUserDetails user = SecurityUtils.getCurrentUser();
        SysClub club = sysClubMapper.selectById(dto.getClubId());
        if (club == null) {
            throw new EIException(ErrorConfig.CLUB_NOT_FOUND_CODE, ErrorConfig.CLUB_NOT_FOUND_MSG);
        }
        if (club.getStatus() == null || club.getStatus() != ClubApplyConstants.CLUB_STATUS_NORMAL) {
            throw new EIException(ErrorConfig.CLUB_NOT_NORMAL_CODE, ErrorConfig.CLUB_NOT_NORMAL_MSG);
        }
        if (!user.getUserId().equals(club.getAdvisorId())) {
            throw new EIException(ErrorConfig.CLUB_NOT_ADVISOR_CODE, ErrorConfig.CLUB_NOT_ADVISOR_MSG);
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
        application.setDissolveReason(dto.getDissolveReason());
        application.setApplicantId(user.getUserId());
        application.setApplicantName(user.getSysUser().getRealName());
        application.setStatus(ClubApplyConstants.STATUS_PENDING_COLLEGE);
        application.setCreateTime(now);
        application.setUpdateTime(now);
        save(application);
        log.info("用户 {} 提交社团解散申请 clubId={}", user.getUsername(), dto.getClubId());
    }

    @Override
    public IPage<ClubApplication> pageQuery(Map<String, Object> param, ClubApplication query) {
        Page<ClubApplication> page = MPUtil.getPage(param);
        QueryWrapper<ClubApplication> wrapper = new QueryWrapper<>();

        if (query != null) {
            if (query.getApplicantId() != null) {
                wrapper.eq("applicant_id", query.getApplicantId());
            }
            if (query.getStatus() != null) {
                wrapper.eq("status", query.getStatus());
            }
            if (query.getApplyType() != null) {
                wrapper.eq("apply_type", query.getApplyType());
            }
        }
        MPUtil.between(wrapper, param);
        wrapper.orderByDesc("create_time");
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public ClubApplicationDetailVO getDetail(Long id) {
        ClubApplication application = getById(id);
        if (application == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        return buildDetailVO(application);
    }

    @Override
    public ClubApplicationDetailVO getDetailByApplicantUsername(String username) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
        if (user == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        ClubApplication application = getOne(new LambdaQueryWrapper<ClubApplication>()
                .eq(ClubApplication::getApplicantId, user.getId())
                .orderByDesc(ClubApplication::getCreateTime)
                .last("LIMIT 1"));
        if (application == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        return buildDetailVO(application);
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
    public void approveCollege(Long id, ClubApproveDTO dto) {
        ClubApplication application = loadForCollegeApprove(id);
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
        log.info("学院审批申请 id={} approved={}", id, dto.getApproved());
    }

    @Transactional
    @Override
    public void approveAdmin(String username, ClubApproveDTO dto) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
        if (user == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        ClubApplication application = getOne(new LambdaQueryWrapper<ClubApplication>()
                .eq(ClubApplication::getApplicantId, user.getId())
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
        log.info("校级审批申请 username={} approved={}", username, dto.getApproved());
    }

    private ClubApplication loadForCollegeApprove(Long id) {
        ClubApplication application = getById(id);
        if (application == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        if (application.getStatus() == null || application.getStatus() != ClubApplyConstants.STATUS_PENDING_COLLEGE) {
            throw new EIException(ErrorConfig.CLUB_APPLY_STATUS_INVALID_CODE, ErrorConfig.CLUB_APPLY_STATUS_INVALID_MSG);
        }
        return application;
    }

    private ClubApplication loadForAdminApprove(Long id) {
        ClubApplication application = getById(id);
        if (application == null) {
            throw new EIException(ErrorConfig.CLUB_APPLY_NOT_FOUND_CODE, ErrorConfig.CLUB_APPLY_NOT_FOUND_MSG);
        }
        if (application.getStatus() == null || application.getStatus() != ClubApplyConstants.STATUS_COLLEGE_APPROVED) {
            throw new EIException(ErrorConfig.CLUB_APPLY_STATUS_INVALID_CODE, ErrorConfig.CLUB_APPLY_STATUS_INVALID_MSG);
        }
        return application;
    }

    private void activateClub(ClubApplication application, LocalDateTime now) {
        SysClub club = new SysClub();
        club.setClubName(application.getClubName());
        club.setClubCode(clubCodeGeneratorUtil.generateClubCode());
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
        log.info("社团 {} 已激活，社长 userId={}", club.getClubCode(), application.getProposedLeaderId());
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
                return dean != null ? dean.getRealName() + "（学院院长）" : "学院院长";
            }
            return "学院院长";
        }
        if (application.getStatus() == ClubApplyConstants.STATUS_COLLEGE_APPROVED) {
            return "校级管理员";
        }
        return null;
    }
}
