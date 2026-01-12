package com.aihub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DatabaseConfigDTO {
    
    @NotBlank(message = "数据库主机地址不能为空")
    private String host;
    
    @NotNull(message = "数据库端口不能为空")
    private Integer port;
    
    @NotBlank(message = "数据库名称不能为空")
    private String database;
    
    @NotBlank(message = "数据库用户名不能为空")
    private String username;
    
    // 密码可以为空（有些数据库用户可能没有密码）
    private String password;
}
