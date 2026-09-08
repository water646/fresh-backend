package com.fresh.controller.user;


import com.fresh.dto.DoSeckillDTO;
import com.fresh.dto.SeckillGoodsAddDTO;
import com.fresh.dto.SeckillGoodsPageQueryDTO;
import com.fresh.dto.SeckillGoodsUpdateDTO;
import com.fresh.entity.SeckillGoods;
import com.fresh.result.PageResult;
import com.fresh.result.Result;
import com.fresh.service.SeckillService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController("userSeckillController")
@RequestMapping("/user/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/get")
    public Result get(@RequestParam Integer id){
        SeckillGoods seckillGoods = seckillService.getById(id);
        return Result.success(seckillGoods);
    }

    @PostMapping("/seckill")
    public Result seckill(@Valid @RequestBody DoSeckillDTO doSeckillDTO){

        Map<String,Object> map = seckillService.seckill(doSeckillDTO.getSeckillGoodsId());
        if(map.get("code").equals(0)){
            return Result.error((String) map.get("msg"));
        }
        else{
            return Result.success();
        }
    }


    /**
     * 秒杀商品分页查询
     *
     * @param seckillGoodsPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/pageOnSale")
    public Result<PageResult> pageOnSale(SeckillGoodsPageQueryDTO seckillGoodsPageQueryDTO) {

        seckillGoodsPageQueryDTO.setStatus(1);
        PageResult pageResult = seckillService.pageQuery(seckillGoodsPageQueryDTO);

        return Result.success(pageResult);
    }

}
