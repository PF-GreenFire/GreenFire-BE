package sisosolsol.greenfire.user.model.dao;

import org.apache.ibatis.annotations.Mapper;
import sisosolsol.greenfire.user.model.dto.UserDTO;

import java.util.UUID;

@Mapper
public interface UserMapper {

    // 회원 프로필 정보 조회
    UserDTO findByUserCode(UUID userCode);

}
