package com.elmar.corebankapp.mappers;

import com.elmar.corebankapp.models.Transaction;
import com.elmar.corebankapp.models.enums.Currency;
import com.elmar.corebankapp.models.enums.TransactionDirection;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TransactionMapper {

    @Insert("INSERT INTO transactions (account_id, amount, currency, direction, description) " +
            "VALUES (#{accountId}, #{amount}, #{currency}, #{direction}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertTransaction(Transaction transaction);

    @Select("SELECT * FROM transactions WHERE account_id = #{accountId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "currency", column = "currency", javaType = Currency.class),
            @Result(property = "direction", column = "direction", javaType = TransactionDirection.class),
            @Result(property = "description", column = "description"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<Transaction> getByAccountId(Long accountId);
}
