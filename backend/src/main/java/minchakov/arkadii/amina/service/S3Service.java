package minchakov.arkadii.amina.service;

import io.awspring.cloud.s3.S3Template;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import minchakov.arkadii.amina.dto.GetSignedPutUrlsOutDto;
import minchakov.arkadii.amina.exception.BadRequestException;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.S3Object;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.repository.S3ObjectRepository;
import minchakov.arkadii.amina.repository.UserChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class S3Service {

    private final UserRepository userRepository;
    private final S3ObjectRepository s3ObjectRepository;
    private final UserChatRepository userChatRepository;
    private final S3Template internalS3Template;
    private final S3Template externalS3Template;
    @Value("${s3-bucket}")
    private String S3_BUCKET;

    public S3Service(
        UserRepository userRepository, S3ObjectRepository s3ObjectRepository,
        UserChatRepository userChatRepository,
        @Qualifier("internalS3Template") S3Template internalS3Template,
        @Qualifier("externalS3Template") S3Template externalS3Template
    ) {
        this.userRepository = userRepository;
        this.s3ObjectRepository = s3ObjectRepository;
        this.userChatRepository = userChatRepository;
        this.internalS3Template = internalS3Template;
        this.externalS3Template = externalS3Template;
    }

    public void deleteObject(int key) {
        internalS3Template.deleteObject(S3_BUCKET, String.valueOf(key));
    }

    public List<GetSignedPutUrlsOutDto> getSignedPutUrls(
        @Valid @NotEmpty(message = "List of filenames cannot be empty")
        List<@NotBlank(message = "Filename cannot be empty") String> filenames,
        User user
    ) {
        var owner = userRepository
            .findByUsername(user.getUsername())
            .orElseThrow(() -> new InternalServerErrorException(
                "User not found from principal while generating upload urls"));
        var result = new ArrayList<GetSignedPutUrlsOutDto>();
        for (var filename : filenames) {
            var objId = s3ObjectRepository.save(new S3Object(owner, filename)).getId();
            var key = String.valueOf(objId);
            var url = externalS3Template.createSignedPutURL(S3_BUCKET, key, Duration.ofMinutes(15));
            result.add(new GetSignedPutUrlsOutDto(url, objId));
        }
        return result;
    }

    public List<URL> getSignedGetUrls(List<Integer> objectKeys, User user) {
        var currentUser = userRepository
            .findByUsername(user.getUsername())
            .orElseThrow(() -> new InternalServerErrorException(
                "User not found from principal while generating download urls"));
        if (
            !objectKeys
                .stream()
                .map(
                    key -> s3ObjectRepository.findById(key).orElse(null)
                )
                .allMatch(
                    obj ->
                        obj != null &&
                        userChatRepository.existsByUserAndChat(currentUser, obj.getMessage().getChat()) &&
                        internalS3Template.objectExists(S3_BUCKET, String.valueOf(obj.getId()))
                )
        ) {
            throw new BadRequestException("Given invalid key set");
        }
        return objectKeys
            .stream()
            .map(key -> externalS3Template.createSignedGetURL(
                S3_BUCKET,
                String.valueOf(key),
                Duration.ofMinutes(15)
            ))
            .toList();
    }
}
