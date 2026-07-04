package untiy.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import untiy.entity.ClubApplication;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ClubApplicationDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private ClubApplication application;

    /** 当前待审批人描述（如院长姓名、校级管理员） */
    private String currentApprover;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime queryTime;
}
