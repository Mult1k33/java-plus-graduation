package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.user.*;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.mapper.UserMapper;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.repository.UserRepository;

import java.util.List;

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
                Sort.by(Sort.Direction.ASC, "id")
        );

        List<User> users = (ids == null || ids.isEmpty())
                ? userRepository.findAllList(sortedPageable)
                : userRepository.findByIdIn(ids, sortedPageable);

        return userMapper.toDtoList(users);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        getUserEntityById(userId); // Проверка существования
        userRepository.deleteById(userId);
        log.info("Удален пользователь с id {}", userId);
    }

    @Override
    public UserShortDto getUserShortInfo(Long userId) {
        User user = getUserEntityById(userId);
        return userMapper.toShortDto(user);
    }

    private User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }
}
