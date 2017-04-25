package io.github.jhipster.grpcsample.grpc.entity.bank_account;


import io.github.jhipster.grpcsample.domain.enumeration.BankAccountType;
import io.github.jhipster.grpcsample.grpc.ProtobufMappers;
import io.github.jhipster.grpcsample.service.dto.BankAccountDTO;


import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = {ProtobufMappers.class})
public interface BankAccountProtoMapper {


    BankAccountDTO bankAccountProtoToBankAccountDTO(BankAccountProto bankAccountProto);

    @AfterMapping
    // Set back null fields : necessary until https://github.com/google/protobuf/issues/2984 is fixed
    default void bankAccountProtoToBankAccountDTO(BankAccountProto bankAccountProto, @MappingTarget BankAccountDTO bankAccountDTO) {
        if ( bankAccountProto == null ) {
            return;
        }

        if(bankAccountProto.getIdOneofCase() != BankAccountProto.IdOneofCase.ID) {
            bankAccountDTO.setId(null);
        }
        if(bankAccountProto.getBankNumberOneofCase() != BankAccountProto.BankNumberOneofCase.BANK_NUMBER) {
            bankAccountDTO.setBankNumber(null);
        }
        if(bankAccountProto.getAgencyNumberOneofCase() != BankAccountProto.AgencyNumberOneofCase.AGENCY_NUMBER) {
            bankAccountDTO.setAgencyNumber(null);
        }
        if(bankAccountProto.getLastOperationDurationOneofCase() != BankAccountProto.LastOperationDurationOneofCase.LAST_OPERATION_DURATION) {
            bankAccountDTO.setLastOperationDuration(null);
        }
        if(bankAccountProto.getMeanOperationDurationOneofCase() != BankAccountProto.MeanOperationDurationOneofCase.MEAN_OPERATION_DURATION) {
            bankAccountDTO.setMeanOperationDuration(null);
        }
        if(bankAccountProto.getActiveOneofCase() != BankAccountProto.ActiveOneofCase.ACTIVE) {
            bankAccountDTO.setActive(null);
        }
        if(bankAccountProto.getAccountTypeOneofCase() != BankAccountProto.AccountTypeOneofCase.ACCOUNT_TYPE) {
            bankAccountDTO.setAccountType(null);
        }
        if(bankAccountProto.getAttachmentOneofCase() != BankAccountProto.AttachmentOneofCase.ATTACHMENT) {
            bankAccountDTO.setAttachment(null);
        }
        if(bankAccountProto.getAttachmentContentTypeOneofCase() != BankAccountProto.AttachmentContentTypeOneofCase.ATTACHMENT_CONTENT_TYPE) {
            bankAccountDTO.setAttachmentContentType(null);
        }
        if(bankAccountProto.getDescriptionOneofCase() != BankAccountProto.DescriptionOneofCase.DESCRIPTION) {
            bankAccountDTO.setDescription(null);
        }
        if(bankAccountProto.getUserIdOneofCase() != BankAccountProto.UserIdOneofCase.USER_ID) {
            bankAccountDTO.setUserId(null);
        }
    }

    default BankAccountProto.Builder createBankAccountProto () {
        return BankAccountProto.newBuilder();
    }


    BankAccountProto.Builder bankAccountDTOToBankAccountProtoBuilder(BankAccountDTO bankAccountDTO);

    default BankAccountProto bankAccountDTOToBankAccountProto(BankAccountDTO bankAccountDTO) {
        if (bankAccountDTO == null) {
            return null;
        }
        return bankAccountDTOToBankAccountProtoBuilder(bankAccountDTO).build();
    }

    BankAccountTypeProto convertBankAccountTypeToBankAccountTypeProto(BankAccountType enumValue);

    @ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.NULL)
    BankAccountType convertBankAccountTypeProtoToBankAccountType(BankAccountTypeProto enumValue);


}
