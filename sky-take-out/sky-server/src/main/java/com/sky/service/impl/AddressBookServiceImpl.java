package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    public List<AddressBook> list() {
        return addressBookMapper.listByUserId(BaseContext.getCurrentId());
    }

    @Override
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    @Override
    public AddressBook getDefault() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> list = addressBookMapper.listByUserId(userId);
        for (AddressBook addressBook : list) {
            if (addressBook.getIsDefault() == 1) {
                return addressBook;
            }
        }
        return null;
    }

    @Override
    public void save(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);

        // 如果是第一个地址，自动设为默认地址
        List<AddressBook> existingList = addressBookMapper.listByUserId(userId);
        if (existingList == null || existingList.isEmpty()) {
            addressBook.setIsDefault(1);
        } else if (addressBook.getIsDefault() == null) {
            addressBook.setIsDefault(0);
        }

        addressBookMapper.insert(addressBook);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        Long userId = BaseContext.getCurrentId();
        addressBookMapper.updateIsDefaultToZero(userId);
        addressBookMapper.updateIsDefaultToOne(id);
    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }
}
