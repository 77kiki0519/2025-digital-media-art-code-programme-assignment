package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.LoginRequest;
import org.example.backend.dto.LoginResponse;
import org.example.backend.dto.RegisterRequest;
import org.example.backend.entity.Role;
import org.example.backend.entity.User;
import org.example.backend.exception.BusinessException;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roles(user.getRoles())
                .build();
    }
    
    /**
     * 用户注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStudentNo(request.getStudentNo());
        user.setTeacherNo(request.getTeacherNo());
        user.setStatus(1);
        
        // 分配角色
        Set<Role> roles = new HashSet<>();
        if (request.getTeacherNo() != null && !request.getTeacherNo().isEmpty()) {
            Role teacherRole = roleRepository.findByRoleCode("TEACHER")
                    .orElseThrow(() -> new BusinessException("教师角色不存在"));
            roles.add(teacherRole);
        } else {
            Role studentRole = roleRepository.findByRoleCode("STUDENT")
                    .orElseThrow(() -> new BusinessException("学生角色不存在"));
            roles.add(studentRole);
        }
        user.setRoles(roles);
        
        userRepository.save(user);
    }
}


