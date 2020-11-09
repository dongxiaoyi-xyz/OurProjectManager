package com.ourprojmgr.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * 负责处理用户相关请求的控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/logout")
    @LoginRequired
    public ResponseEntity<?> logout() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
