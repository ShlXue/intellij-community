package com.intellij.ide;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.options.CompositeConfigurable;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Arrays;
import java.util.List;
import java.awt.*;

public class GeneralSettingsConfigurable extends CompositeConfigurable<Configurable> implements SearchableConfigurable {
  public static ExtensionPointName<Configurable> EP_NAME = ExtensionPointName.create("com.intellij.generalOptionsProvider");
  
  private MyComponent myComponent;


  public void apply() throws ConfigurationException {
    super.apply();
    GeneralSettings settings = GeneralSettings.getInstance();

    settings.setReopenLastProject(myComponent.myChkReopenLastProject.isSelected());
    settings.setSyncOnFrameActivation(myComponent.myChkSyncOnFrameActivation.isSelected());
    settings.setSaveOnFrameDeactivation(myComponent.myChkSaveOnFrameDeactivation.isSelected());
    settings.setConfirmExit(myComponent.myConfirmExit.isSelected());
    // AutoSave in inactive
    settings.setAutoSaveIfInactive(myComponent.myChkAutoSaveIfInactive.isSelected());
    try {
      int newInactiveTimeout = Integer.parseInt(myComponent.myTfInactiveTimeout.getText());
      if (newInactiveTimeout > 0) {
        settings.setInactiveTimeout(newInactiveTimeout);
      }
    }
    catch (NumberFormatException e) {
    }


  }

  public boolean isModified() {
    if (super.isModified()) return true;
    boolean isModified = false;
    GeneralSettings settings = GeneralSettings.getInstance();
    isModified |= settings.isReopenLastProject() != myComponent.myChkReopenLastProject.isSelected();
    isModified |= settings.isSyncOnFrameActivation() != myComponent.myChkSyncOnFrameActivation.isSelected();
    isModified |= settings.isSaveOnFrameDeactivation() != myComponent.myChkSaveOnFrameDeactivation.isSelected();
    isModified |= settings.isAutoSaveIfInactive() != myComponent.myChkAutoSaveIfInactive.isSelected();
    isModified |= settings.isConfirmExit() != myComponent.myConfirmExit.isSelected();

    int inactiveTimeout = -1;
    try {
      inactiveTimeout = Integer.parseInt(myComponent.myTfInactiveTimeout.getText());
    }
    catch (NumberFormatException e) {
    }

    isModified |= inactiveTimeout > 0 && settings.getInactiveTimeout() != inactiveTimeout;

    return isModified;
  }

  public JComponent createComponent() {
//    optionGroup.add(getDiffOptions().getPanel());
    myComponent = new MyComponent();

    myComponent.myChkAutoSaveIfInactive.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        myComponent.myTfInactiveTimeout.setEditable(myComponent.myChkAutoSaveIfInactive.isSelected());
      }
    });

    List<Configurable> list = getConfigurables();
    if (!list.isEmpty()) {
      myComponent.myPluginOptionsPanel.setLayout(new GridLayout(list.size(), 1));
      for (Configurable c : list) {
        myComponent.myPluginOptionsPanel.add(c.createComponent());
      }
    }

    return myComponent.myPanel;
  }

  public String getDisplayName() {
    return IdeBundle.message("title.general");
  }

  public Icon getIcon() {
    return IconLoader.getIcon("/general/configurableGeneral.png");
  }

  public void reset() {
    super.reset();
    GeneralSettings settings = GeneralSettings.getInstance();
    myComponent.myChkReopenLastProject.setSelected(settings.isReopenLastProject());
    myComponent.myChkSyncOnFrameActivation.setSelected(settings.isSyncOnFrameActivation());
    myComponent.myChkSaveOnFrameDeactivation.setSelected(settings.isSaveOnFrameDeactivation());

    myComponent.myChkAutoSaveIfInactive.setSelected(settings.isAutoSaveIfInactive());
    myComponent.myTfInactiveTimeout.setText(Integer.toString(settings.getInactiveTimeout()));
    myComponent.myTfInactiveTimeout.setEditable(settings.isAutoSaveIfInactive());
    myComponent.myConfirmExit.setSelected(settings.isConfirmExit());
  }

  public void disposeUIResources() {
    super.disposeUIResources();
    myComponent = null;
  }

  @NotNull
  public String getHelpTopic() {
    return "preferences.general";
  }



  private static class MyComponent {
    JPanel myPanel;

    private JCheckBox myChkReopenLastProject;
    private JCheckBox myChkSyncOnFrameActivation;
    private JCheckBox myChkSaveOnFrameDeactivation;
    private JCheckBox myChkAutoSaveIfInactive;
    private JTextField myTfInactiveTimeout;
    public JCheckBox myConfirmExit;
    private JPanel myPluginOptionsPanel;


    public MyComponent() {
    }
  }

  public String getId() {
    return getHelpTopic();
  }

  @Nullable
  public Runnable enableSearch(String option) {
    return null;
  }

  protected List<Configurable> createConfigurables() {
    return Arrays.asList(Extensions.getExtensions(EP_NAME));
  }
}