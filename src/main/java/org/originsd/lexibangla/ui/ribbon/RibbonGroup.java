package org.originsd.lexibangla.ui.ribbon;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Group container inside a Ribbon tab with content area and bottom title label.
 */
public class RibbonGroup extends VBox {

    private final HBox contentBox;
    private final Label titleLabel;

    public RibbonGroup(String title, Node... nodes) {
        getStyleClass().add("ribbon-group");
        setAlignment(Pos.CENTER);
        setSpacing(3);
        setPadding(new Insets(3, 8, 4, 8));

        contentBox = new HBox(4);
        contentBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        if (nodes != null) {
            contentBox.getChildren().addAll(nodes);
        }

        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("ribbon-group-title");

        getChildren().addAll(contentBox, titleLabel);
    }

    public void add(Node node) {
        contentBox.getChildren().add(node);
    }

    public HBox getContentBox() {
        return contentBox;
    }
}
