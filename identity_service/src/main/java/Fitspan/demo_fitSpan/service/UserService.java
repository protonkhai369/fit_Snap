package Fitspan.demo_fitSpan.service;

import Fitspan.demo_fitSpan.constant.PredefinedRole;
import Fitspan.demo_fitSpan.dto.request.ProfileRequest;
import Fitspan.demo_fitSpan.dto.request.UserRequest;
import Fitspan.demo_fitSpan.dto.request.UserUpdateRequest;
import Fitspan.demo_fitSpan.dto.response.UserResponse;
import Fitspan.demo_fitSpan.entity.Role;
import Fitspan.demo_fitSpan.entity.User;
import Fitspan.demo_fitSpan.event.UserCreatedEvent;
import Fitspan.demo_fitSpan.exception.AppException;
import Fitspan.demo_fitSpan.exception.ErrorCode;
import Fitspan.demo_fitSpan.mapper.ProfileMapper;
import Fitspan.demo_fitSpan.mapper.UserMapper;
import Fitspan.demo_fitSpan.repository.RoleRepository;
import Fitspan.demo_fitSpan.repository.UserRepository;
import Fitspan.demo_fitSpan.repository.httpClient.ProfileClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    RoleRepository roleRepository;
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    ProfileMapper profileMapper;
    ProfileClient profileClient;
    KafkaTemplate<String, Object> kafkaTemplate;

    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISITED);
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        user = userRepository.save(user);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dob = LocalDate.parse(formatter.format(request.getDateOfBirth())); // YYYY-MM-DD
        int age = Period.between(dob, LocalDate.now()).getYears();

        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .dateOfBirth(request.getDateOfBirth())
                .height(request.getHeight())
                .weight(request.getWeight())
                .gender(request.getGender())
                .age(age)
                .job(request.getJob())
                .styles(request.getStyles())
                .colors(request.getColors())
                .purposes(request.getPurposes())
                .build();

        kafkaTemplate.send("profile-creation", event);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("returnObject.username == authentication.name")
    public UserResponse getById(String id) {
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTEXISITED)));
    }

    @PreAuthorize("hasRole('ADMIN')||returnObject.username == authentication.name")
    public void delete(String id) {
        userRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')||returnObject.username == authentication.name")
    public UserResponse update(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTEXISITED));
        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getByInfo() {
        var context = SecurityContextHolder.getContext();
        var name = context.getAuthentication().getName();
        User user = userRepository.findById(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOTEXISITED));
        return userMapper.toUserResponse(user);
    }

}
