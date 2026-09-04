package com.fresh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.entity.AddressBook;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {
    public List<AddressBook> getByUserId();
}
