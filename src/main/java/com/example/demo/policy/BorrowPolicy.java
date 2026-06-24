package com.example.demo.policy;

import com.example.demo.exception.DailyBorrowLimitExceededException;
import com.example.demo.exception.UnreturnedBorrowExistsException;
import org.springframework.stereotype.Component;

@Component
public class BorrowPolicy {

    public void validateNoActiveBorrow(boolean hasUnreturnedBorrow) {
        if (hasUnreturnedBorrow) {
            throw new UnreturnedBorrowExistsException("まだ貸出返却されていないの貸し出しがあります。");
        }
    }

    public void validateDailyLimit(boolean exists, Boolean limitReached) {
        if (exists && limitReached) {
            throw new DailyBorrowLimitExceededException("一日の借入限度額に達しました。");
        }
    }
}