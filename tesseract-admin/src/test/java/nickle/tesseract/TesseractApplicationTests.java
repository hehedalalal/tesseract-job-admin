package nickle.tesseract;

import admin.TesseractAdminApplication;
import admin.controller.TesseractLogController;
import admin.service.ITesseractLogService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TesseractAdminApplication.class)
public class TesseractApplicationTests {
	@Autowired
	private ITesseractLogService logService;
	@Autowired
	private TesseractLogController logController;
	@Test
	public void testTesseractLogService() {
		System.out.println(logService);
	}
	@Test
	public void testTesseractLogController() {
		System.out.println(logController.logService);
	}
}
