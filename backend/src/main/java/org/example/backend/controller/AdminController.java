package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.common.Result;
import org.example.backend.entity.User;
import org.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器 - 包含密码重置等管理功能
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 重置所有测试用户密码（开发用）
     */
    @PostMapping("/reset-passwords")
    public Result<Map<String, String>> resetPasswords() {
        Map<String, String> result = new HashMap<>();
        
        try {
            // 更新admin密码
            userRepository.findByUsername("admin").ifPresent(user -> {
                user.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(user);
                result.put("admin", "密码已重置为: admin123");
            });
            
            // 更新teacher1密码
            userRepository.findByUsername("teacher1").ifPresent(user -> {
                user.setPassword(passwordEncoder.encode("teacher123"));
                userRepository.save(user);
                result.put("teacher1", "密码已重置为: teacher123");
            });
            
            // 更新student1密码
            userRepository.findByUsername("student1").ifPresent(user -> {
                user.setPassword(passwordEncoder.encode("student123"));
                userRepository.save(user);
                result.put("student1", "密码已重置为: student123");
            });
            
            if (result.isEmpty()) {
                return Result.error("未找到用户，请先运行数据库初始化脚本");
            }
            
            return Result.success("密码重置成功", result);
        } catch (Exception e) {
            return Result.error("密码重置失败: " + e.getMessage());
        }
    }
    
    /**
     * 显示所有用户的密码哈希（开发用）
     */
    @GetMapping("/show-passwords")
    public Result<Map<String, String>> showPasswords() {
        Map<String, String> passwords = new HashMap<>();
        
        List<User> users = userRepository.findAll();
        for (User user : users) {
            passwords.put(user.getUsername(), user.getPassword());
        }
        
        return Result.success(passwords);
    }
}


