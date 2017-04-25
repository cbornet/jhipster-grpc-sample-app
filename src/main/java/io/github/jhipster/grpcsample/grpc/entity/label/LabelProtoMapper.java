package io.github.jhipster.grpcsample.grpc.entity.label;


import io.github.jhipster.grpcsample.domain.Label;
import io.github.jhipster.grpcsample.grpc.ProtobufMappers;


import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = {ProtobufMappers.class})
public interface LabelProtoMapper {


    @Mapping(target = "operations", ignore = true)
    Label labelProtoToLabel(LabelProto labelProto);

    @AfterMapping
    // Set back null fields : necessary until https://github.com/google/protobuf/issues/2984 is fixed
    default void labelProtoToLabel(LabelProto labelProto, @MappingTarget Label label) {
        if ( labelProto == null ) {
            return;
        }

        if(labelProto.getIdOneofCase() != LabelProto.IdOneofCase.ID) {
            label.setId(null);
        }
    }

    default LabelProto.Builder createLabelProto () {
        return LabelProto.newBuilder();
    }


    LabelProto.Builder labelToLabelProtoBuilder(Label label);

    default LabelProto labelToLabelProto(Label label) {
        if (label == null) {
            return null;
        }
        return labelToLabelProtoBuilder(label).build();
    }


    default Label labelFromId(Long id) {
        if (id == null) {
            return null;
        }
        Label label = new Label();
        label.setId(id);
        return label;
    }
}
