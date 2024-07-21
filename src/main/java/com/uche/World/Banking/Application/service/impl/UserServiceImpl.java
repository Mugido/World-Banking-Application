package com.uche.World.Banking.Application.service.impl;

import com.uche.World.Banking.Application.domain.entity.UserEntity;
import com.uche.World.Banking.Application.payload.request.CreditAndDebitRequest;
import com.uche.World.Banking.Application.payload.request.EmailDetails;
import com.uche.World.Banking.Application.payload.request.EnquiryRequest;
import com.uche.World.Banking.Application.payload.request.TransferRequest;
import com.uche.World.Banking.Application.payload.response.AccountInfo;
import com.uche.World.Banking.Application.payload.response.BankResponse;
import com.uche.World.Banking.Application.repository.UserRepository;
import com.uche.World.Banking.Application.service.EmailService;
import com.uche.World.Banking.Application.service.UserService;
import com.uche.World.Banking.Application.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

import static com.uche.World.Banking.Application.utils.AccountUtils.*;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final EmailService emailService;

    @Override
    public BankResponse balanceEnquiry(EnquiryRequest enquiryRequest) {
        boolean isAccountExits =
                userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());

        if(!isAccountExits){
            return  BankResponse.builder()
                    .responseCode(ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage(ACCOUNT_NUMBER_NON_EXISTS_MESSAGE)
                    .build();
        }

        UserEntity foundUserAccount =
                userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());


        return BankResponse.builder()
                .responseCode(ACCOUNT_EXISTS_CODE)
                .responseMessage(ACCOUNT_NUMBER_FOUND_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(foundUserAccount.getAccountBalance())
                        .accountNumber(enquiryRequest.getAccountNumber())
                        .accountName(foundUserAccount.getFirstName() +
                                " " + foundUserAccount.getLastName())
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest enquiryRequest) {
        boolean isAccountExits =
                userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());

        if(!isAccountExits){
            return  ACCOUNT_NUMBER_NON_EXISTS_MESSAGE;
        }

        UserEntity foundUserAccount =
                userRepository.findByAccountNumber
                        (enquiryRequest.getAccountNumber());

        return foundUserAccount.getFirstName() +
                " " + foundUserAccount.getLastName() +
                " " + foundUserAccount.getOtherName();
    }

    @Override
    public BankResponse creditAccount(CreditAndDebitRequest creditAndDebitRequest) {
        boolean isAccountExists =
        userRepository.existsByAccountNumber(creditAndDebitRequest.getAccountNumber());

        if(!isAccountExists){
            return BankResponse.builder()
                    .responseCode(ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage(ACCOUNT_NUMBER_NON_EXISTS_MESSAGE)
                    .build();
        }

        UserEntity userToCredit =
                userRepository.findByAccountNumber(creditAndDebitRequest.getAccountNumber());

        userToCredit.setAccountBalance(userToCredit.getAccountBalance()
                .add(creditAndDebitRequest.getAmount()));


        userRepository.save(userToCredit);

        EmailDetails creditAlert = EmailDetails.builder()
                .subject("CREDIT ALERT")
                .recipient(userToCredit.getEmail())
                .messageBody("Your account has been credited with " +
                creditAndDebitRequest.getAmount() + " from " +
                        userToCredit.getFirstName() + " your current account balance is "
                        + userToCredit.getAccountBalance())


                .build();

        emailService.sendEmailAlert(creditAlert);

        return BankResponse.builder()
                .responseCode(ACCOUNT_CREDITED_SUCCESS_CODE)
                .responseMessage(ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountName(userToCredit.getFirstName())
                        .accountBalance(userToCredit.getAccountBalance())
                        .accountNumber(userToCredit.getAccountNumber())
                        .bankName(userToCredit.getBankName())

                        .build())

                .build();
    }

    @Override
    public BankResponse debitAccount(CreditAndDebitRequest creditAndDebitRequest) {
        boolean isAccountExists =
                userRepository.existsByAccountNumber(creditAndDebitRequest.getAccountNumber());

        if(!isAccountExists){
            return BankResponse.builder()
                    .responseCode(ACCOUNT_NUMBER_NON_EXISTS_CODE)
                    .responseMessage(ACCOUNT_NUMBER_NON_EXISTS_MESSAGE)
                    .build();
        }

        UserEntity userToDebit =
                userRepository.findByAccountNumber(creditAndDebitRequest.getAccountNumber());

        //check for insufficient balance
        BigInteger availableBalance =
                userToDebit.getAccountBalance().toBigInteger();

        BigInteger debitAmount = creditAndDebitRequest.getAmount().toBigInteger();

        if(availableBalance.intValue() < debitAmount.intValue()){
            return BankResponse.builder()
                    .responseCode("006")
                    .responseMessage("Insufficient Balance")
                    .accountInfo(null)
                    .build();
        }else{
            userToDebit.setAccountBalance(userToDebit.getAccountBalance()
                    .subtract(creditAndDebitRequest.getAmount()));

            userRepository.save(userToDebit);

            EmailDetails debitAlert = EmailDetails.builder()
                    .subject("DEBIT ALERT")
                    .recipient(userToDebit.getEmail())
                    .messageBody("The sum of " + creditAndDebitRequest.getAmount() +
                            " has been deducted from your account! Your current" +
                            "account balance is " + userToDebit.getAccountBalance())
                    .build();

            emailService.sendEmailAlert(debitAlert);
        }

        return BankResponse.builder()
                .responseCode("007")
                .responseMessage("Account debited successfully")
                .accountInfo(AccountInfo.builder()
                        .accountName(userToDebit.getFirstName())
                        .accountBalance(userToDebit.getAccountBalance())
                        .accountNumber(userToDebit.getAccountNumber())
                        .bankName(userToDebit.getBankName())
                        .build())

                .build();
    }

    @Override
    public BankResponse transfer(TransferRequest request) {
        /**
         * 1. first check if the destination account number exists
         * 2. then check if amount to send is available.
         * 3. then deduct the amount to send from sender balance
         * 4. then add the send amount to receiver balance
         * 5. then send a debit alert and a credit alert to both sender and receiver
         */

        boolean isDestinationAccountExists
                = userRepository.existsByAccountNumber(
                request.getDestinationAccountNumber()
        );

        if(!isDestinationAccountExists){
            return BankResponse.builder()
                    .responseCode("008")
                    .responseMessage("Account number does not exists")
                    .build();
        }

        UserEntity sourceAccountUser =
                userRepository.findByAccountNumber(request.getSourceAccountNumber());

        if(request.getAmount().compareTo(sourceAccountUser.getAccountBalance())
        > 0){
            return BankResponse.builder()
                    .responseCode("009")
                    .responseMessage("INSUFFICIENT BALANCE")
                    .accountInfo(null)
                    .build();
        }


        sourceAccountUser.setAccountBalance(sourceAccountUser.getAccountBalance()
                .subtract(request.getAmount()));

        userRepository.save(sourceAccountUser);


        String sourceUsername = sourceAccountUser.getFirstName() + " " +
                sourceAccountUser.getOtherName() + " " +
                sourceAccountUser.getLastName();


        EmailDetails debitAlert = EmailDetails.builder()
                .subject("DEBIT ALERT")
                .recipient(sourceAccountUser.getEmail())
                .messageBody("The sum of " +
                        request.getAmount() + " has been deducted from your, account. Your current balance is " +
                        sourceAccountUser.getAccountBalance())
                .build();

        emailService.sendEmailAlert(debitAlert);


        UserEntity destinationAccountUser =
                userRepository.findByAccountNumber(request.getDestinationAccountNumber());
        destinationAccountUser.setAccountBalance(destinationAccountUser
                .getAccountBalance().add(request.getAmount()));

        userRepository.save(destinationAccountUser);


        EmailDetails creditAlert = EmailDetails.builder()
                .subject("CREDIT ALERT")
                .recipient(destinationAccountUser.getEmail())
                .messageBody("Your account has been credited with "
                + " " + request.getAmount() + " from"
                        + sourceUsername
                        + " your current account balance is "
                + destinationAccountUser.getAccountBalance())
                .build();

                emailService.sendEmailAlert(creditAlert);


        return BankResponse.builder()
                .responseCode("200")
                .responseMessage("Transfer Successful")
                .accountInfo(null)
                .build();
    }
}
