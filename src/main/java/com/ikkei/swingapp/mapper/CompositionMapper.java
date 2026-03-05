package com.ikkei.swingapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.ikkei.swingapp.domain.CompositionRow;

@Mapper
public interface CompositionMapper {

    @Update("""
            CREATE TABLE IF NOT EXISTS composition (
                parent_part_no VARCHAR(100) NOT NULL,
                child_part_no VARCHAR(100) NOT NULL,
                level INTEGER NOT NULL,
                PRIMARY KEY (parent_part_no, child_part_no)
            )
            """)
    void createTableIfNotExists();

    @Select("""
            SELECT parent_part_no, child_part_no, level
            FROM composition
            ORDER BY parent_part_no, child_part_no
            """)
    List<CompositionRow> findAll();

    @Delete("DELETE FROM composition")
    void deleteAll();

    @Insert("""
            <script>
            INSERT INTO composition (parent_part_no, child_part_no, level)
            VALUES
            <foreach collection='rows' item='row' separator=','>
                (#{row.parentPartNo}, #{row.childPartNo}, #{row.level})
            </foreach>
            </script>
            """)
    void insertAll(@Param("rows") List<CompositionRow> rows);
}
