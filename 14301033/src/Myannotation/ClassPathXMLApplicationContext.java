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
 * @Description: spring�е�ע��ԭ��
 * @ClassName: ClassPathXMLApplicationContext
 */
public class ClassPathXMLApplicationContext {

	Map<String, Object> sigletions = new HashMap<String, Object>();

	public ClassPathXMLApplicationContext(String fileName) {

		// ʵ����bean
		this.instancesBean(this.readXMLpackagename(fileName));
		// Autowiredע�⴦����
		this.annotationInject();
	}

	/**
	 * ��ȡBean�����ļ�
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
	 * ʵ����Bean
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
	 * ע�⴦���� ���ע��������name���ԣ������name��ָ�������ƻ�ȡҪע���ʵ�����ã�
	 * ���ע��û������name���ԣ��������������������ɨ�������ļ���ȡҪ ע���ʵ������
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
	 * ������set���������ע��
	 * 
	 * @param bean
	 *            �����bean
	 */
	public void propertyAnnotation(Object bean) {
		try {
			// ��ȡ�����Ե�����
			PropertyDescriptor[] ps = Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors();
			for (PropertyDescriptor proderdesc : ps) {
				// ��ȡ����set����
				Method setter = proderdesc.getWriteMethod();
				// �ж�set�����Ƿ�����ע��
				if (setter != null && setter.isAnnotationPresent(Autowired.class)) {
					// ��ȡ��ǰע�⣬���ж�name�����Ƿ�Ϊ��
					Autowired resource = setter.getAnnotation(Autowired.class);
					String name = "";
					Object value = null;
					if (resource.name() != null && !"".equals(resource.name())) {
						// ��ȡע���name���Ե�����
						name = resource.name();
						value = sigletions.get(name);
					} else { // �����ǰע��û��ָ��name����,��������ͽ���ƥ��
						for (String key : sigletions.keySet()) {
							// �жϵ�ǰ���������������Ƿ��������ļ��д���
							if (proderdesc.getPropertyType().isAssignableFrom(sigletions.get(key).getClass())) {
								// ��ȡ����ƥ���ʵ������
								value = sigletions.get(key);
								break;
							}
						}
					}
					// �������private����
					setter.setAccessible(true);
					// �����ö���ע������
					setter.invoke(bean, value);
				}
			}
		} catch (Exception e) {

		}
	}

	/**
	 * �������ֶ��ϵ�ע��
	 * 
	 * @param bean
	 *            �����bean
	 */
	public void fieldAnnotation(Object bean) {
		try {
			// ��ȡ��ȫ�����ֶ�����
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
							// �жϵ�ǰ���������������Ƿ��������ļ��д���
							if (f.getType().isAssignableFrom(sigletions.get(key).getClass())) {
								// ��ȡ����ƥ���ʵ������
								value = sigletions.get(key);
								break;
							}
						}
					}
					// �������private�ֶ�
					f.setAccessible(true);
					// �����ö���ע������
					f.set(bean, value);
				}
			}
		} catch (Exception e) {

		}
	}

	/**
	 * ��ȡMap�еĶ�Ӧ��beanʵ��
	 * 
	 * @param beanId
	 * @return
	 */
	public Object getBean(String beanId) {
		return sigletions.get(beanId);
	}

	
}
