package Myannotation;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ClassPathXMLApplicationContext path = new ClassPathXMLApplicationContext("MyAnnotation.xml");
		User userService = (User) path.getBean("user");
		userService.show();
	}

}
