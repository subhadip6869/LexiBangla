package org.originsd.lexibangla.ui.ribbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Represents a ribbon tab page containing RibbonGroups.
 */
public class RibbonTab {

    private final String title;
    private final List<RibbonGroup> groups = new ArrayList<>();
    private final HBox container;

    public RibbonTab(String title, RibbonGroup... initialGroups) {
        this.title = title;
        this.container = new HBox(8);
        this.container.getStyleClass().add("ribbon-tab-content");
        this.container.setAlignment(Pos.CENTER_LEFT);
        this.container.setPadding(new Insets(4, 10, 4, 10));
        HBox.setHgrow(this.container, Priority.ALWAYS);

        if (initialGroups != null) {
            for (RibbonGroup group : initialGroups) {
                addGroup(group);
            }
        }
    }

    public void addGroup(RibbonGroup group) {
        if (!groups.isEmpty()) {
            // Add vertical divider between groups
            HBox divider = new HBox();
            divider.getStyleClass().add("ribbon-group-divider");
            container.getChildren().add(divider);
        }
        groups.add(group);
        container.getChildren().add(group);
    }

    public String getTitle() {
        return title;
    }

    public List<RibbonGroup> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    public Node getContent() {
        return container;
    }
}
