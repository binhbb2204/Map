package view;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import combo_suggestion.ComboBoxSuggestion;
import radio_button.RadioButton;
import swing.Button;
import swing.Button1;
import swing.MyTextField;
import util.InputValidationDocumentListener;
import util.IntegerStringValidator;
import view.AppConstant.Mode;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import view.AppConstant.Painter;


public class ControlPanel extends JPanel {
    private Parameters parameters;

    private final MyTextField cellSizeTextField = new MyTextField();
    private final MyTextField animationMsTextField = new MyTextField();
	private final MyTextField obstaclePercentTextField = new MyTextField();
    private  Button stopButton;
	private  Button clearMapButton;
	private  Button generateMapButton;
	private  Button applyCellSizeButton;
	private  Button applyAnimationMsButton;
    private final ButtonGroup 	modeButtonGroup = new ButtonGroup();
    private final JPanel modesPanel = new JPanel();
	private final JPanel mapGeneratingPanel = new JPanel();
	private final JPanel mapSettingPanel = new JPanel();
	private final JPanel pathSearchPanel = new JPanel();

    static class TextFiledDocumentListener extends InputValidationDocumentListener {
		JTextField textField;
		
		@Override
		protected void onValidInput(String text) {
			textField.setForeground(Color.black);
		}

		@Override
		protected void onInvalidInput(String text) {
			textField.setForeground(Color.red);
		}

		public JTextField getTextField() {
			return textField;
		}

		public void setTextField(JTextField textField) {
			this.textField = textField;
		}
		
	}

