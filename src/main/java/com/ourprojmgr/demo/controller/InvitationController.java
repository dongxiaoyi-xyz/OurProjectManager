package com.ourprojmgr.demo.controller;

import com.ourprojmgr.demo.controller.utility.CurrentUser;
import com.ourprojmgr.demo.controller.utility.LoginRequired;
import com.ourprojmgr.demo.dbmodel.Invitation;
import com.ourprojmgr.demo.dbmodel.Project;
import com.ourprojmgr.demo.dbmodel.User;
import com.ourprojmgr.demo.exception.BusinessErrorType;
import com.ourprojmgr.demo.exception.BusinessException;
import com.ourprojmgr.demo.jsonmodel.InvitationJson;
import com.ourprojmgr.demo.service.IProjectService;
import com.ourprojmgr.demo.service.IUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理与邀请有关的 API
 */
@RestController
@RequestMapping("/api/projects/{projectId}/invitations")
public class InvitationController {
    //TODO di
    private IUserService userService;

    public void setUserService(IUserService userService) {
        this.userService = userService;
    }

    //TODO di
    private IProjectService projectService;

    public void setProjectService(IProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 发送邀请
     *
     * @param projectId  项目 ID
     * @param invitation 请求体中的 JSON
     * @param user       当前用户
     * @return 邀请的 JSON
     * @throws BusinessException 业务异常
     * @author 朱华彬
     */
    @PostMapping
    @LoginRequired
    public ResponseEntity<?> sendInvitations(
            @PathVariable Integer projectId,
            @RequestBody InvitationJson invitation,
            @CurrentUser User user) {
        Project project = getProjectOrThrow(projectId);
        checkAdminOrThrow(user, project);
        int receiverId = invitation.getReceiver().getId();
        User receiver = userService.getUserById(receiverId);
        if (receiver == null) {
            //接收者不存在
            throw new BusinessException(BusinessErrorType.USER_NOT_FOUND,
                    "Receiver with id " + receiverId + " not found.");
        }
        invitation = projectService.invitationToJson(
                projectService.sendInvitation(user, receiver, project));
        return new ResponseEntity<>(invitation, HttpStatus.CREATED);
    }

    /**
     * 获取项目中已发送的所有邀请
     *
     * @param projectId 项目 ID
     * @param user      当前用户
     * @return 邀请列表 JSON
     * @throws BusinessException 业务异常
     * @author 朱华彬
     */
    @GetMapping
    @LoginRequired
    public ResponseEntity<?> getInvitations(@PathVariable Integer projectId,
                                            @CurrentUser User user) {
        Project project = getProjectOrThrow(projectId);
        checkAdminOrThrow(user, project);
        List<InvitationJson> jsonList = new ArrayList<>();
        for (Invitation invitation : projectService.getInvitations(project)) {
            jsonList.add(projectService.invitationToJson(invitation));
        }
        return new ResponseEntity<>(jsonList, HttpStatus.OK);
    }


    /**
     * 获取某个邀请
     *
     * @param projectId 项目 ID
     * @param id        邀请 ID
     * @param user      当前用户
     * @return 一个邀请 JSON
     * @throws BusinessException 业务异常
     * @author 朱华彬
     */
    @GetMapping("/{id}")
    @LoginRequired
    public ResponseEntity<?> getInvitation(@PathVariable Integer projectId,
                                           @PathVariable Integer id,
                                           @CurrentUser User user) {
        Project project = getProjectOrThrow(projectId);
        checkAdminOrThrow(user, project);
        Invitation invitation = getInvitationOrThrow(id);
        InvitationJson json = projectService.invitationToJson(invitation);
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    /**
     * 取消邀请
     *
     * @param id   邀请 ID
     * @param user 当前用户
     * @throws BusinessException 业务异常
     * @author 朱华彬
     */
    @GetMapping("/{id}/canceled")
    @LoginRequired
    public ResponseEntity<?> cancelInvitation(@PathVariable Integer id, @CurrentUser User user) {
        Invitation invitation = getInvitationOrThrow(id);
        projectService.cancelInvitation(user, invitation);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 接受邀请
     *
     * @param id   邀请 ID
     * @param user 当前用户
     * @author 朱华彬
     */
    @GetMapping("/{id}/accept")
    @LoginRequired
    public ResponseEntity<?> acceptInvitation(@PathVariable Integer id, @CurrentUser User user) {
        Invitation invitation = getInvitationOrThrow(id);
        projectService.acceptInvitation(user, invitation);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 拒绝邀请
     *
     * @param id   邀请 ID
     * @param user 当前用户
     * @author 朱华彬
     */
    @GetMapping("/{id}/reject")
    @LoginRequired
    public ResponseEntity<?> rejectInvitation(@PathVariable Integer id, @CurrentUser User user) {
        Invitation invitation = getInvitationOrThrow(id);
        projectService.rejectInvitation(user, invitation);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 获取 Invitation，若不存在则抛异常
     *
     * @param id 邀请 ID
     * @return Invitation 实体类
     * @throws BusinessException 邀请不存在
     * @author 朱华彬
     */
    private Invitation getInvitationOrThrow(int id) {
        Invitation invitation = projectService.getInvitationById(id);
        if (invitation == null) {
            throw new BusinessException(BusinessErrorType.INVITATION_NOT_FOUND,
                    "Invitation with id " + id + "not found.");
        }
        return invitation;
    }

    /**
     * 获取 Project，若不存在则抛异常
     *
     * @param id 项目 ID
     * @return Project 实体类
     * @throws BusinessException 项目不存在
     * @author 朱华彬
     */
    private Project getProjectOrThrow(int id) {
        Project project = projectService.getProjectById(id);
        if (project == null) {
            throw new BusinessException(BusinessErrorType.PROJECT_NOT_FOUND,
                    "Project with id " + id + " not found.");
        }
        return project;
    }

    /**
     * 若不是本项目的 Admin，则抛异常
     *
     * @param user    用户
     * @param project 项目
     * @throws BusinessException 不是 Admin
     * @author 朱华彬
     */
    private void checkAdminOrThrow(User user, Project project) {
        if (!projectService.isAdminOf(user, project)) {
            //不是本项目的 Admin
            throw new BusinessException(BusinessErrorType.PERMISSION_DENIED, "Not Admin");
        }
    }

}
