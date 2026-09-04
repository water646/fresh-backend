package com.fresh.dto;

import lombok.Data;

@Data
public class GoodsPageQueryDTO {
    /**
     * 页码，默认 1
     */
    private Integer pageNum = 1;
    /**
     * 每页条数，默认 10
     */
    private Integer pageSize = 10;

    private String name;
    private Integer categoryId;

}
