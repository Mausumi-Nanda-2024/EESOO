package com.eesoo.EESOO.user.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.eesoo.EESOO.user.domain.model.entity.DeviceInstall;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceId;
import com.eesoo.EESOO.user.domain.model.valueobject.DeviceInstallId;
import com.eesoo.EESOO.user.domain.model.valueobject.InstallId;
import com.eesoo.EESOO.user.domain.repository.DeviceInstallRepository;
import com.eesoo.EESOO.user.infrastructure.persistence.mapper.DeviceInstallMapper;

@Repository
public class JpaDeviceInstallRepository implements DeviceInstallRepository {

    private final SpringDataDeviceInstallRepository springRepo;
    private final DeviceInstallMapper mapper;

    public JpaDeviceInstallRepository(
            SpringDataDeviceInstallRepository springRepo,
            DeviceInstallMapper mapper
    ) {
        this.springRepo = springRepo;
        this.mapper = mapper;
    }

    @Override
    public boolean existsById(DeviceInstallId id) {
        return springRepo.existsById(id.getValue());
    }

    @Override
    public boolean existsByInstallId(InstallId installId) {
        return springRepo.existsByInstallId(installId.getValue());
    }

    @Override
    public boolean existsByDeviceId(DeviceId deviceId) {
        return springRepo.existsByDeviceId(deviceId.getValue());
    }

    @Override
    public Optional<DeviceInstall> findById(DeviceInstallId id) {
        return springRepo.findById(id.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<DeviceInstall> findByInstallId(InstallId installId) {
        return springRepo.findByInstallId(installId.getValue()).map(mapper::toDomain);
    }

    @Override
    public Optional<DeviceInstall> findByDeviceId(DeviceId deviceId) {
        return springRepo.findByDeviceId(deviceId.getValue()).map(mapper::toDomain);
    }

    @Override
    public void save(DeviceInstall deviceInstall) {
        springRepo.save(mapper.toEntity(deviceInstall));
    }
}
