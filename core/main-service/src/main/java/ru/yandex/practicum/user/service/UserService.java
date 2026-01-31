package ru.yandex.practicum.user.service;

import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.user.dto.NewUserRequest;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest userRequest);

    List<UserDto> getUsers(List<Long> ids, Pageable pageable);

    void deleteUser(Long userId);

    User getUserById(Long userId);

    User getUserByIdOrThrow(Long userId);
}
