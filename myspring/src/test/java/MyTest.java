import com.ying.entiy.Codes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author xieyingheng
 * @title: TestBeanWrapper
 * @projectName spring
 * @description: TODO
 * @date 2020-02-0610:41
 */
//@RunWith(SpringJUnit4ClassRunner.class) //使用junit4进行测试
//@ContextConfiguration(locations={"bean.xml"}) //加载配置文件
public class MyTest {

	@Test
	public void test1(){
		ClassPathResource resource = new ClassPathResource("bean.xml"); // <1>
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory(); // <2>
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory); // <3>
		reader.loadBeanDefinitions(resource); // <4>
		Codes codes = (Codes) factory.getBean("codes");
		Codes codes1 = (Codes) factory.getBean("codes");
		System.out.println(codes);
		System.out.println(codes1);
		System.out.println(codes.getWorker());
	}
}
