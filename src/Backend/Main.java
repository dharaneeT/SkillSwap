import java.util.*;

class Repository<T> {

	private List<T> data = new ArrayList<>();

	public void add(T item) {
		data.add(item);
	}

	public List<T> getAll() {
		return data;
	}

	public void remove(T item) {
		data.remove(item);
	}
}

class User {

	String name;

	public User(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}

public class Main {

	public static void main(String[] args) {
		Repository<User> userRepo = new Repository<>();

		userRepo.add(new User("DD"));
		userRepo.add(new User("Dt"));

		System.out.println(userRepo.getAll());
	}
}
//package Backend;
//
//import java.util.*;
//
//class User {
//    String name;
//    List<String> skillsOffered = new ArrayList<>();
//    List<String> skillsWanted = new ArrayList<>();
//
//    public User(String name) {
//        this.name = name;
//    }
//}
//
//
//class SkillMatcher {
//
//    List<User> users = new ArrayList<>();
//
//    void addUser(User user) {
//        users.add(user);
//    }
//
//    void matchUsers() {
//
//        for (User u1 : users) {
//
//            for (User u2 : users) {
//
//                if (u1 == u2) continue;
//
//                for (String skill : u1.skillsWanted) {
//
//                    if (u2.skillsOffered.contains(skill)) {
//                        System.out.println(
//                                u1.name + " matches with " + u2.name +
//                                        " for skill: " + skill
//                        );
//                    }
//                }
//            }
//        }
//    }
//}
//
//public class Main {
//    public static void main(String[] args) {
//
//        SkillMatcher matcher = new SkillMatcher();
//
//        User a = new User("Alice");
//        a.skillsWanted.add("Java");
//        a.skillsOffered.add("React");
//
//        User b = new User("Bob");
//        b.skillsWanted.add("React");
//        b.skillsOffered.add("Java");
//
//        User c = new User("Charlie");
//        c.skillsOffered.add("Python");
//
//        matcher.addUser(a);
//        matcher.addUser(b);
//        matcher.addUser(c);
//
//        matcher.matchUsers();
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
////class InvalidSkillException extends RuntimeException {
////    public InvalidSkillException(String message) {
////        super(message);
////    }
////}
////class SessionConflictException extends RuntimeException {
////    public SessionConflictException(String message) {
////        super(message);
////    }
////}
////class InsufficientCreditsException extends RuntimeException {
////    public InsufficientCreditsException(String message) {
////        super(message);
////    }
////}
////
//
////class User {
////
////	private String name;
////	private int age;
////
////	User(String name, int age) {
////		this.name = name;
////		this.age = age;
////	}
////
////	String getName() {
////		return name;
////	}
////
////	void setName(String name) {
////		this.name = name;
////	}
////gt
////	int getAge() {
////		return age;
////	}
////
////	void setAge(int age) {
////		this.age = age;
////	}
////}
////
////class Skill {
////
////	private String skillName;
////
////	Skill(String skillName) {
////		this.skillName = skillName;
////	}
////
////	String getSkillName() {
////		return skillName;
////	}
////}
////
////public class Day3 {
////
////	public static void main(String[] args) {
////		User u = new User("Dharanee", 21);
////		u.setAge(22);
////
////		Skill s = new Skill("Java");
////
////		System.out.println(u.getName() + " " + u.getAge());
////		System.out.println(s.getSkillName());
////	}
////}
////class User {
////    private String name;
////    private int age;
////
////    User(String name,int age){
////        this.name=name;
////        this.age=age;
////    }
////
////    String getName(){return name;}
////    void setName(String name){this.name=name;}
////
////    int getAge(){return age;}
////    void setAge(int age){this.age=age;}
////}
////
////class Skill {
////    private String skillName;
////
////    Skill(String skillName){
////        this.skillName=skillName;
////    }
////
////    String getSkillName(){return skillName;}
////}
////
////public class Main{
////    public static void main(String[] args) {
////        User u=new User("Dharanee",21);
////        u.setAge(22);
////
////        Skill s=new Skill("Java");
////
////        System.out.println(u.getName()+" "+u.getAge());
////        System.out.println(s.getSkillName());
////    }
////}
