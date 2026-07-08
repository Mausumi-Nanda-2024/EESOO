package com.eesoo.EESOO.user.application.device.backend_outcome;

import java.util.Objects;
import java.util.Optional;

public final class DeviceLinkOutcome {

    private final DeviceLinkStatus status;
    private final DeviceLinkFailureReason failureReasonOrNull;

    private DeviceLinkOutcome(
        DeviceLinkStatus status,
        DeviceLinkFailureReason failureReasonOrNull
    ){
        this.status = Objects.requireNonNull(status, "status cannot be null");

        if(status == DeviceLinkStatus.LINKED && failureReasonOrNull != null) {
            throw new IllegalArgumentException("failureReason must be null when status is LINKED");
        }

        if(status == DeviceLinkStatus.NOT_LINKED && failureReasonOrNull == null) {
            throw new IllegalArgumentException("failureReason cannot be null when status is NOT_LINKED");
        }

        this.failureReasonOrNull = failureReasonOrNull;
    }

    public static DeviceLinkOutcome linked(){
        return new DeviceLinkOutcome(DeviceLinkStatus.LINKED, null);
    }

    public static DeviceLinkOutcome notLinked(DeviceLinkFailureReason failureReason){
        return new DeviceLinkOutcome(DeviceLinkStatus.NOT_LINKED, Objects.requireNonNull(failureReason , "failure cannot be null") );
    }

    public DeviceLinkStatus getStatus(){
        return status;

    }

    public Optional<DeviceLinkFailureReason> getFailureReason(){
        return Optional.ofNullable(failureReasonOrNull);
    }

    public boolean isLinked(){
        return status == DeviceLinkStatus.LINKED;
    }
    
    public boolean isNotLinked(){
        return status == DeviceLinkStatus.NOT_LINKED;
    }
}
