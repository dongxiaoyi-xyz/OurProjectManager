package com.ourprojmgr.demo.controller;

import com.ourprojmgr.demo.service.IProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 处理与项目有关的请求
 *
 * @author 谢可睿
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final IProjectService projectService;

    @Autowired
    public ProjectController(IProjectService projectService) {
        this.projectService = projectService;
    }

}