    @SuppressWarnings("unchecked")
    public ControlPanel(Parameters aParameters){
        initComponents();
        setComponentBackgrounds();
        setBackground(Color.WHITE);
        parameters = aParameters;
        Dimension buttonSize = new Dimension(100, 30);
        stopButton.setPreferredSize(buttonSize);
        clearMapButton.setPreferredSize(buttonSize);
        generateMapButton.setPreferredSize(buttonSize);
        applyCellSizeButton.setPreferredSize(buttonSize);
        applyAnimationMsButton.setPreferredSize(buttonSize);
        modesPanel.setLayout(new GridLayout(0, 1, 2, 2));
        TitledBorder modeBorder = BorderFactory.createTitledBorder("Mode");
        modeBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 14));
        modeBorder.setTitleColor(new Color(127, 127, 127));
        
        modesPanel.setBorder(modeBorder);


        ActionListener radioButtonListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                Mode oldMode = parameters.getMode();
                Mode newMode = Mode.getValue(((RadioButton)evt.getSource()).getText());
                parameters.setMode(newMode);
                updateVisibility();
                firePropertyChange(AppConstant.StopRequested, null, null);
				firePropertyChange(AppConstant.ModeChanged, oldMode, newMode);
            }
        };

        Mode[] modes = Mode.values();
        for(Mode mode: modes){
            RadioButton button = new RadioButton(mode.toString());
            button.setFocusPainted(false);
            button.addActionListener(radioButtonListener);
            button.setSelected(parameters.getMode() == mode);
            modeButtonGroup.add(button);
            modesPanel.add(button);

        }

        obstaclePercentTextField.setText(Integer.toString(parameters.getObstaclePercent()));
        TextFiledDocumentListener percentTextFieldDocListener = new TextFiledDocumentListener(){
            @Override
            protected void onValidInput(String text) {
                super.onValidInput(text);
				generateMapButton.setEnabled(true);
            }
            @Override
            protected void onInvalidInput(String text) {
                super.onInvalidInput(text);
                generateMapButton.setEnabled(false);
            }
            
        };
        percentTextFieldDocListener.setValidator(new IntegerStringValidator());
		percentTextFieldDocListener.setTextField(obstaclePercentTextField);
		obstaclePercentTextField.getDocument().addDocumentListener(percentTextFieldDocListener);

		generateMapButton.setFocusPainted(false);
        generateMapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = obstaclePercentTextField.getText();
				parameters.setObstaclePercent(Integer.parseInt(text));
				firePropertyChange(AppConstant.GenerateMapRequested, null, null); 
            }
        });

        clearMapButton.setFocusPainted(false);
        clearMapButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                firePropertyChange(AppConstant.ClearMapRequested, null, null); 
            }
        });

        ComboBoxSuggestion painterComboBox = new ComboBoxSuggestion();
        Painter[] painters = Painter.values();
        for (Painter painter : painters) {
			painterComboBox.addItem(painter);
		}
        painterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parameters.setPainter((Painter) painterComboBox.getSelectedItem());
            }
        });

        mapGeneratingPanel.setLayout(new GridLayout(0, 1, 2, 2));
        JLabel obstacleLabel = new JLabel("Obstacle [%]:");
        obstacleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        obstacleLabel.setForeground(new Color(127, 127, 127));
        mapGeneratingPanel.add(obstacleLabel);

		mapGeneratingPanel.add(obstaclePercentTextField);
		mapGeneratingPanel.add(generateMapButton);
		mapGeneratingPanel.add(clearMapButton);

        JLabel toolboxLabel = new JLabel("Toolbox:");
        toolboxLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        toolboxLabel.setForeground(new Color(127, 127, 127));
        mapGeneratingPanel.add(toolboxLabel);
        mapGeneratingPanel.add(painterComboBox);
        
        //.......................................
        // path search 
        animationMsTextField.setText(Integer.toString(parameters.getAnimationMs()));
        TextFiledDocumentListener animationMsTextFieldDocListener = new TextFiledDocumentListener(){
            @Override
            protected void onValidInput(String text) {
                super.onValidInput(text);
				applyAnimationMsButton.setEnabled(true);
            }
            @Override
            protected void onInvalidInput(String text) {
                super.onInvalidInput(text);
				applyAnimationMsButton.setEnabled(false);
            }
        };
        animationMsTextFieldDocListener.setValidator(new IntegerStringValidator());
		animationMsTextFieldDocListener.setTextField(animationMsTextField);
		animationMsTextField.getDocument().addDocumentListener(animationMsTextFieldDocListener);
		
		applyAnimationMsButton.setFocusPainted(false);
        applyAnimationMsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = animationMsTextField.getText();
				parameters.setAnimationMs(Integer.parseInt(text));
            }
        });

        stopButton.setFocusPainted(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                firePropertyChange(AppConstant.StopRequested, null, null); 
            }
        });

        JPanel animationCtrlPanel = new JPanel();
		animationCtrlPanel.setLayout(new GridLayout(0, 1, 2, 2));
        JLabel animationLabel = new JLabel("Animation (ms):");
        animationLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        animationLabel.setForeground(new Color(127, 127, 127));
		animationCtrlPanel.add(animationLabel);
		animationCtrlPanel.add(animationMsTextField);
		animationCtrlPanel.add(applyAnimationMsButton);
        animationCtrlPanel.setBackground(Color.WHITE);

        JPanel runCtrlPanel = new JPanel();
		runCtrlPanel.setLayout(new GridLayout(0, 1, 2, 2));
		runCtrlPanel.add(stopButton);
        runCtrlPanel.setBackground(Color.WHITE);

        pathSearchPanel.setLayout(new BoxLayout(pathSearchPanel, BoxLayout.Y_AXIS));
		pathSearchPanel.add(animationCtrlPanel);
		pathSearchPanel.add(Box.createVerticalStrut(8));
		pathSearchPanel.add(runCtrlPanel);
        pathSearchPanel.setBackground(Color.WHITE);
        //......................................................
        //Map Setting
        cellSizeTextField.setText(Integer.toString(parameters.getCellSize()));
        TextFiledDocumentListener cellSizeTextFieldDocListener = new TextFiledDocumentListener() {

			@Override
			protected void onValidInput(String text) {
				super.onValidInput(text);
				applyCellSizeButton.setEnabled(true);
			}
			
			@Override
			protected void onInvalidInput(String text) {
				super.onInvalidInput(text);
				applyCellSizeButton.setEnabled(false);
			}
		};
        cellSizeTextFieldDocListener.setValidator(new IntegerStringValidator());
		cellSizeTextFieldDocListener.setTextField(cellSizeTextField);
		cellSizeTextField.getDocument().addDocumentListener(cellSizeTextFieldDocListener);
		
		applyCellSizeButton.setFocusPainted(false);
		applyCellSizeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = cellSizeTextField.getText();
				int oldValue = parameters.getCellSize();
				int newValue = Integer.parseInt(text);
				if (oldValue != newValue) {
					parameters.setCellSize(newValue);
					firePropertyChange(AppConstant.CellSizeChanged, oldValue, newValue); 
				}
			}
		});

        mapSettingPanel.setLayout(new GridLayout(0, 1, 2, 2));
        JLabel cellSizeLabel = new JLabel("Cell Size (pxl):");
        cellSizeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        cellSizeLabel.setForeground(new Color(127, 127, 127));
        mapSettingPanel.add(cellSizeLabel);
        mapSettingPanel.add(cellSizeTextField);
		mapSettingPanel.add(applyCellSizeButton);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
		add(modesPanel);
		add(Box.createVerticalStrut(8));
		add(mapGeneratingPanel);
		add(pathSearchPanel);
		add(mapSettingPanel);
		add(Box.createVerticalStrut(2000));

        int preferedWidth = modesPanel.getPreferredSize().width;
		preferedWidth = Math.max(preferedWidth, mapGeneratingPanel.getPreferredSize().width);
		preferedWidth = Math.max(preferedWidth, pathSearchPanel.getPreferredSize().width);
		preferedWidth = Math.max(preferedWidth, mapSettingPanel.getPreferredSize().width);
		modesPanel.setPreferredSize(new Dimension(preferedWidth, modesPanel.getPreferredSize().height));
		mapGeneratingPanel.setPreferredSize(new Dimension(preferedWidth, mapGeneratingPanel.getPreferredSize().height));
		pathSearchPanel.setPreferredSize(new Dimension(preferedWidth, pathSearchPanel.getPreferredSize().height));
		mapSettingPanel.setPreferredSize(new Dimension(preferedWidth, mapSettingPanel.getPreferredSize().height));
		
		updateVisibility();



    }


    public void initComponents() {
        cellSizeTextField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cellSizeTextField.setBackground(new Color(228, 246, 248));

        animationMsTextField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        animationMsTextField.setBackground(new Color(228, 246, 248));
        
        obstaclePercentTextField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        obstaclePercentTextField.setBackground(new Color(228, 246, 248));

        stopButton = new Button();
        stopButton.setText("Stop");
        stopButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        stopButton.setForeground(Color.WHITE);
        stopButton.setBackground(Color.decode("#0072ff"));

        generateMapButton = new Button();
        generateMapButton.setText("Generate Map");
        generateMapButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        generateMapButton.setForeground(Color.WHITE);
        generateMapButton.setBackground(Color.decode("#0072ff"));


        applyCellSizeButton = new Button();
        applyCellSizeButton.setText("Apply");
        applyCellSizeButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        applyCellSizeButton.setForeground(Color.WHITE);
        applyCellSizeButton.setBackground(Color.decode("#0072ff"));

        applyAnimationMsButton = new Button();
        applyAnimationMsButton.setText("Apply");
        applyAnimationMsButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        applyAnimationMsButton.setForeground(Color.WHITE);
        applyAnimationMsButton.setBackground(Color.decode("#0072ff"));

        clearMapButton = new Button();
        clearMapButton.setText("Clear Map");
        clearMapButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        clearMapButton.setForeground(Color.WHITE);
        clearMapButton.setBackground(Color.decode("#0072ff"));
        
        
        
        
    }

    void updateVisibility() {
		mapGeneratingPanel.setVisible(parameters.getMode() == Mode.MAP_EDITING_MODE);
		pathSearchPanel.setVisible(parameters.getMode() == Mode.PATH_SEARCH_MODE);
		mapSettingPanel.setVisible(parameters.getMode() == Mode.MAP_SETTING_MODE);
	}

    private void setComponentBackgrounds() {
        setBackground(Color.WHITE);
        modesPanel.setBackground(Color.WHITE);
        mapGeneratingPanel.setBackground(Color.WHITE);
        pathSearchPanel.setBackground(Color.WHITE);
        mapSettingPanel.setBackground(Color.WHITE);
        
    }

    public Parameters getParameters() {
		return parameters;
	}
}
