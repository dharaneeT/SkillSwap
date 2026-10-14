package com.skillSwap.Service;

import com.skillSwap.Dto.roadmap.RoadmapDtos.*;
import com.skillSwap.Entity.RoadmapEnums.Proficiency;
import com.skillSwap.Entity.RoadmapEnums.RoadmapTaskType;
import com.skillSwap.Exception.ResourceNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RoadmapTemplateService {

	private static final List<String> IDS = List.of("java-backend", "react-frontend", "generic-skill");

	public List<TemplateInfo> list() {
		return IDS
			.stream()
			.map(id -> {
				Draft d = draft(id, null);
				return TemplateInfo
					.builder()
					.id(id)
					.title(d.getTitle())
					.description(d.getDescription())
					.milestoneCount(d.getMilestones().size())
					.build();
			})
			.toList();
	}

	public Draft draft(String id, String skill) {
		return switch (id) {
			case "java-backend" -> javaBackend();
			case "react-frontend" -> reactFrontend();
			case "generic-skill" -> genericSkill(skill == null || skill.isBlank() ? "a new skill" : skill.strip());
			default -> throw new ResourceNotFoundException("Template", "id", id);
		};
	}

	// ------------------------------------------------------------ templates

	private Draft javaBackend() {
		return base(
			"Backend Java Developer",
			"From Java basics to a secured, database-backed Spring Boot REST API.",
			"Become a backend Java developer",
			"Java",
			List.of("A computer with a Java 21 JDK installed", "Comfort with basic computer use"),
			ms(
				"Java fundamentals",
				"Syntax, types, control flow and methods.",
				12,
				"Write and run Java programs|Use loops, conditions and methods",
				"You can solve small problems without looking up basic syntax.",
				List.of(),
				t("Install JDK 21 and an IDE, run your first program", RoadmapTaskType.LEARN, 60),
				t("Learn variables, types, operators and control flow", RoadmapTaskType.LEARN, 180),
				t("Solve 15 beginner problems (loops, conditions, methods)", RoadmapTaskType.PRACTICE, 240)
			),
			ms(
				"Object-oriented programming",
				"Classes, objects, inheritance, interfaces and polymorphism.",
				14,
				"Model real things as classes|Explain encapsulation and polymorphism",
				"You can design a small class hierarchy and explain your choices.",
				List.of(1),
				t("Learn classes, objects and encapsulation", RoadmapTaskType.LEARN, 150),
				t("Learn inheritance, interfaces and polymorphism", RoadmapTaskType.LEARN, 150),
				t("Model a library or bank system with OOP", RoadmapTaskType.PRACTICE, 240)
			),
			ms(
				"Arrays, collections and problem solving",
				"Lists, maps, sets and basic complexity.",
				14,
				"Pick the right collection|Reason about speed with Big-O",
				"You can solve medium problems using collections.",
				List.of(2),
				t("Learn arrays, ArrayList, HashMap and HashSet", RoadmapTaskType.LEARN, 180),
				t("Solve 20 collection-based problems", RoadmapTaskType.PRACTICE, 300),
				t("Explain the Big-O of common collection operations", RoadmapTaskType.REVIEW, 60)
			),
			ms(
				"SQL and database fundamentals",
				"Relational modelling and everyday queries.",
				12,
				"Design simple tables with keys|Write SELECT, JOIN and GROUP BY",
				"You can design and query a small database.",
				List.of(1),
				t("Learn tables, primary and foreign keys", RoadmapTaskType.LEARN, 120),
				t("Learn SELECT, JOIN, GROUP BY and indexes", RoadmapTaskType.LEARN, 180),
				t("Design a schema and write queries for a mini store", RoadmapTaskType.PRACTICE, 240)
			),
			ms(
				"JDBC and database connectivity",
				"Talking to a database from plain Java.",
				8,
				"Use PreparedStatement safely",
				"A console app reads and writes real database rows.",
				List.of(3, 4),
				t("Learn JDBC connections and PreparedStatement", RoadmapTaskType.LEARN, 120),
				t("Build a console CRUD app with JDBC", RoadmapTaskType.PRACTICE, 240)
			),
			ms(
				"Spring Boot fundamentals",
				"Project setup, beans, dependency injection and configuration.",
				12,
				"Explain dependency injection|Configure a Spring Boot app",
				"You can start a Spring Boot app and add your own beans.",
				List.of(2, 3),
				t("Learn Spring Boot structure, beans and dependency injection", RoadmapTaskType.LEARN, 180),
				t("Create a hello-world app with configuration properties", RoadmapTaskType.PRACTICE, 150)
			),
			ms(
				"REST API development",
				"Controllers, DTOs and correct HTTP status codes.",
				14,
				"Design clear endpoints|Return the right status codes",
				"You can build and test a CRUD API.",
				List.of(6),
				t("Learn REST principles, controllers and DTOs", RoadmapTaskType.LEARN, 150),
				t("Build a CRUD REST API for one resource", RoadmapTaskType.PRACTICE, 300),
				t("Test every endpoint with Postman or curl", RoadmapTaskType.REVIEW, 60)
			),
			ms(
				"Database integration and validation",
				"Spring Data JPA, validation and error handling.",
				12,
				"Map entities to tables|Validate input and handle errors",
				"Your API stores data in MySQL and rejects bad input.",
				List.of(5, 7),
				t("Learn JPA entities, repositories and relationships", RoadmapTaskType.LEARN, 180),
				t("Learn bean validation and global exception handling", RoadmapTaskType.LEARN, 120),
				t("Connect your REST API to MySQL with validation", RoadmapTaskType.PRACTICE, 240)
			),
			ms(
				"Authentication and authorization",
				"Password hashing, JWT and role-based access.",
				14,
				"Explain how JWT login works|Protect endpoints by role and owner",
				"Users can only see and change their own data.",
				List.of(8),
				t("Learn password hashing, JWT and Spring Security basics", RoadmapTaskType.LEARN, 180),
				t("Protect endpoints and add roles", RoadmapTaskType.PRACTICE, 240),
				t("Write tests proving users cannot access each other's data", RoadmapTaskType.REVIEW, 90)
			),
			ms(
				"Build and deploy a complete backend project",
				"Combine everything into one portfolio project.",
				30,
				"Plan, build, test and document a project",
				"A working, tested, documented API that someone else can run.",
				List.of(9),
				t("Plan and build your own backend project", RoadmapTaskType.PROJECT, 900),
				t("Write tests and a README with run instructions", RoadmapTaskType.PROJECT, 180),
				t("Get a mentor to review your project", RoadmapTaskType.MENTOR_SESSION, 60)
			)
		);
	}

	private Draft reactFrontend() {
		return base(
			"React Frontend Developer",
			"From web basics to building and shipping a React app.",
			"Become a React frontend developer",
			"React",
			List.of("A computer with Node.js installed", "Basic computer use"),
			ms(
				"HTML, CSS and layout",
				"Semantic HTML, CSS, flexbox and grid.",
				12,
				"Build responsive layouts",
				"You can recreate a simple page from a screenshot.",
				List.of(),
				t("Learn semantic HTML and CSS basics", RoadmapTaskType.LEARN, 150),
				t("Learn flexbox and grid", RoadmapTaskType.LEARN, 120),
				t("Build a responsive landing page", RoadmapTaskType.PRACTICE, 240)
			),
			ms(
				"Modern JavaScript",
				"ES6+, arrays, objects, promises and async/await.",
				14,
				"Use array methods and destructuring|Handle async code",
				"You can fetch data and render it with plain JavaScript.",
				List.of(1),
				t("Learn variables, functions, arrays and objects", RoadmapTaskType.LEARN, 180),
				t("Learn promises and async/await", RoadmapTaskType.LEARN, 120),
				t("Build a small app that calls a public API", RoadmapTaskType.PRACTICE, 240)
			),
			ms(
				"React basics",
				"Components, props and state.",
				12,
				"Split a UI into components|Manage state",
				"You can build a small interactive component tree.",
				List.of(2),
				t("Learn components, props and JSX", RoadmapTaskType.LEARN, 150),
				t("Learn state and events", RoadmapTaskType.LEARN, 120),
				t("Build a todo app", RoadmapTaskType.PRACTICE, 240)
			),
			ms(
				"Hooks and routing",
				"useEffect, custom hooks and React Router.",
				12,
				"Use hooks correctly|Add multiple pages",
				"A multi-page app with working navigation.",
				List.of(3),
				t("Learn useEffect, useMemo and custom hooks", RoadmapTaskType.LEARN, 180),
				t("Add routing with React Router", RoadmapTaskType.PRACTICE, 150)
			),
			ms(
				"Data fetching and state management",
				"Calling APIs, loading and error states, shared state.",
				12,
				"Handle loading and error states|Share state cleanly",
				"Your app talks to a backend with good loading and error states.",
				List.of(4),
				t("Learn data fetching patterns and error handling", RoadmapTaskType.LEARN, 150),
				t("Add shared state (Context or Redux Toolkit)", RoadmapTaskType.PRACTICE, 210)
			),
			ms(
				"Build and ship a React project",
				"A complete, deployed portfolio project.",
				24,
				"Plan, build and deploy",
				"A live app with a README.",
				List.of(5),
				t("Plan and build your own React project", RoadmapTaskType.PROJECT, 720),
				t("Deploy it and write a README", RoadmapTaskType.PROJECT, 120),
				t("Get a mentor to review your project", RoadmapTaskType.MENTOR_SESSION, 60)
			)
		);
	}

	private Draft genericSkill(String skill) {
		return base(
			"Learn " + skill,
			"A simple five-phase plan you can adapt to any skill.",
			"Learn " + skill,
			skill,
			List.of("Time set aside each week", "A way to practise regularly"),
			ms(
				"Foundations of " + skill,
				"Understand the basic vocabulary and the big picture.",
				8,
				"Know the key terms|Know what good looks like",
				"You can explain the basics to a friend.",
				List.of(),
				t("List the key terms and concepts of " + skill, RoadmapTaskType.LEARN, 90),
				t("Follow one beginner-friendly guide or course", RoadmapTaskType.LEARN, 180)
			),
			ms(
				"Core concepts",
				"Go deeper into the ideas that matter most.",
				10,
				"Understand the core techniques",
				"You can apply the core ideas without a guide.",
				List.of(1),
				t("Study the core techniques step by step", RoadmapTaskType.LEARN, 180),
				t("Do daily exercises for one week", RoadmapTaskType.PRACTICE, 210)
			),
			ms(
				"Guided practice",
				"Practise with feedback.",
				10,
				"Practise deliberately|Learn from feedback",
				"You have completed several exercises and fixed your mistakes.",
				List.of(2),
				t("Complete 5 practice exercises", RoadmapTaskType.PRACTICE, 240),
				t("Book a SkillSwap session to get feedback", RoadmapTaskType.MENTOR_SESSION, 60)
			),
			ms(
				"A real project",
				"Use the skill on something you care about.",
				14,
				"Apply the skill end to end",
				"You finished and can show a real result.",
				List.of(3),
				t("Plan a small real project", RoadmapTaskType.PROJECT, 60),
				t("Build and finish the project", RoadmapTaskType.PROJECT, 480)
			),
			ms(
				"Review and next steps",
				"Look back and plan what comes next.",
				4,
				"Spot gaps|Set the next goal",
				"You have a written plan for what to learn next.",
				List.of(4),
				t("Review what worked and what didn't", RoadmapTaskType.REVIEW, 60),
				t("Write your next learning goal", RoadmapTaskType.REVIEW, 30)
			)
		);
	}

	// ------------------------------------------------------------ builders

	private Draft base(
		String title,
		String description,
		String goal,
		String skill,
		List<String> prerequisites,
		MilestoneDraft... milestones
	) {
		return Draft
			.builder()
			.title(title)
			.description(description)
			.learningGoal(goal)
			.targetSkill(skill)
			.currentLevel(Proficiency.BEGINNER)
			.targetLevel(Proficiency.INTERMEDIATE)
			.preferences(List.of())
			.prerequisites(prerequisites)
			.milestones(Arrays.asList(milestones))
			.source("TEMPLATE")
			.build();
	}

	private MilestoneDraft ms(
		String title,
		String description,
		int hours,
		String objectives,
		String criteria,
		List<Integer> dependsOn,
		TaskDraft... tasks
	) {
		return MilestoneDraft
			.builder()
			.title(title)
			.description(description)
			.estimatedHours(hours)
			.learningObjectives(Arrays.asList(objectives.split("\\|")))
			.completionCriteria(criteria)
			.dependsOn(dependsOn)
			.tasks(Arrays.asList(tasks))
			.build();
	}

	private TaskDraft t(String title, RoadmapTaskType type, int minutes) {
		return TaskDraft.builder().title(title).type(type).estimatedMinutes(minutes).build();
	}
}
