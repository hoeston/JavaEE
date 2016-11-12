package Myannotation;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @Description: spring中的注解原理
 * @ClassName: ClassPathXMLApplicationContext
 */
public class ClassPathXMLApplicationContext {

	Map<String, Object> sigletions = new HashMap<String, Object>();

	public ClassPathXMLApplicationContext(String fileName) {

		// 实例化bean
		this.instancesBean(this.readXMLpackagename(fileName));
		// Autowired注解处理器
		this.annotationInject();
	}

	/**
	 * 读取Bean配置文件
	 * 
	 * @param fileName
	 * @return
	 */
	public String readXMLpackagename(String fileName) {
		String packagename="";
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
			Document document = dbBuilder.parse(new FileInputStream(fileName));
			NodeList packAge = document.getElementsByTagName("package");
			for (int i = 0; i < packAge.getLength(); i++) {
				Node p = packAge.item(i);
				packagename=p.getAttributes().getNamedItem("name").getNodeValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return packagename;
	}

	/**
	 * 实例化Bean
	 */
	public void instancesBean(String packagename) {
//		for (BeanDefine bean : beanList) {
//			try {
//				System.out.println(bean.getBeanClassName());
//				sigletions.put(bean.getId(), Class.forName(bean.getBeanClassName()).newInstance());
//			} catch (Exception e) {
//			}
//		}
		 String id = "";
		 List<Class<?>> allClasses = ClassUtil.getClasses(packagename);
		 try {
		 for (Class<?> class1 : allClasses) {
		 if (class1.isAnnotationPresent(Component.class)) {
		 Component resource = class1.getAnnotation(Component.class);
		 if (resource.id() != null && !"".equals(resource.id())) {
		 id = resource.id();
		 sigletions.put(id, class1.newInstance());
		 } else {
		 id = class1.getSimpleName().substring(0, 1).toLowerCase() +
		 class1.getSimpleName().substring(1);
		 sigletions.put(id, class1.newInstance());
		 }
		
		 }
		 }
		 } catch (Exception e) {
		
		 }
	}

	/**
	 * 注解处理器 如果注解配置了name属性，则根据name所指定的名称获取要注入的实例引用，
	 * 如果注解没有配置name属性，则根据属性所属类型来扫描配置文件获取要 注入的实例引用
	 * 
	 */
	public void annotationInject() {
		for (String beanName : sigletions.keySet()) {
			Object bean = sigletions.get(beanName);
			if (bean != null) {
				this.propertyAnnotation(bean);
				this.fieldAnnotation(bean);
			}
		}
	}

	/**
	 * 处理在set方法加入的注解
	 * 
	 * @param bean
	 *            处理的bean
	 */
	public void propertyAnnotation(Object bean) {
		try {
			// 获取其属性的描述
			PropertyDescriptor[] ps = Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
			for (PropertyDescriptor proderdesc : ps) {
				// 获取所有set方法
				Method setter = proderdesc.getWriteMethod();
				// 判断set方法是否定义了注解
				if (setter != null && setter.isAnnotationPresent(Autowired.class)) {
					// 获取当前注解，并判断name属性是否为空
					Autowired resource = setter.getAnnotation(Autowired.class);
					String name = "";
					Object value = null;
					if (resource.name() != null && !"".equals(resource.name())) {
						// 获取注解的name属性的内容
						name = resource.name();
						value = sigletions.get(name);
					} else { // 如果当前注解没有指定name属性,则根据类型进行匹配
						for (String key : sigletions.keySet()) {
							// 判断当前属性所属的类型是否在配置文件中存在
							if (proderdesc.getPropertyType().isAssignableFrom(sigletions.get(key).getClass())) {
								// 获取类型匹配的实例对象
								value = sigletions.get(key);
								break;
							}
						}
					}
					// 允许访问private方法
					setter.setAccessible(true);
					// 把引用对象注入属性
					setter.invoke(bean, value);
				}
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 处理在字段上的注解
	 * 
	 * @param bean
	 *            处理的bean
	 */
	public void fieldAnnotation(Object bean) {
		try {
			// 获取其全部的字段描述
			Field[] fields = bean.getClass().getFields();
			for (Field f : fields) {
				if (f != null && f.isAnnotationPresent(Autowired.class)) {
					Autowired resource = f.getAnnotation(Autowired.class);
					String name = "";
					Object value = null;
					if (resource.name() != null && !"".equals(resource.name())) {
						name = resource.name();
						value = sigletions.get(name);
					} else {
						for (String key : sigletions.keySet()) {
							// 判断当前属性所属的类型是否在配置文件中存在
							if (f.getType().isAssignableFrom(sigletions.get(key).getClass())) {
								// 获取类型匹配的实例对象
								value = sigletions.get(key);
								break;
							}
						}
					}
					// 允许访问private字段
					f.setAccessible(true);
					// 把引用对象注入属性
					f.set(bean, value);
				}
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 获取Map中的对应的bean实例
	 * 
	 * @param beanId
	 * @return
	 */
	public Object getBean(String beanId) {
		return sigletions.get(beanId);
	}

	
}
