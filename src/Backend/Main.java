class User {

	private String name;
	private int age;

	User(String name, int age) {
		this.name = name;
		this.age = age;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}
gt
	int getAge() {
		return age;
	}

	void setAge(int age) {
		this.age = age;
	}
}

class Skill {

	private String skillName;

	Skill(String skillName) {
		this.skillName = skillName;
	}

	String getSkillName() {
		return skillName;
	}
}

public class Day3 {

	public static void main(String[] args) {
		User u = new User("Dharanee", 21);
		u.setAge(22);

		Skill s = new Skill("Java");

		System.out.println(u.getName() + " " + u.getAge());
		System.out.println(s.getSkillName());
	}
}
//class User {
//    private String name;
//    private int age;
//
//    User(String name,int age){
//        this.name=name;
//        this.age=age;
//    }
//
//    String getName(){return name;}
//    void setName(String name){this.name=name;}
//
//    int getAge(){return age;}
//    void setAge(int age){this.age=age;}
//}
//
//class Skill {
//    private String skillName;
//
//    Skill(String skillName){
//        this.skillName=skillName;
//    }
//
//    String getSkillName(){return skillName;}
//}
//
//public class Main{
//    public static void main(String[] args) {
//        User u=new User("Dharanee",21);
//        u.setAge(22);
//
//        Skill s=new Skill("Java");
//
//        System.out.println(u.getName()+" "+u.getAge());
//        System.out.println(s.getSkillName());
//    }
//}
