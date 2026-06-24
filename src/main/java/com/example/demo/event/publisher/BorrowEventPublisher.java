package com.example.demo.event.publisher;

import com.example.demo.dto.BorrowAggregate;
import com.example.demo.dto.ReturnBorrowAggregate;
import com.example.demo.model.Borrow;

import java.util.List;

public interface BorrowEventPublisher {

    void publishBorrowCreated(BorrowAggregate aggregate, List<Borrow> entities);

    void publishReturnBorrowCreated(ReturnBorrowAggregate aggregate, List<Borrow> entities);
}