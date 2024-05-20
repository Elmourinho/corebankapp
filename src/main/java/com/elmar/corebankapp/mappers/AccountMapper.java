package com.elmar.corebankapp.mappers;

import com.elmar.corebankapp.models.Account;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AccountMapper {

    @Insert("INSERT INTO accounts (customer_id, country) VALUES (#{customerId}, #{country})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Account account);

    @Select("SELECT * FROM accounts WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "customerId", column = "customer_id"),
            @Result(property = "country", column = "country"),
            @Result(property = "balances", column = "id", javaType = List.class,
                    many = @Many(select = "com.elmar.corebankapp.mappers.BalanceMapper.getByAccountId"))
    })
    Optional<Account> getById(Long id);
}
