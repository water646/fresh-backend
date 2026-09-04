package com.fresh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.context.BaseContext;
import com.fresh.entity.AddressBook;
import com.fresh.mapper.AddressBookMapper;
import com.fresh.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    public List<AddressBook> getByUserId(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> qw = new LambdaQueryWrapper<AddressBook>();
        qw.eq(AddressBook::getUserId,userId);

        List<AddressBook> addressBooks = addressBookMapper.selectList(qw);
        return addressBooks;
    }
}
