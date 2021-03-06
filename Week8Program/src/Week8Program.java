/**
 *	This program creates a web browser.
 *
 *  Date Last Modified: 11/2/2018
 *	@author Charles Vidro
 *
 *	CS1131, Fall 2018
 *	Lab Section 3
 */

// IMPORTS
// These are some classes that may be useful for completing the project.
// You may have to add others.
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.concurrent.Worker.State;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 * The main class for Week8Program. Week8Program constructs the JavaFX window and
 * handles interactions with the dynamic components contained therein.
 */
public class Week8Program extends Application {
	// INSTANCE VARIABLES
	private Stage stage = null; // the stage
	private BorderPane borderPane = null; // the pane that contains the general layout
    private WebView view = null; // the webview in center
	private WebEngine webEngine = null; // the webengine
	private TextField statusbar = null; // the status bar to display moused over hyperlinks
	private ArrayList<String> history = new ArrayList<>(); // an array that contains the history
	private TextField addressBar = null; // the address bar
	private int historyIndex = -1; // keeps track of history index
	private boolean newPage = true; // determines if a new page is loaded to add it to history
    private String lastHyperlink = ""; // keeps the last hyperlink and displays in status bar as a workaround to webpage design variations

	// HELPER METHODS
	/**
	 * Retrieves the value of a command line argument specified by the index.
	 *
	 * @param index - position of the argument in the args list.
	 * @return The value of the command line argument.
	 */
	private String getParameter( int index ) {
		Parameters params = getParameters();
		List<String> parameters = params.getRaw();
		return !parameters.isEmpty() ? parameters.get(index) : "";
	}

	/**
	 * Creates a WebView which handles mouse and some keyboard events, and
	 * manages scrolling automatically, so there's no need to put it into a ScrollPane.
	 * The associated WebEngine is created automatically at construction time.
	 *
	 * @return browser - a WebView container for the WebEngine.
	 */
	private WebView makeHtmlView( ) {
		view = new WebView();
		webEngine = view.getEngine();
		webEngine.getLoadWorker().stateProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != State.SUCCEEDED) {
                        return;
                    }

                    //Status Bar events
                    EventListener mouseOver = e -> {
                        String hyperlink = ((Element) e.getTarget()).getAttribute("href");
                        if (hyperlink != null) {
                            lastHyperlink = e.getTarget().toString();
                            statusbar.setText(e.getTarget().toString());
                            //System.out.println("Entered link " + e.getTarget().toString());
                        }
                        else {
                            statusbar.setText(lastHyperlink);
                        }
                    };

                    EventListener mouseOut = e -> {
                        if (e.getTarget().toString().contains("http")) {
                            statusbar.setText("");
                            //System.out.println("Exited link " + e.getTarget().toString());
                        }
                    };

