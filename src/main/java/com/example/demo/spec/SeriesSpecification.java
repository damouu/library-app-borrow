package com.example.demo.spec;

import com.example.demo.model.Series;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SeriesSpecification {
    public static Specification<Series> filterSeries(Map allParams) {
        if (!(allParams.containsKey("page") && allParams.containsKey("size"))) {
            return (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                String title = (String) allParams.get("title");
                String author = (String) allParams.get("author");
                String illustrator = (String) allParams.get("illustrator");
                String genre = (String) allParams.get("genre");
                String publisher = (String) allParams.get("publisher");


                if (StringUtils.isNotBlank(title)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern(title)));
                }

                if (StringUtils.isNotBlank(illustrator)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("illustrator")), likePattern(illustrator)));
                }

                if (StringUtils.isNotBlank(author)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), likePattern(author)));
                }

                if (StringUtils.isNotBlank(genre)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("genre")), likePattern(genre)));
                }

                if (StringUtils.isNotBlank(publisher)) {
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("publisher")), likePattern(publisher)));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }


    private static String likePattern(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}
