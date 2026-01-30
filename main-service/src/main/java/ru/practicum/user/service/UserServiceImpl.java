package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.email())) {
            throw new ConflictException("Пользователь с таким e-mail уже существует: " + userRequest.email());
        }

        User user = userMapper.toEntity(userRequest);
        user = userRepository.save(user);
        log.info("Добавлен новый пользователь {}", user);
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(ASC, "id")
        );
        List<User> users = (ids == null || ids.isEmpty())
                ? userRepository.findAllList(sortedPageable)
                : userRepository.findByIdIn(ids, sortedPageable);
        return users.stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        getUserByIdOrThrow(userId);
        log.info("Удален пользователь с id {}", userId);
        userRepository.deleteById(userId);
    }

    @Override
    public User getUserById(Long userId) {
        return getUserByIdOrThrow(userId);
    }

    @Override
    public User getUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }
}
