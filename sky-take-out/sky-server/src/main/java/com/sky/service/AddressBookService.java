package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    List<AddressBook> list();

    AddressBook getById(Long id);

    AddressBook getDefault();

    void save(AddressBook addressBook);

    void update(AddressBook addressBook);

    void setDefault(Long id);

    void deleteById(Long id);
}
