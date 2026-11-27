package com.training.assetmanagement.emailservice.delivery;

// Tiền tố của last_error - VD "TEMPLATE: ...", "SMTP: ..."
public enum EmailFailureKind {

    TEMPLATE,
    SMTP,
    BROKER;

    public String describe(String detail) {
        return name() + ": " + detail;
    }
}
