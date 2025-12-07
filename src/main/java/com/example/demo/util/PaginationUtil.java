package com.example.demo.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public class PaginationUtil {

    public static Pageable extractPage(Map<String, String> allParams) {

        PageRequest pageable;

        int page = Integer.parseInt(allParams.getOrDefault("page", "0"));
        int size = Integer.parseInt(allParams.getOrDefault("size", "5"));

        pageable = PageRequest.of(page, size);
        return pageable;
    }
}
