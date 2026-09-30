package com.orbitcommerce.identity.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitcommerce.identity.dto.ChangePasswordDTO;
import com.orbitcommerce.identity.dto.UserDetailDTO;
import com.orbitcommerce.identity.dto.UserRegisterDTO;
import com.orbitcommerce.identity.dto.UserRegisterResponseDTO;
import com.orbitcommerce.identity.enums.RoleName;
import com.orbitcommerce.identity.enums.UserStatus;
import com.orbitcommerce.identity.exception.BusinessException;
import com.orbitcommerce.identity.exception.EmailAlreadyExistsException;
import com.orbitcommerce.identity.mapper.UserMapper;
import com.orbitcommerce.identity.model.OutboxEvent;
import com.orbitcommerce.identity.model.Role;
import com.orbitcommerce.identity.model.User;
import com.orbitcommerce.identity.repository.OutboxEventRepository;
import com.orbitcommerce.identity.repository.RefreshTokenRepository;
import com.orbitcommerce.identity.repository.RoleRepository;
import com.orbitcommerce.identity.repository.UserRepository;
import com.orbitcommerce.identity.security.HierarchyService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Value("${api.security.role.admin}")
    private String roleAdmin;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HierarchyService hierarchyService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, HierarchyService hierarchyService,
                       RefreshTokenRepository refreshTokenRepository, TokenService tokenService,
                       UserMapper userMapper, PasswordEncoder passwordEncoder,
                       OutboxEventRepository outboxEventRepository, MeterRegistry meterRegistry, Tracer tracer) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hierarchyService = hierarchyService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenService = tokenService;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.meterRegistry = meterRegistry;
        this.tracer = tracer;
        this.objectMapper = new ObjectMapper();
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public UserRegisterResponseDTO createUser(UserRegisterDTO userRegisterDTO) {

        Span span = tracer.nextSpan().name("userService.createUser").start();
        Counter failCounter = meterRegistry.counter("userService.createUser.fail");

        try(Tracer.SpanInScope ws = tracer.withSpan(span)) {

            span.tag("user.email", userRegisterDTO.email());

            if (userRepository.existsByEmail(userRegisterDTO.email())) {
                failCounter.increment();
                throw new EmailAlreadyExistsException("Email already exists");
            }
            User userEntity = userMapper.toEntity(userRegisterDTO);
            userEntity.setPasswordHash(passwordEncoder.encode(userRegisterDTO.password()));

            Role role = roleRepository.findByName(RoleName.CUSTOMER);
            userEntity.addRole(role);
            User userCreated = userRepository.save(userEntity);
            span.tag("user.id", userCreated.getId().toString());
            span.event("User saved in database");
            log.info("User created successfully with id: {}", userCreated.getId());

            publishUserRegisteredEvent(userCreated);

            return userMapper.toRegisterResponselDTO(userCreated);
        } catch (Exception e) {
            span.error(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private OutboxEvent publishUserRegisteredEvent(User user) {

        Span outboxSpan = tracer.nextSpan().name("outbox.createEvent").start();

        try (Tracer.SpanInScope ws = tracer.withSpan(outboxSpan)) {
            outboxSpan.tag("outbox.event_type", "user.registered");
            outboxSpan.tag("outbox.aggregate_id", user.getId().toString());

            Map<String, Object> payload = Map.of(
                    "userId", user.getId(),
                    "fullName", user.getFullName(),
                    "email", user.getEmail()
            );

            JsonNode payloadNode = objectMapper.valueToTree(payload);

            String traceId = outboxSpan.context().traceId();

            OutboxEvent outboxEvent = new OutboxEvent(
                    user.getId(),
                    "user.registered",
                    payloadNode,
                    traceId
            );

            OutboxEvent savedEvent = outboxEventRepository.save(outboxEvent);

            outboxSpan.tag("outbox.event_id", savedEvent.getId().toString());
            log.info("Outbox event created successfully");

            return savedEvent;
        } catch (Exception e) {
            outboxSpan.error(e);
            throw e;
        } finally {
            outboxSpan.end();
        }

    }

    @Transactional
    public void blockUser(UUID id) {
        userRepository.findById(id).ifPresentOrElse(
                user -> {
                    user.setStatus(UserStatus.BLOCKED);
                    userRepository.save(user);
                },
                () -> {
                    throw new EntityNotFoundException("User not found!");
                }
        );

        tokenService.revokeAllSessions(id);
        refreshTokenRepository.revokeAllByUserId(id);
    }

    public UserDetailDTO userDetails(User user) {
        return userMapper.toDetailDTO(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws EntityNotFoundException {
        ArrayList<UserStatus> statuses = new ArrayList<>();
        statuses.add(UserStatus.BLOCKED);
        statuses.add(UserStatus.DELETED);
        return userRepository.findByEmailIgnoreCaseAndStatusNotIn(username, statuses)
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));
    }


    @Transactional
    public void unlockUser(UUID id) {
        userRepository.findById(id).ifPresentOrElse(
                user -> {
                    user.setStatus(UserStatus.ACTIVE);
                    userRepository.save(user);
                },
                () -> {
                    throw new EntityNotFoundException("User not found!");
                }
        );
    }

    @Transactional
    public void changePassword(User user, ChangePasswordDTO changePasswordDTO) {
        ArrayList<UserStatus> statuses = new ArrayList<>();
        statuses.add(UserStatus.BLOCKED);
        statuses.add(UserStatus.DELETED);
        User userEntity = userRepository.findByEmailIgnoreCaseAndStatusNotIn(user.getEmail(), statuses)
                .orElseThrow(() -> new EntityNotFoundException("User not found!"));


        validateCurrentPassword(changePasswordDTO.oldPassword(), user.getPasswordHash());
        validatePasswordNotReused(changePasswordDTO.newPassword(), user.getPasswordHash());

        userEntity.setPasswordHash(passwordEncoder.encode(changePasswordDTO.newPassword()));

    }

    private void validateCurrentPassword(String rawPassword, String encodedHash) {
        if (!passwordEncoder.matches(rawPassword, encodedHash)) {
            throw new BusinessException("The new password is incorrect");
        }
    }

    private void validatePasswordNotReused(String newRawPassword, String currentEncodedHash) {
        if (passwordEncoder.matches(newRawPassword, currentEncodedHash)) {
            throw new BusinessException("The new password is not equal to the current password");
        }
    }

    @Transactional
    public void softDelete(UUID userId, User userLoggedIn) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if(!hierarchyService.verifyIfUserCanDelete(userLoggedIn, user, roleAdmin)) {
            throw new AccessDeniedException("It is not allowed to delete this user");
        }

        user.anonymizeAndDelete();
        refreshTokenRepository.revokeAllByUserId(userId);
    }



}
