package com.aihub.dto;

import lombok.Data;

import java.util.List;

/**
 * 路由元信息DTO
 */
@Data
public class RouteMeta {

    private String icon;
    private String title;
    private Integer sortOrder;
    private List<String> roles;
    private Boolean showLink;
    private String activePath;
    private Boolean keepAlive;
    private String frameSrc;
}
