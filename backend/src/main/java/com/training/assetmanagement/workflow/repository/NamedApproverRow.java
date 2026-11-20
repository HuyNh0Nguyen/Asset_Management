package com.training.assetmanagement.workflow.repository;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "NamedApproverRow", description = "Named approver row")
public record NamedApproverRow(UUID ruleId, String workflowCode, String workflowName, String stageName) {
}
