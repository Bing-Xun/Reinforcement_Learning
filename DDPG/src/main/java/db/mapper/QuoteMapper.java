package db.mapper;

import db.entity.QuoteEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuoteMapper {

    @Select({
            "<script>",
            "SELECT * FROM ${tableName} ",
            "ORDER BY open_time ASC ",
            "LIMIT #{limit}",
            "</script>"
    })
    List<QuoteEntity> getQuotes(@Param("tableName") String tableName, @Param("limit") int limit);
}

