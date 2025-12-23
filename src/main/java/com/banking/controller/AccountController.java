package com.banking.controller;

import com.banking.entity.Account;
import com.banking.entity.Transaction;
import com.banking.entity.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import com.banking.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private JwtService jwtService;

    record TransferRequest(String toAccountNumber, BigDecimal amount) {}
    record AccountResponse(String accountNumber, BigDecimal balance) {}
    record TransferResponse(String message, AccountResponse account) {}

    @GetMapping
    public ResponseEntity<?> getAccounts(HttpServletRequest request) {
        String token = extractToken(request);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) return ResponseEntity.notFound().build();

        Account account = accountRepository.findByUser(user).orElse(null);
        if (account == null) {
            account = new Account(user);
            account.setBalance(BigDecimal.valueOf(1000)); // Starting balance
            accountRepository.save(account);
        }

        return ResponseEntity.ok(new AccountResponse(account.getAccountNumber(), account.getBalance()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request, HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) return ResponseEntity.badRequest().body("User not found");

        Account fromAccount = accountRepository.findByUser(user).orElse(null);
        if (fromAccount == null) return ResponseEntity.badRequest().body("Account not found");

        Account toAccount = accountRepository.findByAccountNumber(request.toAccountNumber()).orElse(null);
        if (toAccount == null) return ResponseEntity.badRequest().body("Destination account not found");

        if (fromAccount.getBalance().compareTo(request.amount()) < 0) {
            return ResponseEntity.badRequest().body("Insufficient funds");
        }

        // Transfer logic
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.amount()));
        toAccount.setBalance(toAccount.getBalance().add(request.amount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Record transaction
        Transaction transaction = new Transaction(
                fromAccount.getAccountNumber(),
                toAccount.getAccountNumber(),
                request.amount(),
                "TRANSFER"
        );
        transactionRepository.save(transaction);

        return ResponseEntity.ok(new TransferResponse(
                "Transfer successful: $" + request.amount() + " to " + request.toAccountNumber(),
                new AccountResponse(fromAccount.getAccountNumber(), fromAccount.getBalance())
        ));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(HttpServletRequest request) {
        String token = extractToken(request);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) return ResponseEntity.notFound().build();

        Account account = accountRepository.findByUser(user).orElse(null);
        if (account == null) return ResponseEntity.ok(List.of());

        List<Transaction> transactions = transactionRepository.findByFromAccountOrToAccountOrderByTimestampDesc(
                account.getAccountNumber(), account.getAccountNumber()
        );
        return ResponseEntity.ok(transactions);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}