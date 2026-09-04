package com.fresh.controller.user;

import com.fresh.context.BaseContext;
import com.fresh.dto.AddressBookDTO;
import com.fresh.entity.AddressBook;
import com.fresh.result.Result;
import com.fresh.service.AddressBookService;
import org.redisson.MapWriterTask;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/address")
public class AddressBookController {

    @Autowired
    AddressBookService addressBookService;

    @PostMapping("/add")
    public Result add(@RequestBody AddressBookDTO addressBookDTO){

        Long userId = BaseContext.getCurrentId();

        AddressBook addressBook = new AddressBook();
        BeanUtils.copyProperties(addressBookDTO,addressBook);
        addressBook.setUserId(userId);

        addressBookService.save(addressBook);

        return Result.success();
    }

    @GetMapping("/delete")
    public Result delete(Long id){
        addressBookService.removeById(id);
        return Result.success();
    }

    @GetMapping("/get")
    public Result get(Long id){
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }

    @GetMapping("/getall")
    public Result getall(){
        List<AddressBook> addressBooks = addressBookService.getByUserId();
        return Result.success(addressBooks);
    }


}