                    // Adding page nodes to a list and adding event listeners
                    Document document = webEngine.getDocument();
                    NodeList nodeList = document.getElementsByTagName("a");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        ((EventTarget) nodeList.item(i)).addEventListener("mouseover", mouseOver, false);
                        ((EventTarget) nodeList.item(i)).addEventListener("mouseout", mouseOut, false);
                    }

                    // System.out.println("Loaded a new page");

                    // reset view to webEngine if not already
                    if(!borderPane.getCenter().toString().substring(0,7).equals("WebView")) {
                        borderPane.setCenter(view);
                    }

                    // set scene title
                    if (webEngine.getTitle()!= null) {
                        stage.setTitle(webEngine.getTitle());
                    } else {
                        stage.setTitle(webEngine.getLocation());
                    }

                    // Update address bar
                    addressBar.setText(webEngine.getLocation());

                    // Add to history
                    if (newPage) {
                        history.add(webEngine.getLocation());
                        historyIndex = history.size() - 1;
                        //System.out.println(history.toString());
                    }

                    newPage = true;
                }
        );
		return view;
	}

	/**
	 * Generates the status bar layout and text field.
	 *
	 * @return statusbarPane - the HBox layout that contains the statusbar.
	 */
	private HBox makeStatusBar( ) {
		HBox statusbarPane = new HBox();
		statusbarPane.setPadding(new Insets(5, 4, 5, 4));
		statusbarPane.setSpacing(10);
		statusbarPane.setStyle("-fx-background-color: #336699;");
		statusbar = new TextField();
		statusbar.setEditable(false);
		HBox.setHgrow(statusbar, Priority.ALWAYS);
		statusbarPane.getChildren().addAll(statusbar);
		return statusbarPane;
	}

    /**
     * Generates the control box and prompts for controls to be created
     *
     * @return controls - the control box
     */
	private HBox makeControlBox() {
	    HBox controls = new HBox();
	    controls.setPadding(new Insets(5,5,5,5));
	    controls.setSpacing(10);
	    controls.setStyle("-fx-background-color: #f2f2f2;");
	    controls.getChildren().addAll(makeBackButton(), makeForwardButton(), makeAddressBar(), makeHelpButton());

	    return controls;
    }

    /**
     * Generates the back button
     *
     * @return backButton
     */
    private Button makeBackButton() {
	    Button backButton = new Button("", new ImageView("leftButton.png"));
        backButton.setOnAction(e -> {
            if (historyIndex > 0) {
                newPage = false;
                if (history.get(historyIndex - 1).equals("browser://help")) {
                    historyIndex--;
                    displayHelp();
                } else {
                    webEngine.load(history.get(--historyIndex));
                }
            }
            //System.out.println("back " + historyIndex);
        });
	    return backButton;
    }

    /**
     * Generates the forward button
     *
     * @return forwardButton
     */
    private Button makeForwardButton() {
        Button forwardButton = new Button("", new ImageView("rightButton.png"));
        forwardButton.setOnAction(e -> {
            if (history.size() > historyIndex + 1) {
                newPage = false;
                if (history.get(historyIndex + 1).equals("browser://help")) {
                    historyIndex++;
                    displayHelp();
                } else {
                    webEngine.load(history.get(++historyIndex));
                }
            }
            //System.out.println("forward " + historyIndex);
        });
        return forwardButton;
    }

    /**
     * Generates the address bar
     *
     * @return forwardButton
     */
    private TextField makeAddressBar() {
        addressBar = new TextField();
        addressBar.setEditable(true);
        addressBar.prefWidthProperty().bind(borderPane.widthProperty());
        addressBar.setOnAction(e -> {
            String input = addressBar.getText();

           if (input.equals("browser://help")) {
               displayHelp();
           }
           else {
              webEngine.load(correctFormat(input));
           }
		});

        return addressBar;
    }

    /**
     * Generates the help button
     *
     * @return helpButton
     */
    private Button makeHelpButton() {
        Button helpButton = new Button("", new ImageView("helpButton.png"));
        helpButton.setOnAction(e -> {
            displayHelp();
            historyIndex++;
        });

        return helpButton;
    }

    /**
     * Displays the help screen
     *
     * @return void
     */
    private void displayHelp() {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setSpacing(10);

        // Help Content
        Font titleFont = Font.font("Arial", FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 30.0);
        Font bodyFont = Font.font("Arial", FontWeight.NORMAL, FontPosture.REGULAR, 20);

        Text title = new Text("Web Browser Help Menu");
        title.setFont(titleFont);

        TextArea body = new TextArea("This is a basic web browser. For questions, refer to the following documentation." +
                "\nQ: How can I get help?" +
                "\nA: Click on the '?' help button, or type browser://help in the address bar\n" +
                "\nQ: Why won't the website I entered into the address bar work?" +
                "\nA: There was a format error. Follow the format http://www.addressname. If the problem persists, try another browser.\n" +
                "\nQ: How can I move backwards or forwards in my history?" +
                "\nA: Click on the '<-' backwards or '->' forwards button." +
                "\n\n For any other questions, contact cjvidro@mtu.edu" +
                "\n\n________________\nCharles Vidro" +
                "\nCS1131 R01, L03");

        body.setEditable(false);
        body.setWrapText(true);
        body.setPrefColumnCount(100);
        body.setPrefRowCount(500);
        body.setFont(bodyFont);

        vBox.getChildren().addAll(title, body);

        borderPane.setCenter(vBox);
        if (newPage) {
            history.add("browser://help");
        }
        newPage = true;
        stage.setTitle("Help");
        addressBar.setText("browser://help");
    }
    /**
     * Checks and attempts to correct format errors
     *
     * @param input The original url
     *
     * @return the correct url
     */
    private String correctFormat(String input) {
        if (!input.contains("http://" ) && !input.contains("https://" ) && !input.contains("www.")) {
            input = "http://www." + input;
        }
        if (input.contains("http://") && !input.contains("www.")) {
            input = "http://www." + input.substring(7);
        }
        if (input.contains("https://") && !input.contains("www.")) {
            input = "http://www." + input.substring(8);
        }
        if (!input.contains("http://")) {
            input = "http://" + input;
        }

        return input;
    }

	// REQUIRED METHODS
	/**
	 * The main entry point for all JavaFX applications. The start method is
	 * called after the init method has returned, and after the system is ready
	 * for the application to begin running.
	 *
	 * NOTE: This method is called on the JavaFX Application Thread.
	 *
	 * @param stage - the primary stage for this application, onto which
	 * the application scene can be set.
	 */
	@Override
	public void start(Stage stage) {
		borderPane = new BorderPane();
		borderPane.setBottom(makeStatusBar());
		borderPane.setTop(makeControlBox());
        borderPane.setCenter(makeHtmlView());

        Scene scene = new Scene(borderPane, 1280,720);
		this.stage = new Stage();
		this.stage.setScene(scene);
		this.stage.show();

        if (!getParameter(0).equals("")) {
            String parameter = getParameter(0);

            if (parameter.equals("browser://help")) {
                displayHelp();
            }
            else {
               webEngine.load(correctFormat(parameter));
            }
        } else {
            displayHelp();
        }
}

	/**
     * The main( ) method is ignored in JavaFX applications.
     * main( ) serves only as fallback in case the application is launched
     * as a regular Java application, e.g., in IDEs with limited FX
     * support.
     *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
}