package com.testtask.taskmanagement.mapper;

import com.testtask.taskmanagement.model.Comment;
import com.testtask.taskmanagement.pojo.CommentDTO;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class CommentMapper {
    public CommentDTO toDTO(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentDTO()
                .setId(comment.getId())
                .setText(comment.getText())
                .setAuthorId(comment.getAuthor().getId());
    }

    public List<CommentDTO> toCommentDTOList(List<Comment> comments) {
        if (comments == null) {
            return Collections.emptyList();
        }
        return comments.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
