package ecnu.modana.alsmc.parameter;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HeuristicUI {
	// Heuristic window
		private Stage heuristicStage = null;
		TextField bietKField = null; // default 0.05 false negatives
		TextField bietCField = null; // default 0.99 Ratio bound
		public void start(Stage heuristicStage) throws Exception {
			this.heuristicStage = heuristicStage;
			heuristicStage.initModality(Modality.WINDOW_MODAL);
			heuristicStage.setOpacity(0.87);
			heuristicStage.setTitle("Heuristic Method");
			heuristicStage.setWidth(400);
			heuristicStage.setHeight(200);
			heuristicStage.centerOnScreen();
			heuristicStage.setResizable(false);
			BorderPane heuristicRoot = new BorderPane();
			initPlotPanel(heuristicRoot);
			Scene plotScene = new Scene(heuristicRoot);
			heuristicStage.setScene(plotScene);
			heuristicStage.show();
		}

		private void initPlotPanel(BorderPane heuristicRoot) {
			Font font3 = new Font(12);
			Font font4 = new Font(15);
			VBox vBox = new VBox(10);
			vBox.setAlignment(Pos.CENTER);
			HBox hBox0 = new HBox(7);
			hBox0.setAlignment(Pos.CENTER);
			Label hm = new Label("Using Heuristic Method");
			hm.setFont(font4);
			// add false negatives
			HBox hBox1 = new HBox(7); 
			Label bietK = new Label("            False Negatives :   ");
			bietK.setFont(font3);
			bietKField = new TextField();
			bietKField.setId("bietKField");
			bietKField.setPrefWidth(200);
			// add Ratio bound
			HBox hBox2 = new HBox(7);
			Label bietC = new Label("                 Ratio Bound :   ");
			bietC.setFont(font3);
			bietCField = new TextField();
			bietCField.setId("bietCField");
			bietCField.setPrefWidth(200);
			HBox hBox3 = new HBox(7);
			hBox3.setAlignment(Pos.CENTER);
			Button submit = new Button("Confirm");
			hBox0.getChildren().add(hm);
			hBox1.getChildren().addAll(bietK,bietKField);
			hBox2.getChildren().addAll(bietC,bietCField);
			hBox3.getChildren().add(submit);
			vBox.getChildren().addAll(hBox0,hBox1,hBox2,hBox3);
			heuristicRoot.setCenter(vBox);
		}

		public Stage getHeuristicStage() {
			return heuristicStage;
		}
		
}
