package Evercare_CafeteriaApp.Mapper;

import Evercare_CafeteriaApp.DTO.PaymentDtoPackage.TransactionDTO;
import Evercare_CafeteriaApp.Model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "customerId", source = "customer.id") // Extract Customer ID instead of embedding full object
    TransactionDTO fromTransaction(Transaction transaction);

    @Mapping(target = "customer.id", source = "customerId")
    Transaction fromTransactionDTO(TransactionDTO transactionDTO);

    List<TransactionDTO> fromTransactions(List<Transaction> transactions);
    List<Transaction> fromTransactionDTOs(List<TransactionDTO> transactionDTOs);

}
