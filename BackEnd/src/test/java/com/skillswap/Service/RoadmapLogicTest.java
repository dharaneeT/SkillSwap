package com.skillSwap.Service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillSwap.Dto.roadmap.RoadmapDtos.Draft;
import com.skillSwap.Dto.roadmap.RoadmapDtos.GenerateRequest;
import com.skillSwap.Dto.roadmap.RoadmapDtos.RoadmapView;
import com.skillSwap.Entity.LearningRoadmap;
import com.skillSwap.Entity.RoadmapEnums.Proficiency;
import com.skillSwap.Entity.RoadmapMilestone;
import com.skillSwap.Entity.RoadmapTask;
import com.skillSwap.Exception.RoadmapAiException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RoadmapLogicTest {

	private final ObjectMapper json = new ObjectMapper();
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	private GenerateRequest request() {
		GenerateRequest r = new GenerateRequest();
		r.setGoal("Become a backend Java developer");
		r.setCurrentLevel(Proficiency.BEGINNER);
		r.setHoursPerWeek(8);
		return r;
	}

	/** Wraps the "AI answer" the way Gemini does: candidates[0].content.parts[0].text */
	private String geminiEnvelope(String innerJson) throws Exception {
		return json.writeValueAsString(
			Map.of("candidates", List.of(Map.of("content", Map.of("parts", List.of(Map.of("text", innerJson))))))
		);
	}

	private String milestones(int count) {
		StringBuilder sb = new StringBuilder(
			"{\"title\":\"Java Backend\",\"description\":\"d\",\"prerequisites\":[\"PC\"],\"milestones\":["
		);
		for (int i = 1; i <= count; i++) {
			if (i > 1) sb.append(',');
			sb
				.append("{\"title\":\"Step ")
				.append(i)
				.append("\",\"description\":\"d\",\"estimatedHours\":4,")
				.append("\"learningObjectives\":[\"o\"],\"completionCriteria\":\"c\",\"dependsOn\":[],")
				.append(
					"\"tasks\":[{\"title\":\"Task\",\"description\":\"d\",\"type\":\"practice\",\"estimatedMinutes\":60}]}"
				);
		}
		return sb.append("]}").toString();
	}

	private GeminiRoadmapService serviceWith(String apiKey, String aiAnswer) throws Exception {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://test.invalid");
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		if (aiAnswer != null) {
			server
				.expect(requestTo("https://test.invalid/models/test-model:generateContent"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("x-goog-api-key", apiKey))
				.andRespond(withSuccess(geminiEnvelope(aiAnswer), MediaType.APPLICATION_JSON));
		}
		return new GeminiRoadmapService(apiKey, "test-model", "", 10, builder.build(), validator);
	}

	@Test
	void validAiRoadmapIsAcceptedAndCleaned() throws Exception {
		Draft d = serviceWith("key", milestones(4)).generate(1, request());

		assertThat(d.getSource()).isEqualTo("GEMINI");
		assertThat(d.getMilestones()).hasSize(4);
		// "practice" (lower case) was understood as PRACTICE
		assertThat(d.getMilestones().get(0).getTasks().get(0).getType().name()).isEqualTo("PRACTICE");
		assertThat(d.getLearningGoal()).isEqualTo("Become a backend Java developer");
	}

	@Test
	void incompleteAiRoadmapIsRejected() throws Exception {
		assertThatThrownBy(() -> serviceWith("key", milestones(1)).generate(1, request()))
			.isInstanceOfSatisfying(
				RoadmapAiException.class,
				e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY)
			);
	}

	@Test
	void garbageAiAnswerIsRejected() throws Exception {
		assertThatThrownBy(() -> serviceWith("key", "this is not json").generate(1, request()))
			.isInstanceOfSatisfying(
				RoadmapAiException.class,
				e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY)
			);
	}

	@Test
	void missingKeyGivesAClearError() throws Exception {
		assertThatThrownBy(() -> serviceWith("", null).generate(1, request()))
			.isInstanceOfSatisfying(
				RoadmapAiException.class,
				e -> {
					assertThat(e.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
					assertThat(e.getMessage()).contains("GEMINI_API_KEY");
				}
			);
	}

	@Test
	void progressIsCalculatedFromCompletedTasks() {
		LearningRoadmap r = new LearningRoadmap();
		r.setTitle("t");
		r.setLearningGoal("g");
		r.setCurrentLevel(Proficiency.BEGINNER);

		RoadmapMilestone m1 = new RoadmapMilestone();
		m1.setSequenceNumber(1);
		m1.setTitle("m1");
		m1.setEstimatedHours(10);
		m1.setRoadmap(r);
		for (int i = 0; i < 4; i++) {
			RoadmapTask t = new RoadmapTask();
			t.setTitle("t" + i);
			t.setSequenceNumber(i + 1);
			t.setMilestone(m1);
			t.setCompleted(i < 1); // 1 of 4 done
			m1.getTasks().add(t);
		}
		r.getMilestones().add(m1);

		RoadmapView v = new RoadmapMapper().toDetail(r);

		assertThat(v.getProgressPercent()).isEqualTo(25);
		assertThat(v.getCompletedTasks()).isEqualTo(1);
		assertThat(v.getTotalTasks()).isEqualTo(4);
		assertThat(v.getEstimatedHoursCompleted()).isEqualTo(3); // 25% of 10h, rounded
		assertThat(v.getNext().getTaskTitle()).isEqualTo("t1");
	}
}
