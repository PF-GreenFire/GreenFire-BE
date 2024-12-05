package sisosolsol.greenfire.user.model.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sisosolsol.greenfire.user.model.dto.UserDTO;
import sisosolsol.greenfire.user.model.dto.UserListResponseDTO;
import sisosolsol.greenfire.user.model.dto.UserUpdateDTO;

import java.util.List;
import java.util.UUID;

@Mapper
public interface UserMapper {

    // 일반 회원
    UserDTO findByUserCode(UUID userCode);

    void updateUserProfile(@Param("userCode") UUID userCode, @Param("userDTO") UserUpdateDTO user);

    // 관리자
    List<UserListResponseDTO> findAll();

}