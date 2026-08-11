package com.eesoo.EESOO.auth.presentation.pinreset.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eesoo.EESOO.auth.application.pinreset.dto.ConfirmPinResetMobileDTO;
import com.eesoo.EESOO.auth.application.pinreset.dto.ConfirmPinResetMobileResultDTO;
import com.eesoo.EESOO.auth.application.pinreset.dto.IssueResetPinDTO;
import com.eesoo.EESOO.auth.application.pinreset.dto.IssueResetPinResultDTO;
import com.eesoo.EESOO.auth.application.pinreset.service.ConfirmPinResetMobileService;
import com.eesoo.EESOO.auth.application.pinreset.service.IssueResetPinService;
import com.eesoo.EESOO.auth.presentation.pinreset.dto.ConfirmPinResetMobileRequestDTO;
import com.eesoo.EESOO.auth.presentation.pinreset.dto.ConfirmPinResetMobileResponseDTO;
import com.eesoo.EESOO.auth.presentation.pinreset.dto.IssueResetPinRequestDTO;
import com.eesoo.EESOO.auth.presentation.pinreset.dto.IssueResetPinResponseDTO;
import com.eesoo.EESOO.shared.Api.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth/pin-reset")
public class PinResetController {

    private final ConfirmPinResetMobileService confirmPinResetMobileService;
    private final IssueResetPinService issueResetPinService;

    public PinResetController(
            ConfirmPinResetMobileService confirmPinResetMobileService,
            IssueResetPinService issueResetPinService
    ) {
        this.confirmPinResetMobileService =
                confirmPinResetMobileService;

        this.issueResetPinService =
                issueResetPinService;
    }

    @PostMapping("/confirm-mobile")
    public ResponseEntity<
            ApiResponse<ConfirmPinResetMobileResponseDTO>
    > confirmMobile(
            @Valid
            @RequestBody
            ConfirmPinResetMobileRequestDTO request
    ) {
        ConfirmPinResetMobileDTO applicationDTO =
                new ConfirmPinResetMobileDTO(
                        request.getPinResetAttemptId(),
                        request.getPhoneNumber(),
                        request.getInstallId()
                );

        ConfirmPinResetMobileResultDTO result =
                confirmPinResetMobileService.confirmMobile(
                        applicationDTO
                );

        ConfirmPinResetMobileResponseDTO response =
                ConfirmPinResetMobileResponseDTO.from(
                        result
                );

        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.noStore()
                )
                .body(
                        ApiResponse.success(
                                "Mobile number confirmed successfully",
                                response
                        )
                );
    }

    @PostMapping("/issue")
    public ResponseEntity<
            ApiResponse<IssueResetPinResponseDTO>
    > issuePin(
            @Valid
            @RequestBody
            IssueResetPinRequestDTO request
    ) {
        IssueResetPinDTO applicationDTO =
                new IssueResetPinDTO(
                        request.getPinResetAttemptId(),
                        request.getInstallId()
                );

        IssueResetPinResultDTO result =
                issueResetPinService.issuePin(
                        applicationDTO
                );

        IssueResetPinResponseDTO response =
                IssueResetPinResponseDTO.from(
                        result
                );

        return ResponseEntity
                .ok()
                .cacheControl(
                        CacheControl.noStore()
                )
                .body(
                        ApiResponse.success(
                                "PIN reset successfully",
                                response
                        )
                );
    }
}
