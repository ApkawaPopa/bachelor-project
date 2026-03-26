package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import minchakov.arkadii.amina.dto.GetSignedPutUrlsOutDto;
import minchakov.arkadii.amina.dto.RestResponse;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.service.S3Service;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/api/v1/s3")
public class S3Controller {

    private final S3Service s3Service;

    public S3Controller(
        S3Service s3Service
    ) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload-urls")
    public RestResponse<List<GetSignedPutUrlsOutDto>> getSignedPutUrls(
        @RequestBody
        @Valid
        @NotEmpty(message = "List of filenames cannot be empty")
        @Size(min = 1, max = 15, message = "You cannot attach more than 15 files")
        List<@NotBlank(message = "Filename cannot be empty") String> filenames,
        @AuthenticationPrincipal User user
    ) {
        return RestResponse.success(s3Service.getSignedPutUrls(filenames, user));
    }

    @PostMapping("/download-urls")
    public RestResponse<List<URL>> getSignedGetUrls(
        @RequestBody
        @Valid
        @NotEmpty(message = "List of keys cannot be empty")
        @Size(min = 1, max = 15, message = "You cannot get more than 15 urls at once")
        List<Integer> objectKeys,
        @AuthenticationPrincipal User user
    ) {
        return RestResponse.success(s3Service.getSignedGetUrls(objectKeys, user));
    }
}
