package ecnu.modana.util;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MyUtil {
	//private static WiAlertDialog wiAlertDialog;
	private static Stage  newAlertDialog ;
	public static void showAlertDialog(String message) {
        newAlertDialog = new Stage(StageStyle.TRANSPARENT);
        newAlertDialog.setResizable(false);
        //wiAlertDialog = new WiAlertDialog(message);
        //newAlertDialog.setScene(new Scene(wiAlertDialog));
        newAlertDialog.show();
    }
}
