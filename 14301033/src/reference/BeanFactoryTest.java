package reference;



import reference.BeanFactory;
import reference.XMLBeanFactory;
import reference.LocalFileResource;

public class BeanFactoryTest {

	public static void main(String[] args) {
		LocalFileResource resource = new LocalFileResource("beans.xml");

		BeanFactory beanFactory = new XMLBeanFactory(resource);
		// the BeanDefinition doesn`t create the real bean yet
		Dishes d = (Dishes) beanFactory.getBean("egg dishes");
		Car c = (Car) beanFactory.getBean("car");
		Office o = (Office) beanFactory.getBean("office");
		System.out.println(d.printDishes() + d.getCar().printDishes() + d.getCar().getOffice().P());

	}
}
