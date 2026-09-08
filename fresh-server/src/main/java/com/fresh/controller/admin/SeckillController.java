package com.fresh.controller.admin;


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

@RestController
@RequestMapping("/admin/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @PostMapping("/save")
    public Result save(@Valid @RequestBody SeckillGoodsAddDTO seckillGoodsAddDTO){
        seckillService.add(seckillGoodsAddDTO);
        return Result.success();
    }

    @DeleteMapping("delete")
    public Result delete(@RequestParam Integer id){
        seckillService.removeById(id);
        return Result.success();
    }

    @GetMapping("/get")
    public Result get(@RequestParam Integer id){
        SeckillGoods seckillGoods = seckillService.getById(id);
        return Result.success(seckillGoods);
    }

    @PutMapping("/update")
    public Result update(@Valid @RequestBody SeckillGoodsUpdateDTO seckillGoodsUpdateDTO){
        SeckillGoods seckillGoods = new SeckillGoods();
        BeanUtils.copyProperties(seckillGoodsUpdateDTO,seckillGoods);

        seckillService.update(seckillGoodsUpdateDTO);
        return Result.success();
    }


    /**
     * 秒杀商品分页查询
     *
     * @param seckillGoodsPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult> page(SeckillGoodsPageQueryDTO seckillGoodsPageQueryDTO) {

        PageResult pageResult = seckillService.pageQuery(seckillGoodsPageQueryDTO);

        return Result.success(pageResult);
    }

}
