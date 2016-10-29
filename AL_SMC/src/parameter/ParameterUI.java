package parameter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.UserFile;

public class ParameterUI {
	public static TextField pcaField = null; // PCAthreshold
	public static TextField learnTNField = null; // learnTraceNum
	public static TextField extractTNField = null; // extractTraceNum
	public static TextField extractTPField = null; // extractTraceProbability
	public static TextField bietKField = null; // default 0.05 false negatives
	public static TextField bietCField = null; // default 0.99 Ratio bound
	// Parameter window
	private Stage parameterStage = null;

	public void start(Stage parameterStage) throws Exception {
		parameterStage.getIcons().add(new Image("modana-logo.png"));
		this.parameterStage = parameterStage;
		parameterStage.initModality(Modality.WINDOW_MODAL);
		parameterStage.setOpacity(0.87);
		parameterStage.setTitle("Parameter List");
		parameterStage.setWidth(400);
		parameterStage.setHeight(330);
		parameterStage.centerOnScreen();
	//	parameterStage.setResizable(false);
		BorderPane parameterRoot = new BorderPane();
		initPlotPanel(parameterRoot);
		Scene plotScene = new Scene(parameterRoot);
		parameterStage.setScene(plotScene);
		parameterStage.show();
	}

	private void initPlotPanel(BorderPane parameterRoot) {
		Font font3 = new Font(12);
		Font font4 = new Font(15);
		VBox vBox = new VBox(10);
		vBox.setAlignment(Pos.CENTER);
		HBox hBox0 = new HBox(7);
		hBox0.setAlignment(Pos.CENTER);
		Label setting = new Label("Parameters Setting");
		setting.setFont(font4);
		// add PCAthreshold Input
		HBox hBox1 = new HBox(7);
		Label pca = new Label("            PCA Threshold :   ");
		pca.setFont(font3);
		pcaField = new TextField();
		pcaField.setId("pcaField");
		pcaField.setPrefWidth(200);
		// add learnTraceNum
		HBox hBox2 = new HBox(7);
		Label learnTN = new Label("        Learning Number :   ");
		learnTN.setFont(font3);
		learnTNField = new TextField();
		learnTNField.setId("learnTNField");
		learnTNField.setPrefWidth(200);
		// add extractTraceNum
		HBox hBox3 = new HBox(7);
		Label extractTN = new Label("      Extracting Number :   ");
		extractTN.setFont(font3);
		extractTNField = new TextField();
		extractTNField.setId("extractTNField");
		extractTNField.setPrefWidth(200);
		// add extractTraceProbability
		HBox hBox4 = new HBox(7);
		Label extractTP = new Label("  Extracting Probability :   ");
		extractTP.setFont(font3);
		extractTPField = new TextField();
		extractTPField.setId("extractTPField");
		extractTPField.setPrefWidth(200);
		// add false negatives
		HBox hBox5 = new HBox(7); 
		Label bietK = new Label("            False Negatives :   ");
		bietK.setFont(font3);
		bietKField = new TextField();
		bietKField.setId("bietKField");
		bietKField.setPrefWidth(200);
		// add Ratio bound
		HBox hBox6 = new HBox(7);
		Label bietC = new Label("                 Ratio Bound :   ");
		bietC.setFont(font3);
		bietCField = new TextField();
		bietCField.setId("bietCField");
		bietCField.setPrefWidth(200);
		HBox hBox7 = new HBox(7);
		hBox7.setAlignment(Pos.CENTER);
		Button submit = new Button("Submit");
		
		submit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (ParameterUI.pcaField.getText()!=null&&!ParameterUI.pcaField.getText().toString().equals("")) {
					UserFile.PCAthreshold = Double.parseDouble(ParameterUI.pcaField.getText().toString());
				}
				if (ParameterUI.learnTNField.getText()!=null&&!ParameterUI.learnTNField.getText().toString().equals("")) {
					UserFile.learnTraceNum = Integer.parseInt(ParameterUI.learnTNField.getText().toString());
				}
				if (ParameterUI.extractTNField.getText()!=null&&!ParameterUI.extractTNField.getText().toString().equals("")) {
					UserFile.extractTraceNum = Integer.parseInt(ParameterUI.extractTNField.getText().toString());
				}
				if (ParameterUI.extractTPField.getText()!=null&&!ParameterUI.extractTPField.getText().toString().equals("")) {
					UserFile.extractTraceProbability = Double.parseDouble(ParameterUI.extractTPField.getText().toString());
				}
				if (ParameterUI.bietKField.getText()!=null&&!ParameterUI.bietKField.getText().toString().equals("")) {
					UserFile.bietK = Double.parseDouble(ParameterUI.bietKField.getText().toString());
				}
				if (ParameterUI.bietCField.getText()!=null&&!ParameterUI.bietCField.getText().toString().equals("")) {
					UserFile.bietC = Double.parseDouble(ParameterUI.bietCField.getText().toString());
				}
				parameterStage.close();
			}
		});
		
		hBox0.getChildren().add(setting);
		hBox1.getChildren().addAll(pca,pcaField);
		hBox2.getChildren().addAll(learnTN,learnTNField);
		hBox3.getChildren().addAll(extractTN,extractTNField);
		hBox4.getChildren().addAll(extractTP,extractTPField);
		hBox5.getChildren().addAll(bietK,bietKField);
		hBox6.getChildren().addAll(bietC,bietCField);
		hBox7.getChildren().add(submit);
		vBox.getChildren().addAll(hBox0,hBox1,hBox2,hBox3,hBox4,hBox5,hBox6,hBox7);
		parameterRoot.setCenter(vBox);
	}

	public Stage getParameterUI() {
		return parameterStage;
	}
}