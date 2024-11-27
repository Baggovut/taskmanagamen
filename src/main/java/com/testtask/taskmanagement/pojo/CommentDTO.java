package com.testtask.taskmanagement.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "DTO for comments")
public class CommentDTO {
    @Schema(description = "A comment's id", example = "1")
    private Long id;

    @Schema(description = "A comment's text", example = "I'm tired, I want to go home.")
    private String text;

    @Schema(description = "A author's id", example = "1")
    private Long authorId;
}
