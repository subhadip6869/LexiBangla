package org.originsd.lexibangla.ui.ribbon;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Top-level Ribbon navigation component with content-driven height.
 */
public class Ribbon extends VBox {

    private final HBox tabBar;
    private final StackPane contentArea;
    private final List<RibbonTab> tabs = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();
    private int selectedIndex = -1;

    public Ribbon() {
        getStyleClass().add("ribbon-bar");
        setMinHeight(Region.USE_COMPUTED_SIZE);
        setPrefHeight(Region.USE_COMPUTED_SIZE);

        tabBar = new HBox(2);
        tabBar.getStyleClass().add("ribbon-tab-bar");
        tabBar.setAlignment(Pos.CENTER_LEFT);

        contentArea = new StackPane();
        contentArea.getStyleClass().add("ribbon-content-area");
        contentArea.setMinHeight(Region.USE_COMPUTED_SIZE);
        contentArea.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        getChildren().addAll(tabBar, contentArea);
    }

    public void addTab(RibbonTab tab) {
        tabs.add(tab);
        int index = tabs.size() - 1;

        Button tabButton = new Button(tab.getTitle());
        tabButton.getStyleClass().add("ribbon-tab-button");
        tabButton.setFocusTraversable(false);
        tabButton.setOnAction(e -> selectTab(index));

        tabButtons.add(tabButton);
        tabBar.getChildren().add(tabButton);

        if (selectedIndex == -1) {
            selectTab(0);
        }
    }

    public void selectTab(int index) {
        if (index < 0 || index >= tabs.size()) return;

        selectedIndex = index;
        for (int i = 0; i < tabButtons.size(); i++) {
            Button btn = tabButtons.get(i);
            if (i == index) {
                if (!btn.getStyleClass().contains("selected")) {
                    btn.getStyleClass().add("selected");
                }
            } else {
                btn.getStyleClass().remove("selected");
            }
        }

        contentArea.getChildren().clear();
        contentArea.getChildren().add(tabs.get(index).getContent());
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}
