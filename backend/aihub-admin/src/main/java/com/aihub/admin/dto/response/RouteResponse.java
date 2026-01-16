package com.aihub.admin.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 动态路由响应DTO
 */
@Data
public class RouteResponse {

    private String path;
    private String name;
    private String component;
    private String redirect;
    private RouteMeta meta;
    private List<RouteResponse> children;
}
