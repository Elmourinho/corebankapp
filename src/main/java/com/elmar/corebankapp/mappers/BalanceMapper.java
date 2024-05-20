package com.elmar.corebankapp.mappers;

import com.elmar.corebankapp.models.Balance;
import com.elmar.corebankapp.models.enums.Currency;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BalanceMapper {

    @Insert("INSERT INTO balances (account_id, currency, amount) VALUES (#{accountId}, #{currency}, #{amount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Balance balance);

    @Select("SELECT * FROM balances WHERE account_id = #{accountId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "accountId", column = "account_id"),
            @Result(property = "currency", column = "currency", javaType = Currency.class),
            @Result(property = "amount", column = "amount")
    })
    List<Balance> getByAccountId(Long accountId);

    @Update("UPDATE balances SET amount = #{amount} WHERE id = #{id}")
    void updateBalance(Balance balance);
}
