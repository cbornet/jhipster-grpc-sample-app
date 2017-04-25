package io.github.jhipster.grpcsample.grpc.entity.label;


import io.github.jhipster.grpcsample.domain.Label;
import io.github.jhipster.grpcsample.grpc.AuthenticationInterceptor;
import io.github.jhipster.grpcsample.grpc.PageRequest;
import io.github.jhipster.grpcsample.grpc.ProtobufMappers;
import io.github.jhipster.grpcsample.service.LabelService;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * gRPC service providing CRUD methods for entity Label.
 */
@GRpcService(interceptors = {AuthenticationInterceptor.class})
public class LabelGrpcService extends LabelServiceGrpc.LabelServiceImplBase{

    private final LabelService labelService;

    private final LabelProtoMapper labelProtoMapper;

    public LabelGrpcService(LabelService labelService, LabelProtoMapper labelProtoMapper) {
        this.labelService = labelService;
        this.labelProtoMapper = labelProtoMapper;
    }

    @Override
    public void createLabel(LabelProto request, StreamObserver<LabelProto> responseObserver) {
        if( request.getIdOneofCase() == LabelProto.IdOneofCase.ID) {
            responseObserver.onError(Status.ALREADY_EXISTS.asException());
            responseObserver.onCompleted();
        } else {
            updateLabel(request, responseObserver);
        }

    }

    @Override
    public void updateLabel(LabelProto request, StreamObserver<LabelProto> responseObserver) {
        Label label = labelProtoMapper.labelProtoToLabel(request);
        label = labelService.save(label);
        LabelProto result = labelProtoMapper.labelToLabelProto(label);
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllLabels(PageRequest request, StreamObserver<LabelProto> responseObserver) {
        labelService.findAll(ProtobufMappers.pageRequestProtoToPageRequest(request))
            .forEach(label -> responseObserver.onNext(labelProtoMapper.labelToLabelProto(label)));
        responseObserver.onCompleted();
    }

    @Override
    public void getLabel(Int64Value request, StreamObserver<LabelProto> responseObserver) {
        Label label = labelService.findOne(request.getValue());
        if( label != null) {
            responseObserver.onNext(labelProtoMapper.labelToLabelProto(label));
        } else {
            responseObserver.onError(Status.NOT_FOUND.asException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deleteLabel(Int64Value request, StreamObserver<Empty> responseObserver) {
        labelService.delete(request.getValue());
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
