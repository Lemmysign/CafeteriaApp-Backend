package Evercare_CafeteriaApp.ServiceImplementation;

import Evercare_CafeteriaApp.Model.Transaction;
import Evercare_CafeteriaApp.Repository.PaymentRepoPackage.TransactionRepository;
import Evercare_CafeteriaApp.Services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
}
