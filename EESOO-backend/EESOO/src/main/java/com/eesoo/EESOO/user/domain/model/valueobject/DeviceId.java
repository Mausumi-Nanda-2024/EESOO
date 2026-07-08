package com.eesoo.EESOO.user.domain.model.valueobject;

public final class DeviceId {

    private final String value;

    private DeviceId(String value) {
        this.value = value;
    }

    public static DeviceId of(String value){

        if(value == null || value.isEmpty()){
            throw new IllegalArgumentException("DeviceId cannot be null or empty");
        }

         return new DeviceId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceId)) return false;
        DeviceId deviceId = (DeviceId) o;
        return value.equals(deviceId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
    
    
}
