package com.example.back.enums.converter;

import com.example.back.enums.AnswerVoteType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 回答投票类型转换器
 */
@Converter(autoApply = true)
public class AnswerVoteTypeConverter implements AttributeConverter<AnswerVoteType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(AnswerVoteType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public AnswerVoteType convertToEntityAttribute(Integer dbData) {
        return AnswerVoteType.fromCode(dbData);
    }
}

