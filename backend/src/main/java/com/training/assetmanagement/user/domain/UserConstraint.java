package com.training.assetmanagement.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserConstraint {

    UK_USERS_USERNAME("uk_users_username"),
    UK_USERS_EMAIL("uk_users_email"),
    FK_USERS_RANK("fk_users_rank");

    private final String constraintName;
}
