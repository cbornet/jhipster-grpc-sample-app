package io.github.jhipster.grpcsample.grpc.entity.label;

import io.github.jhipster.grpcsample.JhipsterGrpcSampleApplicationApp;

import io.github.jhipster.grpcsample.domain.Label;
import io.github.jhipster.grpcsample.grpc.PageRequest;
import io.github.jhipster.grpcsample.repository.LabelRepository;
import io.github.jhipster.grpcsample.service.LabelService;
import io.github.jhipster.grpcsample.web.rest.TestUtil;

import com.google.protobuf.Int64Value;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Iterator;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

/**
 * Test class for the LabelGrpcService gRPC service.
 *
 * @see LabelGrpcService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JhipsterGrpcSampleApplicationApp.class)
public class LabelGrpcServiceIntTest {

    private static final String DEFAULT_LABEL = "AAAAAAAAAA";
    private static final String UPDATED_LABEL = "BBBBBBBBBB";

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelProtoMapper labelProtoMapper;

    @Autowired
    private LabelService labelService;

    @Autowired
    private EntityManager em;

    private Server mockServer;

    private LabelServiceGrpc.LabelServiceBlockingStub stub;

    private Label label;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        LabelGrpcService labelGrpcService = new LabelGrpcService(labelService, labelProtoMapper);
        String uniqueServerName = "Mock server for " + LabelGrpcService.class;
        mockServer = InProcessServerBuilder
            .forName(uniqueServerName).directExecutor().addService(labelGrpcService).build().start();
        InProcessChannelBuilder channelBuilder =
            InProcessChannelBuilder.forName(uniqueServerName).directExecutor();
        stub = LabelServiceGrpc.newBlockingStub(channelBuilder.build());
    }

    @After
    public void tearDown() {
        mockServer.shutdownNow();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Label createEntity(EntityManager em) {
        Label label = new Label()
            .label(DEFAULT_LABEL);
        return label;
    }

    @Before
    public void initTest() {
        label = createEntity(em);
    }

    @Test
    @Transactional
    public void createLabel() throws Exception {
        int databaseSizeBeforeCreate = labelRepository.findAll().size();

        // Create the Label
        LabelProto labelProto = labelProtoMapper.labelToLabelProto(label);

        stub.createLabel(labelProto);

        // Validate the Label in the database
        List<Label> labelList = labelRepository.findAll();
        assertThat(labelList).hasSize(databaseSizeBeforeCreate + 1);
        Label testLabel = labelList.get(labelList.size() - 1);
        assertThat(testLabel.getLabel()).isEqualTo(DEFAULT_LABEL);
    }

    @Test
    @Transactional
    public void createLabelWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = labelRepository.findAll().size();

        // Create the Label with an existing ID
        label.setId(1L);
        LabelProto labelProto = labelProtoMapper.labelToLabelProto(label);

        try {
            stub.createLabel(labelProto);
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.ALREADY_EXISTS);
        }

        // Validate the Alice in the database
        List<Label> labelList = labelRepository.findAll();
        assertThat(labelList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllLabels() throws Exception {
        // Initialize the database
        Label savedLabel = labelRepository.saveAndFlush(label);

        // Get all the labels
        Label foundLabel = null;
        Iterator<LabelProto> it = stub.getAllLabels(PageRequest.getDefaultInstance());
        while(it.hasNext()) {
            LabelProto labelProto = it.next();
            if (savedLabel.getId().equals(labelProto.getId())) {
                foundLabel = labelProtoMapper.labelProtoToLabel(labelProto);
                break;
            }
        }

        assertThat(foundLabel).isNotNull();
        assertThat(foundLabel.getLabel()).isEqualTo(DEFAULT_LABEL);
    }

    @Test
    @Transactional
    public void getLabel() throws Exception {
        // Initialize the database
        labelRepository.saveAndFlush(label);

        // Get the label
        LabelProto labelProto = stub.getLabel(Int64Value.newBuilder().setValue(label.getId()).build());
        Label foundLabel = labelProtoMapper.labelProtoToLabel(labelProto);

        assertThat(foundLabel.getLabel()).isEqualTo(DEFAULT_LABEL);
    }

    @Test
    @Transactional
    public void getNonExistingLabel() throws Exception {
        try {
            // Get the label
            stub.getLabel(Int64Value.newBuilder().setValue(Long.MAX_VALUE).build());
            failBecauseExceptionWasNotThrown(StatusRuntimeException.class);
        } catch (StatusRuntimeException e) {
            assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        }
    }

    @Test
    @Transactional
    public void updateLabel() throws Exception {
        // Initialize the database
        labelService.save(label);

        int databaseSizeBeforeUpdate = labelRepository.findAll().size();

        // Update the label
        Label updatedLabel = labelRepository.findOne(label.getId());
        updatedLabel
            .label(UPDATED_LABEL);
        LabelProto labelProto = labelProtoMapper.labelToLabelProto(updatedLabel);

        stub.updateLabel(labelProto);

        // Validate the Label in the database
        List<Label> labelList = labelRepository.findAll();
        assertThat(labelList).hasSize(databaseSizeBeforeUpdate);
        Label testLabel = labelList.get(labelList.size() - 1);
        assertThat(testLabel.getLabel()).isEqualTo(UPDATED_LABEL);
    }

    @Test
    @Transactional
    public void updateNonExistingLabel() throws Exception {
        int databaseSizeBeforeUpdate = labelRepository.findAll().size();

        // Create the Label
        LabelProto labelProto = labelProtoMapper.labelToLabelProto(label);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        stub.updateLabel(labelProto);

        // Validate the Label in the database
        List<Label> labelList = labelRepository.findAll();
        assertThat(labelList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteLabel() throws Exception {
        // Initialize the database
        labelService.save(label);

        int databaseSizeBeforeDelete = labelRepository.findAll().size();

        // Get the label
        stub.deleteLabel(Int64Value.newBuilder().setValue(label.getId()).build());

        // Validate the database is empty
        List<Label> labelList = labelRepository.findAll();
        assertThat(labelList).hasSize(databaseSizeBeforeDelete - 1);
    }

}
