package cn.itcast.user.mapper;

import cn.itcast.user.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据传入的id查询用户信息
     * @param id
     * @return
     */
    @Select("select * from tb_user where id = #{id}")
    User findById(@Param("id") Long id);
}