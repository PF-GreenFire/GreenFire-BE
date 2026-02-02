package sisosolsol.greenfire.location.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisosolsol.greenfire.location.model.dao.LocationMapper;
import sisosolsol.greenfire.location.model.dto.LocationDTO;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationMapper locationMapper;

    //초록불 장소 신청 등록시 지역 정보 등록
    public int registerLocation(LocationDTO location) {
        return locationMapper.registLocation(location);
    }
}
