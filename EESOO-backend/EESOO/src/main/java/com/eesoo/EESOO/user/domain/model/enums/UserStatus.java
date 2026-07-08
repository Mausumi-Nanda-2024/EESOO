package com.eesoo.EESOO.user.domain.model.enums;

public enum UserStatus {

    PENDING_VERIFICATION,
    ACTIVE,
    DELETED;

    // Domain Logic: Can Thus user access the dashboard?

    public boolean canLogin(){
        return this == ACTIVE;
    }

    // Domain Logic: has this user been deleted?
    public boolean isDeleted() {
        return this == DELETED;
    }

    //cleaner output when printing / logging
    @Override
    public String toString() {
       //Capitalize the first letter , rest lowercase
      String lowercase = this.name().toLowerCase();
      return Character.toUpperCase(lowercase.charAt(0)) + lowercase.substring(1);
    }
}


