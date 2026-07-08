package com.eesoo.EESOO.user.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.eesoo.EESOO.user.domain.model.entity.UserDeviceLink;
import com.eesoo.EESOO.user.domain.model.enums.LinkStatus;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserDeviceLinkId;
import com.eesoo.EESOO.user.domain.model.valueobject.UserId;
import com.eesoo.EESOO.user.domain.repository.UserDeviceLinkRepository;
import com.eesoo.EESOO.user.infrastructure.persistence.mapper.UserDeviceLinkMapper;

@Repository
public class JpaUserDeviceLinkRepository implements UserDeviceLinkRepository {

    private final SpringDataUserDeviceLinkRepository springRepo;
    private final UserDeviceLinkMapper mapper;

    public JpaUserDeviceLinkRepository(
            SpringDataUserDeviceLinkRepository springRepo,
            UserDeviceLinkMapper mapper
    ) {
        this.springRepo = springRepo;
        this.mapper = mapper;
    }

    @Override
    public boolean existsById(UserDeviceLinkId id) {
        return springRepo.existsById(id.getValue());
    }

    @Override
    public Optional<UserDeviceLink> findById(UserDeviceLinkId id) {
        return springRepo.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<UserDeviceLink> findActiveByDeviceInstallId(DeviceInstallId deviceInstallId) {
        return springRepo.findByDeviceInstallIdAndStatus(deviceInstallId.getValue(), LinkStatus.ACTIVE.name())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<UserDeviceLink> findActiveByUserId(UserId userId) {
        return springRepo.findByUserIdAndStatus(userId.getValue(), LinkStatus.ACTIVE.name())
                .map(mapper::toDomain);
    }

    @Override
    public void save(UserDeviceLink userDeviceLink) {
        springRepo.save(mapper.toEntity(userDeviceLink));
    }
}
