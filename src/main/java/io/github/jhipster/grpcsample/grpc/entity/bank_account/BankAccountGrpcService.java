package io.github.jhipster.grpcsample.grpc.entity.bank_account;


import io.github.jhipster.grpcsample.grpc.AuthenticationInterceptor;
import io.github.jhipster.grpcsample.service.BankAccountService;
import io.github.jhipster.grpcsample.service.dto.BankAccountDTO;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * gRPC service providing CRUD methods for entity BankAccount.
 */
@GRpcService(interceptors = {AuthenticationInterceptor.class})
public class BankAccountGrpcService extends BankAccountServiceGrpc.BankAccountServiceImplBase{

    private final BankAccountService bankAccountService;

    private final BankAccountProtoMapper bankAccountProtoMapper;

    public BankAccountGrpcService(BankAccountService bankAccountService, BankAccountProtoMapper bankAccountProtoMapper) {
        this.bankAccountService = bankAccountService;
        this.bankAccountProtoMapper = bankAccountProtoMapper;
    }

    @Override
    public void createBankAccount(BankAccountProto request, StreamObserver<BankAccountProto> responseObserver) {
        if( request.getIdOneofCase() == BankAccountProto.IdOneofCase.ID) {
            responseObserver.onError(Status.ALREADY_EXISTS.asException());
            responseObserver.onCompleted();
        } else {
            updateBankAccount(request, responseObserver);
        }

    }

    @Override
    public void updateBankAccount(BankAccountProto request, StreamObserver<BankAccountProto> responseObserver) {
        BankAccountDTO bankAccountDTO = bankAccountProtoMapper.bankAccountProtoToBankAccountDTO(request);
        bankAccountDTO = bankAccountService.save(bankAccountDTO);
        BankAccountProto result = bankAccountProtoMapper.bankAccountDTOToBankAccountProto(bankAccountDTO);
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllBankAccounts(Empty request, StreamObserver<BankAccountProto> responseObserver) {
        bankAccountService.findAll()
            .forEach(bankAccount -> responseObserver.onNext(bankAccountProtoMapper.bankAccountDTOToBankAccountProto(bankAccount)));
        responseObserver.onCompleted();
    }

    @Override
    public void getBankAccount(Int64Value request, StreamObserver<BankAccountProto> responseObserver) {
        BankAccountDTO bankAccountDTO = bankAccountService.findOne(request.getValue());
        if( bankAccountDTO != null) {
            responseObserver.onNext(bankAccountProtoMapper.bankAccountDTOToBankAccountProto(bankAccountDTO));
        } else {
            responseObserver.onError(Status.NOT_FOUND.asException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBankAccount(Int64Value request, StreamObserver<Empty> responseObserver) {
        bankAccountService.delete(request.getValue());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
