package com.banking.controller;

import com.banking.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping("/debug/accounts/count")
    public long countAccounts() {
        return accountRepository.count();
    }
}