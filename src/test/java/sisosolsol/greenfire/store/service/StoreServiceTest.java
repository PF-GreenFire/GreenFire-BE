package sisosolsol.greenfire.store.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sisosolsol.greenfire.common.enums.store.StoreStatus;
import sisosolsol.greenfire.image.service.ImageService;
import sisosolsol.greenfire.location.model.dao.LocationMapper;
import sisosolsol.greenfire.location.service.LocationService;
import sisosolsol.greenfire.notification.model.NotificationType;
import sisosolsol.greenfire.notification.service.NotificationService;
import sisosolsol.greenfire.spark.service.SparkService;
import sisosolsol.greenfire.store.model.dao.StoreMapper;
import sisosolsol.greenfire.store.model.dto.StoreUpdateStatusDTO;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreMapper storeMapper;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private LocationService locationService;

    @Mock
    private ImageService imageService;

    @Mock
    private SparkService sparkService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private StoreService storeService;

    @Test
    @DisplayName("updateStoreStatus(APPROVE): 상태 업데이트 후 spark 적립과 STORE_APPROVED 알림을 보낸다")
    void updateStoreStatus_approve_awardsSparkAndNotifies() {
        // Arrange
        int storeCode = 100;
        UUID applicant = UUID.randomUUID();
        StoreUpdateStatusDTO dto = mock(StoreUpdateStatusDTO.class);
        when(dto.getStatus()).thenReturn(StoreStatus.APPROVE);
        when(storeMapper.findApplicantUserCode(storeCode)).thenReturn(applicant);

        // Act
        storeService.updateStoreStatus(storeCode, dto);

        // Assert
        verify(storeMapper).updateStoreStatus(storeCode, dto);
        verify(sparkService).award(applicant, 50, "STORE_APPROVED", "STORE", storeCode);
        verify(notificationService).notify(eq(applicant),
                eq(NotificationType.STORE_APPROVED),
                eq((UUID) null), eq("STORE"), eq(String.valueOf(storeCode)));
    }

    @Test
    @DisplayName("updateStoreStatus(REJECT): spark 적립 없이 STORE_REJECTED 알림만 보낸다")
    void updateStoreStatus_reject_onlyNotifies() {
        // Arrange
        int storeCode = 101;
        UUID applicant = UUID.randomUUID();
        StoreUpdateStatusDTO dto = mock(StoreUpdateStatusDTO.class);
        when(dto.getStatus()).thenReturn(StoreStatus.REJECT);
        when(storeMapper.findApplicantUserCode(storeCode)).thenReturn(applicant);

        // Act
        storeService.updateStoreStatus(storeCode, dto);

        // Assert
        verify(storeMapper).updateStoreStatus(storeCode, dto);
        verifyNoInteractions(sparkService);
        verify(notificationService).notify(eq(applicant),
                eq(NotificationType.STORE_REJECTED),
                eq((UUID) null), eq("STORE"), eq(String.valueOf(storeCode)));
    }

    @Test
    @DisplayName("updateStoreStatus: 신청자 정보가 없으면 알림과 spark 둘 다 호출되지 않는다")
    void updateStoreStatus_skipsRewards_whenApplicantMissing() {
        // Arrange
        int storeCode = 102;
        StoreUpdateStatusDTO dto = mock(StoreUpdateStatusDTO.class);
        when(storeMapper.findApplicantUserCode(storeCode)).thenReturn(null);

        // Act
        storeService.updateStoreStatus(storeCode, dto);

        // Assert
        verify(storeMapper).updateStoreStatus(storeCode, dto);
        verifyNoInteractions(sparkService);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("updateStoreStatus(WAITING): APPROVE/REJECT 외 상태에서는 spark/알림 발생하지 않는다")
    void updateStoreStatus_otherStatus_noRewards() {
        // Arrange
        int storeCode = 103;
        UUID applicant = UUID.randomUUID();
        StoreUpdateStatusDTO dto = mock(StoreUpdateStatusDTO.class);
        when(dto.getStatus()).thenReturn(StoreStatus.WAITING);
        when(storeMapper.findApplicantUserCode(storeCode)).thenReturn(applicant);

        // Act
        storeService.updateStoreStatus(storeCode, dto);

        // Assert
        verify(storeMapper).updateStoreStatus(storeCode, dto);
        verifyNoInteractions(sparkService);
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("storeLike: storeMapper.storeLike를 정확히 호출한다")
    void storeLike_delegatesToMapper() {
        // Arrange
        UUID userCode = UUID.randomUUID();
        int storeCode = 200;

        // Act
        storeService.storeLike(userCode, storeCode);

        // Assert
        verify(storeMapper).storeLike(userCode, storeCode);
    }

    @Test
    @DisplayName("deleteStoreLike: storeMapper.deleteStoreLike를 정확히 호출한다")
    void deleteStoreLike_delegatesToMapper() {
        // Arrange
        UUID userCode = UUID.randomUUID();
        int storeCode = 201;

        // Act
        storeService.deleteStoreLike(userCode, storeCode);

        // Assert
        verify(storeMapper).deleteStoreLike(userCode, storeCode);
    }
}
