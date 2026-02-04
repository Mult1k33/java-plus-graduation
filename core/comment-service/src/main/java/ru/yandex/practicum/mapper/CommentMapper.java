package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import ru.yandex.practicum.dto.comment.CommentAdminDto;
import ru.yandex.practicum.dto.comment.CommentDto;
import ru.yandex.practicum.dto.comment.NewCommentDto;
import ru.yandex.practicum.dto.comment.UpdateCommentDto;
import ru.yandex.practicum.model.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "status", expression = "java(CommentStatus.PENDING)")
    Comment toComment(NewCommentDto newCommentDto, Long userId, Long eventId);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "eventId", source = "eventId")
    CommentDto toDto(Comment comment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updateCommentFromDto(UpdateCommentDto dto, @MappingTarget Comment comment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "text", source = "text")
    @Mapping(target = "status", source = "status")
    void updateFromAdminDto(CommentAdminDto commentAdminDto, @MappingTarget Comment comment);
}
